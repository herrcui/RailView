package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.element.dto.RelativePosition;
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
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railapp.units.UnitUtility;
import railview.simulation.container.CoordinateMapper;
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
import javafx.geometry.Point2D;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * The controller class for TrainRunMonitorPane.fxml. The Pane gives a
 * selectable list of trains where you can see their blockingTimeChart.
 * 
 */
public class TrainRunMonitorPaneController {

	protected static final BlockingTime blockingTime = null;

	@FXML
	private AnchorPane blockingTimePane, snapshotRoot, trainRoot, lineRoot,
			linePane, lineBlockingTimesPane;

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
	private static HashMap<String, Line> lineMap = new HashMap<String, Line>();
	private HashMap<AbstractTrainSimulator, List<Length>> opDistMap = new HashMap<AbstractTrainSimulator, List<Length>>();
	private Rectangle rectangle;

	double minX = Double.MAX_VALUE;
	double maxX = Double.MIN_VALUE;
	double maxY = Double.MIN_VALUE;
	double minY = Double.MAX_VALUE;

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
				Length.fromMeter(td.getDistance()));
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

								chart.setBlockingTime(getBlockingTimeStairway(
										train, null));

								chart.setEventsMap(getEvents(train,
										getTimeInDistance(train, null)));

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
								chart.setBlockingTime(getBlockingTimeStairway(
										train, null));
							} catch (Exception e) {
							}

							chart.setEventsMap(getEvents(train,
									getTimeInDistance(train, null)));

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
					}

				});

		xAxis.setSide(Side.TOP);

		return chart;
	}

	private List<BlockingTime> getBlockingTimeStairway(
			AbstractTrainSimulator train, Line line) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();
		if (train instanceof TrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;

			List<ResourceOccupancy> resourceOccupancies = ((TrainSimulator) train)
					.getBlockingTimeStairWay();

			/*
			 * List<ResourceOccupancy> scheduldResourceOccupancies =
			 * this.trainRunPredictor .getScheduledBlockingTime().get(train);
			 * Course course =
			 * this.trainRunPredictor.getScheduledCourseMap().get( train);
			 */
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

				if (line == null) {
					blockingTimes.add(new BlockingTime(startMeter, endMeter,
							startTimeInSecond, endTimeInSecond));
				} else {
					List<Length> opDistances = this.getOpDistances(train);

					double startDist = this.getDistanceInLine(opDistances,
							startMeter, train);
					double endDist = this.getDistanceInLine(opDistances,
							endMeter, train);
					blockingTimes.add(new BlockingTime(startDist, endDist,
							startTimeInSecond, endTimeInSecond));
				}

				meter = endMeter;
			}

		}

		return blockingTimes;
	}

	private List<Length> getOpDistances(AbstractTrainSimulator train) {
		List<Length> opDistances = this.opDistMap.get(train);

		if (opDistances == null) {
			Length refDist = Length.fromMeter(0);
			opDistances = new ArrayList<Length>();
			for (TripElement elem : train.getTripSection().getTripElements()) {
				Length opDistance = train.getFullPath().findFirstDistance(
						(InfrastructureObject) elem.getOperationalPoint(),
						refDist);
				opDistances.add(opDistance);
				refDist = opDistance;
			}

			this.opDistMap.put(train, opDistances);
		}

		return opDistances;
	}

	private double getDistanceInLine(List<Length> opDistances, double meter,
			AbstractTrainSimulator train) {
		int index = 0;
		if (meter >= opDistances.get(opDistances.size() - 1).getMeter()) {
			return ((InfrastructureObject) train.getTripSection()
					.getTripElements().get(opDistances.size() - 1)
					.getOperationalPoint()).getElement().getStation()
					.getCoordinate().getX();
		}

		for (Length dist : opDistances) {
			if (index >= opDistances.size() - 1) {
				break;
			}

			if (opDistances == null || opDistances.get(index + 1) == null) {
				continue;
			}

			if (meter - dist.getMeter() >= UnitUtility.ERROR * -1
					&& opDistances.get(index + 1).getMeter() - meter >= UnitUtility.ERROR
							* -1) {

				Station startStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index)
						.getOperationalPoint()).getElement().getStation();
				Station endStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index + 1)
						.getOperationalPoint()).getElement().getStation();

				return startStation.getCoordinate().getX()
						+ (meter - dist.getMeter())	* 
						  (endStation.getCoordinate().getX() - startStation.getCoordinate().getX()) /
						  (opDistances.get(index + 1).getMeter() - opDistances.get(index).getMeter());
			}
			index++;
		}

		return -1;
	}

	// Map: Meter, TimeInSecond
	private List<TimeDistance> getTimeInDistance(AbstractTrainSimulator train,
			Line line) {
		List<TimeDistance> pointList = new ArrayList<TimeDistance>();
		double meter = 0; // x
		double timeInSecond = 0; // y

		for (DiscretePoint point : train.getWholeCoursePoints()) {
			timeInSecond += point.getDuration().getTotalSeconds();
			meter += point.getDistance().getMeter();
			if (line == null) {
				pointList.add(new TimeDistance(meter, timeInSecond));
			} else {
				List<Length> opDistances = this.getOpDistances(train);
				double distance = this.getDistanceInLine(opDistances, meter,
						train);
				pointList.add(new TimeDistance(distance, timeInSecond));
			}
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
						lastMeter += factor * (point.getDistance() - lastMeter);
					}
					break;
				} else {
					lastSecond = point.getSecond();
					lastMeter = point.getDistance();
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
			List<TimeDistance> timeDistances = this.getTimeInDistance(train,
					null);

			for (TimeDistance point : timeDistances) {
				courseForTimeSeries.getData().add(
						new Data<Number, Number>(point.getDistance(), (point
								.getSecond() * -1)));
			}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}

		return chart;
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


						//TODO are x,y-coordinates right?
						// max and min x-coordinate for the Mapper
						for (Station station : infraServiceUtility
								.getLineService().findStationsByLine(line)) {
							if (station.getCoordinate().getX() > maxX)
								maxX = station.getCoordinate().getX();
							if (station.getCoordinate().getX() < minX)
								minX = station.getCoordinate().getX();
							;
						}

						// max and minimum y-coordinate
						HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways = getAllBlockingTimeStairways(line);

						for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : blockingTimeStairways.entrySet()) {
							for (BlockingTime blockingTime : entry.getValue()) {
								double time = blockingTime.getEndTimeInSecond() + 
									entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
								if (time > maxY)
									maxY = time;
								if (time < minY)
									minY = time;
							}
						}

						CoordinateMapper mapper = new CoordinateMapper(maxX,
								minX, maxY, minY);
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

						lineBlockingTimesPane.getChildren().clear();
						drawAllBlockingtimesInLine(line);
						drawAllTimeDistanceInLine(line);
					}

				});

	}

	// for Kai
	private void drawAllBlockingtimesInLine(Line line) {
		HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways = this
				.getAllBlockingTimeStairways(line);
		if (blockingTimeStairways.size() > 0) {
			CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
			// TODO doesnt draw any rectangles
			for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : blockingTimeStairways.entrySet()) {
				double activeTime = entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
				for (BlockingTime blockingTime : entry.getValue()) {
					double startX = blockingTime.getStartDistance();
					double endX = blockingTime.getEndDistance();
					double startY = blockingTime.getStartTimeInSecond();
					double endY = blockingTime.getEndTimeInSecond();

					Rectangle rectangle = new Rectangle();

					rectangle.setY(mapper.mapToPaneY(maxY - (activeTime + startY),
							lineBlockingTimesPane));
					rectangle.setHeight(mapper.mapToPaneY(maxY - (activeTime + endY), lineBlockingTimesPane) - 
							mapper.mapToPaneY(maxY - (activeTime + startY), lineBlockingTimesPane));
					if (endX > startX) {
						rectangle.setX(mapper.mapToPaneX(startX,
								lineBlockingTimesPane));
						rectangle.setWidth(mapper.mapToPaneX(endX, lineBlockingTimesPane) - 
								mapper.mapToPaneX(startX, lineBlockingTimesPane));
					} else {
						rectangle.setX(mapper.mapToPaneX(endX,
								lineBlockingTimesPane));
						rectangle.setWidth(mapper.mapToPaneX(startX, lineBlockingTimesPane) - 
								mapper.mapToPaneX(endX, lineBlockingTimesPane));
					}
					lineBlockingTimesPane.getChildren().add(rectangle);
				}
			}
		}
	}

	private void drawAllTimeDistanceInLine(Line line) {
		HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistances = this
				.getAllTimeDistances(line);
		if (timeDistances.size() > 0) {
			//TODO wrong lines
			CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
			for (Entry<AbstractTrainSimulator, List<TimeDistance>> entry : timeDistances.entrySet()) {
				// Iterator to get the current and next distance and time value
				double activeTime = entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
				Iterator<TimeDistance> it = entry.getValue().iterator();
				TimeDistance previous = null;
				if (it.hasNext()) {
					previous = it.next();
				}
				while (it.hasNext()) {
					TimeDistance current = it.next();
					// Process previous and current here.
					javafx.scene.shape.Line polyLine = new javafx.scene.shape.Line();
					polyLine.setStartX(mapper.mapToPaneX(
							previous.getDistance(), lineBlockingTimesPane));
					polyLine.setEndX(mapper.mapToPaneX(current.getDistance(),
							lineBlockingTimesPane));
					polyLine.setStartY(mapper.mapToPaneY(maxY - (activeTime + previous.getSecond()),
							lineBlockingTimesPane));
					polyLine.setEndY(mapper.mapToPaneY(maxY - (activeTime + current.getSecond()),
							lineBlockingTimesPane));
					lineBlockingTimesPane.getChildren().add(polyLine);
					previous = current;
				}
			}
		}
	}

	static Double[] addElement(Double[] a, Double e) {
		a = Arrays.copyOf(a, a.length + 1);
		a[a.length - 1] = e;
		return a;
	}

	// kai, rectangle
	private HashMap<AbstractTrainSimulator, List<BlockingTime>> getAllBlockingTimeStairways(
			Line line) {
		HashMap<AbstractTrainSimulator, List<BlockingTime>> result = new HashMap<AbstractTrainSimulator, List<BlockingTime>>();

		for (AbstractTrainSimulator train : this.trainMap.values()) {
			result.put(train, this.getBlockingTimeStairway(train, line));
		}
		return result;
	}

	// kai, line
	private HashMap<AbstractTrainSimulator, List<TimeDistance>> getAllTimeDistances(
			Line line) {
		HashMap<AbstractTrainSimulator, List<TimeDistance>> result = new HashMap<AbstractTrainSimulator, List<TimeDistance>>();
		for (AbstractTrainSimulator train : this.trainMap.values()) {
			result.put(train, this.getTimeInDistance(train, line));
		}
		return result;
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
