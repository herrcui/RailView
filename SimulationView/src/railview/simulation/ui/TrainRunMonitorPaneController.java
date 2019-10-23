package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.container.CoordinateMapper;
import railview.simulation.ui.components.BlockingTimeForTripChart;
import railview.simulation.ui.components.BlockingTimeForLineChart;
import railview.simulation.ui.components.DraggableChart;
import railview.simulation.ui.components.Zoom;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.data.TrainRunDataManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
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
			linePane, lineBlockingTimesAnchorPane;

	@FXML
	private ListView<String> trainNumbers, lineListView, stationListView;

	@FXML
	private Label eventLabel;

	@FXML
	private TableView<TableProperty> eventTable, trainInfoTable;

	@FXML
	private CheckBox selfEventCheckBox, inEventCheckBox, outEventCheckBox;

	private DraggableChart<Number, Number> blockingTimeChart;
	private BlockingTimeForLineChart<Number, Number> blockingTimeStairwaysChart;
	private StackPane snapshotPane;
	private SnapshotPaneController snapshotPaneController;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
	private IInfrastructureServiceUtility infrastructureServiceUtility;
	private static HashMap<String, Line> lineMap = new HashMap<String, Line>();
	private TrainRunDataManager trainRunDataManager = new TrainRunDataManager();
	
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double maxY = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;

	/**
	 * initialize the trainRunMonitorPane, add blockingTimeChart on top of it,
	 * add zoom function, load snapshotPane, add window resize listener, create
	 * eventTable and trainInfoTable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@FXML
	public void initialize() {

		lineBlockingTimesAnchorPane.setPickOnBounds(false);

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


	private BlockingTimeForTripChart<Number, Number> createBlockingTimeChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		BlockingTimeForTripChart<Number, Number> chart = new BlockingTimeForTripChart<Number, Number>(
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

								chart.setBlockingTime(trainRunDataManager.getBlockingTimeStairway(
										train, null));

								chart.setEventsMap(getEvents(train,
										trainRunDataManager.getTimeInDistance(train, null)));

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
								chart.setBlockingTime(trainRunDataManager.getBlockingTimeStairway(
										train, null));
							} catch (Exception e) {
							}

							chart.setEventsMap(getEvents(train,
								trainRunDataManager.getTimeInDistance(train, null)));

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

	private BlockingTimeForTripChart<Number, Number> drawCourseforTimeTable(
			AbstractTrainSimulator train,
			BlockingTimeForTripChart<Number, Number> chart) {
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			List<TimeDistance> timeDistances = trainRunDataManager.getTimeInDistance(train,
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
		this.trainRunDataManager.setInfraServiceUtility(infraServiceUtility);
		
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

						ObservableList<String> stationNameList = FXCollections
								.observableArrayList();

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
						HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways = 
								trainRunDataManager.getAllBlockingTimeStairwaysInLine(line, trainMap.values());

						for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : blockingTimeStairways
								.entrySet()) {
							for (BlockingTime blockingTime : entry.getValue()) {
								double time = blockingTime.getEndTimeInSecond()
										+ entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
								if (time > maxY)
									maxY = time;
								if (time < minY)
									minY = time;
							}
						}

						lineBlockingTimesAnchorPane.getChildren().clear();
						blockingTimeStairwaysChart = createBlockingTimeStairwayChart();
						blockingTimeStairwaysChart
								.getBlockingTimeStairwayChartPlotChildren()
								.clear();

						CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
						javafx.scene.shape.Line stationLine = new javafx.scene.shape.Line();
						stationLine.setStartX(mapper.mapToPaneX(minX, linePane));
						stationLine.setEndX(mapper.mapToPaneX(maxX, linePane));
						stationLine.setStartY(linePane.getHeight() / 2);
						stationLine.setEndY(linePane.getHeight() / 2);
						linePane.getChildren().add(stationLine);

						List<Station> stationList = new ArrayList<Station>();
						for (Station station : infraServiceUtility
								.getLineService().findStationsByLine(line)) {
							stationList.add(station);
							stationNameList.add(station.getName());

							blockingTimeStairwaysChart
									.setStationList(stationList);
							drawStations(stationList,
									blockingTimeStairwaysChart);
						}

						// to draw the rectangles directly into the chart
						blockingTimeStairwaysChart.setBlockingTimeStairwaysMap(
							trainRunDataManager.getAllBlockingTimeStairwaysInLine(line, trainMap.values()));

						// to draw the timeDistances directly into the chart
						blockingTimeStairwaysChart.setTimeDistancesMap(
							trainRunDataManager.getAllTimeDistancesInLine(line, trainMap.values()));

						stationListView.setItems(stationNameList);

						AnchorPane
								.setTopAnchor(blockingTimeStairwaysChart, 0.0);
						AnchorPane.setLeftAnchor(blockingTimeStairwaysChart,
								0.0);
						AnchorPane.setRightAnchor(blockingTimeStairwaysChart,
								0.0);
						AnchorPane.setBottomAnchor(blockingTimeStairwaysChart,
								0.0);

						drawAllTimeDistances(line, blockingTimeStairwaysChart);

						new Zoom(blockingTimeStairwaysChart, lineBlockingTimesAnchorPane);
						
						blockingTimeStairwaysChart.setMouseFilter(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent mouseEvent) {
								if (mouseEvent.getButton() == MouseButton.PRIMARY) {
								} else {
									mouseEvent.consume();
								}
							}
						});
						blockingTimeStairwaysChart.startEventHandlers();
						
						lineBlockingTimesAnchorPane.getChildren().add(
								blockingTimeStairwaysChart);

					}

				});
	}

	private BlockingTimeForLineChart<Number, Number> createBlockingTimeStairwayChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();

		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(minX);
		xAxis.setUpperBound(maxX);
		yAxis.setAutoRanging(true);

		BlockingTimeForLineChart<Number, Number> chart = new BlockingTimeForLineChart<Number, Number>(
				xAxis, yAxis);

		xAxis.setSide(Side.TOP);
		chart.setMaxY(maxY);

		return chart;
	}

	private BlockingTimeForLineChart<Number, Number> drawStations(
			List<Station> stationList,
			BlockingTimeForLineChart<Number, Number> chart) {
		XYChart.Series<Number, Number> stationSeries = new Series<Number, Number>();

		for (Station station : stationList) {
			stationSeries.getData()
					.add(new Data<Number, Number>(station.getCoordinate()
							.getX(), 0));

		}
		chart.getData().add(stationSeries);
	    return chart;
	}

	private BlockingTimeForLineChart<Number, Number> drawAllTimeDistances(
			Line line, BlockingTimeForLineChart<Number, Number> chart) {
		HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistances = trainRunDataManager
				.getAllTimeDistancesInLine(line, this.trainMap.values());

		chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
		chart.setLegendVisible(false);
		chart.setCreateSymbols(false);

		if (timeDistances.size() > 0) {
			for (Entry<AbstractTrainSimulator, List<TimeDistance>> entry : timeDistances
					.entrySet()) {
				XYChart.Series<Number, Number> timeDistancesSeries = new Series<Number, Number>();

				double activeTime = entry.getKey().getActiveTime()
						.getDifference(Time.getInstance(0, 0, 0))
						.getTotalSeconds();
				for (TimeDistance timeDistance : entry.getValue()) {
					if (timeDistance.getDistance() == -1) {
						if (timeDistancesSeries.getData().size() > 0) {
							// store series and prepare new series
							chart.getData().add(timeDistancesSeries);
							timeDistancesSeries.nodeProperty().get().setStyle("-fx-stroke: red");
							timeDistancesSeries = new Series<Number, Number>();
						}
					} else {
						timeDistancesSeries.getData().add(new Data<Number, Number>(
							timeDistance.getDistance(),
							(maxY - (activeTime + timeDistance.getSecond()))));
					}
				}

				if (timeDistancesSeries.getData().size() > 0) {
					chart.getData().add(timeDistancesSeries);
					timeDistancesSeries.nodeProperty().get().setStyle("-fx-stroke: red");
				}
			}

		}

		return chart;
	}

	@FXML
	private void resetZoomBlockingTimeStairways(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				blockingTimeStairwaysChart.getXAxis().setAutoRanging(true);
				blockingTimeStairwaysChart.getYAxis().setAutoRanging(true);
			}
		}
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
