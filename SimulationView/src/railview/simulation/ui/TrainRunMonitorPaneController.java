package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.events.ScheduledEvent;
import railapp.simulation.events.totrain.AbstractEventToTrain;
import railapp.simulation.events.totrain.UpdateLocationEvent;
import railapp.simulation.infrastructure.PartialRouteResource;
import railapp.simulation.infrastructure.ResourceOccupancy;
import railapp.simulation.prediction.TrainRunPredictor;
import railapp.simulation.runingdynamics.Course;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.ui.components.BlockingTimeChart;
import railview.simulation.ui.components.DraggableChart;
import railview.simulation.ui.components.Zoom;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * The controller class for TrainRunMonitorPane.fxml. The Pane gives a
 * selectable list of trains where you can see their blockingTimeChart.
 * 
 */
public class TrainRunMonitorPaneController {

	@FXML
	private AnchorPane blockingTimePane, snapshotRoot, trainRoot, lineRoot,
			linePane;

	@FXML
	private ListView<String> trainNumbers, lineListView, stationListView;

	@FXML
	private Label eventLabel;

	@FXML
	private TableView<TableProperty> eventTable, trainInfoTable;

	@FXML
	private CheckBox selfEventCheckBox, inEventCheckBox, outEventCheckBox;

	private DraggableChart<Number, Number> blockingTimeChart;
	private StackPane snapshotPane;
	private SnapshotPaneController snapshotPaneController;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
	private IInfrastructureServiceUtility infrastructureServiceUtility;
	private TrainRunPredictor trainRunPredictor;
	private static HashMap<String, Line> lineMap = new HashMap<String, Line>();

	/**
	 * initialize the trainRunMonitorPane, add blockingTimeChart on top of it,
	 * add zoom function, load snapshotPane, add window resize listener, create
	 * eventTable and trainInfoTable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@FXML
	public void initialize() {

		eventLabel.toFront();

		blockingTimeChart = createBlockingTimeChart();

		blockingTimePane.getChildren().add(blockingTimeChart);

		AnchorPane.setTopAnchor(blockingTimeChart, 0.0);
		AnchorPane.setLeftAnchor(blockingTimeChart, 0.0);
		AnchorPane.setRightAnchor(blockingTimeChart, 0.0);
		AnchorPane.setBottomAnchor(blockingTimeChart, 0.0);

		new Zoom(blockingTimeChart, blockingTimePane);

		blockingTimeChart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		blockingTimeChart.startEventHandlers();

		try {
			FXMLLoader snapshotPaneLoader = new FXMLLoader();
			URL location = SnapshotPaneController.class
					.getResource("SnapshotPane.fxml");
			snapshotPaneLoader.setLocation(location);
			snapshotPane = (StackPane) snapshotPaneLoader.load();
			this.snapshotPaneController = snapshotPaneLoader.getController();

			this.snapshotRoot.getChildren().add(snapshotPane);
		} catch (IOException e) {
			e.printStackTrace();
		}

		snapshotRoot.widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(
					ObservableValue<? extends Number> observableValue,
					Number oldSceneWidth, Number newSceneWidth) {
				snapshotPane.setLayoutX((newSceneWidth.doubleValue() / 2)
						- (snapshotPane.prefWidth(-1) / 2));
			}
		});

		blockingTimePane.heightProperty().addListener(
				new ChangeListener<Number>() {
					@Override
					public void changed(
							ObservableValue<? extends Number> observableValue,
							Number oldSceneHeight, Number newSceneHeight) {
						blockingTimePane.setLayoutY((newSceneHeight
								.doubleValue() / 2)
								- (blockingTimePane.prefHeight(-1) / 2));
					}
				});

		// initialize eventTable
		TableColumn eventItemCol = new TableColumn("Item");
		eventItemCol.setMinWidth(100);
		eventItemCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"item"));

		TableColumn eventValueCol = new TableColumn("Value");
		eventValueCol.setMinWidth(100);
		eventValueCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"value"));

		eventTable.getColumns().addAll(eventItemCol, eventValueCol);

		eventValueCol.setCellFactory(createCellFactory());

		// initialize trainInfoTable
		TableColumn trainItemCol = new TableColumn("Item");
		trainItemCol.setMinWidth(100);
		trainItemCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"item"));

		TableColumn trainValueCol = new TableColumn("Value");
		trainValueCol.setMinWidth(100);
		trainValueCol
				.setCellValueFactory(new PropertyValueFactory<TableProperty, String>(
						"value"));

		trainInfoTable.getColumns().addAll(trainItemCol, trainValueCol);

		trainValueCol.setCellFactory(createCellFactory());

	}

	/**
	 * draw the event points on the path in the snapshotPane
	 * 
	 * @param td
	 */
	public void drawEventOnSnap(TimeDistance td) {
		AbstractTrainSimulator train = trainMap.get(trainNumbers
				.getSelectionModel().getSelectedItem().toString());
		Coordinate coordinate = train.getFullPath().getCoordinate(
				Length.fromMeter(td.getMeter()));
		snapshotPaneController.setEventPoint(coordinate);
		snapshotPaneController.draw();
	}

