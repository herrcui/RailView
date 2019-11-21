package railview.simulation.graph.trainrunmonitor;

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
import railview.simulation.ui.components.BlockingTimeForTripChart;
import railview.simulation.ui.components.BlockingTimeForLineChart;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.CoordinateMapper;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.data.TrainRunDataManager;
import railview.simulation.ui.utilities.DraggableChart;
import railview.simulation.ui.utilities.Zoom;
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

	private DraggableChart<Number, Number> tripChart;
	private BlockingTimeForLineChart<Number, Number> lineChart;
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

		tripChart = createBlockingTimeForTripChart();
		blockingTimePane.getChildren().add(tripChart);
		
		AnchorPane.setTopAnchor(tripChart, 0.0);
		AnchorPane.setLeftAnchor(tripChart, 0.0);
		AnchorPane.setRightAnchor(tripChart, 0.0);
		AnchorPane.setBottomAnchor(tripChart, 0.0);

		new Zoom(tripChart, blockingTimePane);

		tripChart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		tripChart.startEventHandlers();

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
					public void changed(ObservableValue<? extends Number> observableValue,
							Number oldSceneHeight,
							Number newSceneHeight) {
						blockingTimePane.setLayoutY((newSceneHeight.doubleValue() / 2)
								- (blockingTimePane.prefHeight(-1) / 2));
					}
				});

		// initialize eventTable
		TableColumn eventItemCol = new TableColumn("Item");
		eventItemCol.setMinWidth(100);
		eventItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn eventValueCol = new TableColumn("Value");
		eventValueCol.setMinWidth(100);
		eventValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		eventTable.getColumns().addAll(eventItemCol, eventValueCol);

		eventValueCol.setCellFactory(createCellFactory());

		// initialize trainInfoTable
		TableColumn trainItemCol = new TableColumn("Item");
		trainItemCol.setMinWidth(100);
		trainItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn trainValueCol = new TableColumn("Value");
		trainValueCol.setMinWidth(100);
		trainValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		trainInfoTable.getColumns().addAll(trainItemCol, trainValueCol);

		trainValueCol.setCellFactory(createCellFactory());

		lineChart = BlockingTimeForLineChart.createBlockingTimeChartForLine();
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
				tripChart.getXAxis().setAutoRanging(true);
				tripChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	private BlockingTimeForTripChart<Number, Number> createBlockingTimeForTripChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		BlockingTimeForTripChart<Number, Number> chart = new BlockingTimeForTripChart<Number, Number>(
				xAxis, yAxis, eventLabel, eventTable, this);

		trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				if (oldValue == null || !oldValue.equals(newValue)) {
					eventTable.getItems().clear();
				}
				lineRoot.setVisible(false);
				trainRoot.setVisible(true);

				AbstractTrainSimulator train = trainMap.get(
					trainNumbers.getSelectionModel().getSelectedItem().toString());

				trainInfoTable.setItems(generateTrainInfo(train, newValue));

				List<Coordinate> path = trainRunDataManager.getTrainPathCoordinates(train);
				snapshotPaneController.setHighlightedPath(path);
				snapshotPaneController.setEventPoint(null);
				snapshotPaneController.draw();

				if (chart.getData().isEmpty()) {
					try {
						chart.getData().clear();
						chart.getBlockingTimeChartPlotChildren().clear();
						tripChart.getXAxis().setAutoRanging(true);
						tripChart.getYAxis().setAutoRanging(true);
						drawTimeDistancesForTrip(train, chart);

						chart.setBlockingTime(trainRunDataManager.getBlockingTimeStairway(train, null));

						chart.setEventsMap(trainRunDataManager.getEvents(train));

						chart.setAnimated(false);
						chart.setCreateSymbols(false);

						Time startTime = train.getTripSection().getStartTime();

						yAxis.setTickLabelFormatter(new StringConverter<Number>() {
							@Override
							public String toString(Number t) {
								Time testTime = startTime.add(Duration.fromTotalSecond(-t.doubleValue()));
								return testTime.toString();
							}

							@Override
							public Number fromString(String string) {
								return 1;
							}
						});

						Thread.sleep(500);
						chart.getBlockingTimeChartPlotChildren().clear();
						chart.getData().clear();
						drawTimeDistancesForTrip(train, chart);
						// TODO arrows for events

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					chart.getData().clear();
					chart.getBlockingTimeChartPlotChildren().clear();
					tripChart.getXAxis().setAutoRanging(true);
					tripChart.getYAxis().setAutoRanging(true);
					drawTimeDistancesForTrip(train, chart);
					try {
						chart.setBlockingTime(trainRunDataManager.getBlockingTimeStairway(train, null));
					} catch (Exception e) {
					}

					chart.setEventsMap(trainRunDataManager.getEvents(train));

					chart.setAnimated(false);
					chart.setCreateSymbols(false);
					Time startTime = train.getTripSection().getStartTime();

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

						if (event.getButton().equals(MouseButton.PRIMARY)) {
							chart.getBlockingTimeChartPlotChildren().clear();
							chart.getData().clear();

							drawTimeDistancesForTrip(train, chart);
						}

						chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
							public void handle(MouseEvent event) {
								if (event.getButton().equals(MouseButton.SECONDARY)) {
									chart.getBlockingTimeChartPlotChildren().clear();
									chart.getData().clear();
									drawTimeDistancesForTrip(train, chart);
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
							chart.getBlockingTimeChartPlotChildren().clear();
							drawTimeDistancesForTrip(train, chart);
						});

				blockingTimePane.heightProperty().addListener(
						(obs, oldVal, newVal) -> {
							chart.getData().clear();
							chart.getBlockingTimeChartPlotChildren().clear();
							drawTimeDistancesForTrip(train, chart);
						});
			}

		});

		xAxis.setSide(Side.TOP);

		return chart;
	}

	private BlockingTimeForTripChart<Number, Number> drawTimeDistancesForTrip(
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
						new Data<Number, Number>(point.getDistance(), (point.getSecond() * -1)));
			}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}

		return chart;
	}

	public static Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>> createCellFactory() {
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

	public void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}

	public void setTrainMap(ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}

	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		
		this.snapshotPaneController.setInfrastructureServiceUtility(infraServiceUtility);
		this.trainRunDataManager.setInfraServiceUtility(infraServiceUtility);
		
		this.infrastructureServiceUtility = infraServiceUtility;
		
		for (Line line : this.infrastructureServiceUtility.getNetworkService().allLines(null)) {
			lineMap.put(line.getDescription(), line);
		} // Kai

		ObservableList<String> lineList = FXCollections.observableArrayList();

		for (Line line : lineMap.values()) {
			lineList.add(line.getName());
		}
		lineListView.setItems(lineList);

		lineListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (oldValue == null || !oldValue.equals(newValue)) {
					stationListView.getItems().clear();
					linePane.getChildren().clear();
				}
				lineRoot.setVisible(true);
				trainRoot.setVisible(false);

				Line line = lineMap.get(lineListView.getSelectionModel().getSelectedItem().toString());

				ObservableList<String> stationNameList = FXCollections.observableArrayList();

				// max and min x-coordinate for the Mapper
				for (Station station : infraServiceUtility.getLineService().findStationsByLine(line)) {
					if (station.getCoordinate().getX() > maxX)
						maxX = station.getCoordinate().getX();
					if (station.getCoordinate().getX() < minX)
						minX = station.getCoordinate().getX();
				}

				// max and minimum y-coordinate
				HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways = 
						trainRunDataManager.getBlockingTimeStairwaysInLine(line, trainMap.values());

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
				
				lineChart.setX(minX, maxX);

				lineBlockingTimesAnchorPane.getChildren().clear();
				//lineChart = createBlockingTimeChartForLine();
				lineChart.getBlockingTimeStairwayChartPlotChildren().clear();

				CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
				javafx.scene.shape.Line stationLine = new javafx.scene.shape.Line();
				stationLine.setStartX(mapper.mapToPaneX(minX, linePane));
				stationLine.setEndX(mapper.mapToPaneX(maxX, linePane));
				stationLine.setStartY(linePane.getHeight() / 2);
				stationLine.setEndY(linePane.getHeight() / 2);
				linePane.getChildren().add(stationLine);

				List<Station> stationList = new ArrayList<Station>();
				for (Station station : infraServiceUtility.getLineService().findStationsByLine(line)) {
					stationList.add(station);
					stationNameList.add(station.getName());

					lineChart.setStationList(stationList);
					lineChart.drawStations(stationList, lineChart);
				}

				// to draw the rectangles directly into the chart
				lineChart.setBlockingTimeStairwaysMap(
					trainRunDataManager.getBlockingTimeStairwaysInLine(line, trainMap.values()));

				// to draw the timeDistances directly into the chart
				lineChart.setTimeDistancesMap(
					trainRunDataManager.getTimeDistancesInLine(line, trainMap.values()));

				stationListView.setItems(stationNameList);

				AnchorPane.setTopAnchor(lineChart, 0.0);
				AnchorPane.setLeftAnchor(lineChart, 0.0);
				AnchorPane.setRightAnchor(lineChart, 0.0);
				AnchorPane.setBottomAnchor(lineChart, 0.0);

				lineChart.drawTimeDistances(lineChart);
				//lineChart.drawBlockingTimeStairway();

				//new Zoom(lineChart, lineBlockingTimesAnchorPane);
				
				lineChart.setMouseFilter(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent mouseEvent) {
						if (mouseEvent.getButton() != MouseButton.PRIMARY) {
							mouseEvent.consume();
						}
					}
				});
				lineChart.startEventHandlers();
				
				lineBlockingTimesAnchorPane.getChildren().add(lineChart);
			}
		});
	}

	@FXML
	private void resetZoomBlockingTimeStairways(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				lineChart.getXAxis().setAutoRanging(true);
				lineChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	public static ObservableList<TableProperty> generateTrainInfo(
			AbstractTrainSimulator train, String trainNumber) {
		
		ObservableList<TableProperty> observableTrainInfoList = FXCollections.observableArrayList();
		observableTrainInfoList.add(new TableProperty("Train Number", trainNumber));
		observableTrainInfoList.add(new TableProperty("State", 
			train.getTrain().getStatus() == SimpleTrain.ACTIVE ? "In operation ..."	: "Terminated"));
		List<TripElement> elements = train.getTripSection().getTripElements();
		observableTrainInfoList.add(new TableProperty("From",
			((InfrastructureObject) elements.get(0).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("To",
			((InfrastructureObject) elements.get(elements.size() - 1).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("Start time", 
			elements.get(0).getArriverTime().toString()));
		
		return observableTrainInfoList;
	}

}