	/**
	 * reset the zoom of the blockingTimeChart
	 * 
	 * @param event
	 */
	@FXML
	private void resetZoomBlockingTime(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				blockingTimeChart.getXAxis().setAutoRanging(true);
				blockingTimeChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	private BlockingTimeChart<Number, Number> createBlockingTimeChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		BlockingTimeChart<Number, Number> chart = new BlockingTimeChart<Number, Number>(
				xAxis, yAxis, eventLabel, eventTable, this);

		trainNumbers.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						if (oldValue == null || !oldValue.equals(newValue)) {
							eventTable.getItems().clear();
						}
						lineRoot.setVisible(false);
						trainRoot.setVisible(true);

						AbstractTrainSimulator train = trainMap
								.get(trainNumbers.getSelectionModel()
										.getSelectedItem().toString());

						trainInfoTable.setItems(generateTrainInfo(train,
								newValue));

						List<Coordinate> path = getTrainPathCoordinates(train);
						snapshotPaneController.setHighlightedPath(path);
						snapshotPaneController.setEventPoint(null);
						snapshotPaneController.draw();

						if (chart.getData().isEmpty()) {
							try {

								chart.getData().clear();
								chart.getBlockingTimeChartPlotChildren()
										.clear();
								blockingTimeChart.getXAxis().setAutoRanging(
										true);
								blockingTimeChart.getYAxis().setAutoRanging(
										true);
								drawCourseforTimeTable(train, chart);

								chart.setBlockingTime(getBlockingTimeStairway(train));

								chart.setEventsMap(getEvents(train,
										getTimeInDistance(train)));

								chart.setAnimated(false);
								chart.setCreateSymbols(false);

								Time startTime = train.getTripSection()
										.getStartTime();

								yAxis.setTickLabelFormatter(new StringConverter<Number>() {
									@Override
									public String toString(Number t) {
										Time testTime = startTime.add(Duration
												.fromTotalSecond(-t
														.doubleValue()));
										return testTime.toString();
									}

									@Override
									public Number fromString(String string) {
										return 1;
									}
								});

								Thread.sleep(500);
								chart.getBlockingTimeChartPlotChildren()
										.clear();
								chart.getData().clear();
								drawCourseforTimeTable(train, chart);
								// TODO arrows for events

							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {

							chart.getData().clear();
							chart.getBlockingTimeChartPlotChildren().clear();
							blockingTimeChart.getXAxis().setAutoRanging(true);
							blockingTimeChart.getYAxis().setAutoRanging(true);
							drawCourseforTimeTable(train, chart);
							try {
								chart.setBlockingTime(getBlockingTimeStairway(train));
							} catch (Exception e) {
							}

							chart.setEventsMap(getEvents(train,
									getTimeInDistance(train)));

							chart.setAnimated(false);
							chart.setCreateSymbols(false);
							Time startTime = train.getTripSection()
									.getStartTime();

							yAxis.setTickLabelFormatter(new StringConverter<Number>() {

								@Override
								public String toString(Number t) {
									Time testTime = startTime.add(Duration
											.fromTotalSecond(-t.doubleValue()));
									return testTime.toString();
								}

								@Override
								public Number fromString(String string) {
									return 1;
								}
							});

						}

						chart.setOnMouseDragged(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {

								if (event.getButton().equals(
										MouseButton.PRIMARY)) {

									chart.getBlockingTimeChartPlotChildren()
											.clear();
									chart.getData().clear();

									drawCourseforTimeTable(train, chart);
								}

								chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
									public void handle(MouseEvent event) {
										if (event.getButton().equals(
												MouseButton.SECONDARY)) {
											chart.getBlockingTimeChartPlotChildren()
													.clear();
											chart.getData().clear();
											drawCourseforTimeTable(train, chart);
										}
									}
								});
							}
						});

						// window resize listener
						// TODO

						blockingTimePane.widthProperty().addListener(
								(obs, oldVal, newVal) -> {
									chart.getData().clear();
									chart.getBlockingTimeChartPlotChildren()
											.clear();
									drawCourseforTimeTable(train, chart);
								});

						blockingTimePane.heightProperty().addListener(
								(obs, oldVal, newVal) -> {
									chart.getData().clear();
									chart.getBlockingTimeChartPlotChildren()
											.clear();
									drawCourseforTimeTable(train, chart);
								});

						/**
						 * ChangeListener<Number> stageSizeListener = (obs,
						 * oldVal, newVal) -> chart.getData().clear();
						 * chart.getBlockingTimeChartPlotChildren().clear();
						 * drawCourseforTimeTable(train, chart);
						 * 
						 * 
						 * 
						 * blockingTimePane.widthProperty().addListener(
						 * stageSizeListener);
						 * blockingTimePane.heightProperty().
						 * addListener(stageSizeListener);
						 **/
					}

				});

		xAxis.setSide(Side.TOP);

		return chart;
	}

	private List<BlockingTime> getBlockingTimeStairway(
			AbstractTrainSimulator train) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();
		if (train instanceof TrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;

			List<ResourceOccupancy> resourceOccupancies = ((TrainSimulator) train)
					.getBlockingTimeStairWay();

			List<ResourceOccupancy> scheduldResourceOccupancies = this.trainRunPredictor
					.getScheduledBlockingTime().get(train);
			Course course = this.trainRunPredictor.getScheduledCourseMap().get(
					train);
			// for Kai

			Time trainStartTime = train.getTripSection().getStartTime();

			for (ResourceOccupancy resourceOccupancy : resourceOccupancies) {
				if (headDistanceInFirstResource == null) {
					headDistanceInFirstResource = ((PartialRouteResource) resourceOccupancy
							.getResource()).getPath().findFirstDistance(
							(InfrastructureObject) train.getTripSection()
									.getTripElements().get(0)
									.getOperationalPoint());
					if (headDistanceInFirstResource == null) {
						continue;
					}
				}

				if (resourceOccupancy.getReleaseTime() == null) {
					break;
				}

				double startMeter = meter;
				double endMeter = meter
						+ ((PartialRouteResource) resourceOccupancy
								.getResource()).getPartialRoute().getPath()
								.getLength().getMeter();
				double startTimeInSecond = resourceOccupancy.getGrantTime()
						.getDifference(trainStartTime).getTotalSeconds();
				double endTimeInSecond = resourceOccupancy.getReleaseTime()
						.getDifference(trainStartTime).getTotalSeconds();

				if (blockingTimes.size() == 0) { // for the first resource
					endMeter = endMeter
							- headDistanceInFirstResource.getMeter();
				}

				blockingTimes.add(new BlockingTime(startMeter, endMeter,
						startTimeInSecond, endTimeInSecond));

				meter = endMeter;
			}

		}

		return blockingTimes;
	}

	// Map: Meter, TimeInSecond
	private List<TimeDistance> getTimeInDistance(AbstractTrainSimulator train) {
		List<TimeDistance> pointList = new ArrayList<TimeDistance>();
		double meter = 0; // x
		double timeInSecond = 0; // y

		for (DiscretePoint point : train.getWholeCoursePoints()) {
			timeInSecond += point.getDuration().getTotalSeconds();
			meter += point.getDistance().getMeter();
			pointList.add(new TimeDistance(meter, timeInSecond));
		}
		return pointList;
	}

	private List<Coordinate> getTrainPathCoordinates(
			AbstractTrainSimulator train) {
		List<Coordinate> coordniates = new ArrayList<Coordinate>();
		for (LinkEdge edge : train.getFullPath().getEdges()) {
			coordniates.addAll(edge.getCoordinates());
		}

		return coordniates;
	}

	private Map<TimeDistance, List<EventData>> getEvents(
			AbstractTrainSimulator train, List<TimeDistance> timeDistances) {
		Map<TimeDistance, List<EventData>> eventsMap = new HashMap<TimeDistance, List<EventData>>();

		for (ScheduledEvent scheduledEvent : train.getEvents()) {
			if (scheduledEvent instanceof UpdateLocationEvent) {
				continue;
			}

			double second = scheduledEvent.getScheduleTime()
					.getDifference(train.getTripSection().getStartTime())
					.getTotalSeconds();
			double lastSecond = 0;
			double lastMeter = 0;

			// second and meter in timeDistances are accumulated value
			for (TimeDistance point : timeDistances) {
				if (point.getSecond() >= second) {
					if (point.getSecond() - lastSecond != 0) {
						double factor = (second - lastSecond)
								/ (point.getSecond() - lastSecond);
						lastMeter += factor * (point.getMeter() - lastMeter);
					}
					break;
				} else {
					lastSecond = point.getSecond();
					lastMeter = point.getMeter();
				}
			}

			TimeDistance entry = new TimeDistance(lastMeter, second);
			int type = EventData.IN;
			if (scheduledEvent.getSource().equals(train)) {
				type = EventData.SELF;
			}

			String text = scheduledEvent instanceof AbstractEventToTrain ? ((AbstractEventToTrain) scheduledEvent)
					.getEventString() : scheduledEvent.toString();

			List<EventData> events = eventsMap.get(entry);
			if (events == null) {
				events = new ArrayList<EventData>();
				eventsMap.put(entry, events);
			}

			EventData event = new EventData(entry, type, scheduledEvent
					.getClass().getSimpleName(), text);
			events.add(event);
		}

		return eventsMap;
	}

	private BlockingTimeChart<Number, Number> drawCourseforTimeTable(
			AbstractTrainSimulator train,
			BlockingTimeChart<Number, Number> chart) {
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			List<TimeDistance> timeDistances = this.getTimeInDistance(train);

			for (TimeDistance point : timeDistances) {
				courseForTimeSeries.getData().add(
						new Data<Number, Number>(point.getMeter(), (point
								.getSecond() * -1)));
			}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}

		return chart;
	}

	private Collection<Station> getStationByLines(Line line) {
		return this.infrastructureServiceUtility.getLineService()
				.findStationsByLine(line);
	}

	static Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>> createCellFactory() {
		return new Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>>() {
			@Override
			public TableCell<TableProperty, String> call(
					TableColumn<TableProperty, String> param) {
				TableCell<TableProperty, String> cell = new TableCell<>();
				Text text = new Text();
				cell.setGraphic(text);
				cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
				text.wrappingWidthProperty().bind(cell.widthProperty());
				text.textProperty().bind(cell.itemProperty());
				return cell;
			}
		};
	}

	void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}

	void setTrainMap(ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}

	void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility infraServiceUtility) {
		this.snapshotPaneController
				.setInfrastructureServiceUtility(infraServiceUtility);
		this.infrastructureServiceUtility = infraServiceUtility;
		for (Line line : this.infrastructureServiceUtility.getNetworkService()
				.allLines(null)) {
			lineMap.put(line.getDescription(), line);
		} // Kai

		ObservableList<String> lineList = FXCollections.observableArrayList();

		for (Line line : lineMap.values()) {
			lineList.add(line.getName());
		}
		lineListView.setItems(lineList);

		lineListView.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						if (oldValue == null || !oldValue.equals(newValue)) {
							stationListView.getItems().clear();
							linePane.getChildren().clear();
						}
						lineRoot.setVisible(true);
						trainRoot.setVisible(false);

						Line line = lineMap.get(lineListView
								.getSelectionModel().getSelectedItem()
								.toString());

						ObservableList<String> stationList = FXCollections
								.observableArrayList();

						double maxX = getMaxX(line);
						double minX = getMinX(line);

						LineMapper mapper = new LineMapper(maxX, minX);
						javafx.scene.shape.Line stationLine = new javafx.scene.shape.Line();
						stationLine.setStartX(mapper.mapToPaneX(minX, linePane));
						stationLine.setEndX(mapper.mapToPaneX(maxX, linePane));
						stationLine.setStartY(linePane.getHeight() / 2);
						stationLine.setEndY(linePane.getHeight() / 2);
						linePane.getChildren().add(stationLine);

						for (Station station : infraServiceUtility
								.getLineService().findStationsByLine(line)) {
							stationList.add(station.getName());

							Circle circle = new Circle();
							circle.setCenterX(mapper.mapToPaneX(station
									.getCoordinate().getX(), linePane));
							circle.setCenterY(linePane.getHeight() / 2);
							circle.setRadius(5);
							
							Label stationLabel = new Label();
							stationLabel.setText(station.getName());
							stationLabel.setVisible(true);
							stationLabel.setTranslateX(mapper.mapToPaneX(
									station.getCoordinate().getX()
											- stationLabel.getWidth(), linePane));
							stationLabel.setTranslateY(linePane.getHeight() / 2
									- linePane.getHeight() / 15);
							
							linePane.getChildren().addAll(circle, stationLabel);

						}

						stationListView.setItems(stationList);

					}

				});

	}

	private double getMinX(Line line) {
		double minX = Double.MAX_VALUE;
		for (Station station : this.infrastructureServiceUtility
				.getLineService().findStationsByLine(line)) {
			if (station.getCoordinate().getX() < minX)
				minX = station.getCoordinate().getX();
		}
		return minX;
	}

	private double getMaxX(Line line) {
		double maxX = Double.MIN_VALUE;
		for (Station station : this.infrastructureServiceUtility
				.getLineService().findStationsByLine(line)) {
			if (station.getCoordinate().getX() > maxX)
				maxX = station.getCoordinate().getX();
		}
		return maxX;
	}

	void setTrainRunPredictor(TrainRunPredictor trainRunPredictor) {
		this.trainRunPredictor = trainRunPredictor;
	}

	static ObservableList<TableProperty> generateTrainInfo(
			AbstractTrainSimulator train, String trainNumber) {
		ObservableList<TableProperty> observableTrainInfoList = FXCollections
				.observableArrayList();
		observableTrainInfoList.add(new TableProperty("Train Number",
				trainNumber));
		observableTrainInfoList.add(new TableProperty("State", train.getTrain()
				.getStatus() == SimpleTrain.ACTIVE ? "In operation ..."
				: "Terminated"));
		List<TripElement> elements = train.getTripSection().getTripElements();
		observableTrainInfoList.add(new TableProperty("From",
				((InfrastructureObject) elements.get(0).getOperationalPoint())
						.getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("To",
				((InfrastructureObject) elements.get(elements.size() - 1)
						.getOperationalPoint()).getElement().getStation()
						.getDescription()));
		observableTrainInfoList.add(new TableProperty("Start time", elements
				.get(0).getArriverTime().toString()));
		return observableTrainInfoList;
	}

}
