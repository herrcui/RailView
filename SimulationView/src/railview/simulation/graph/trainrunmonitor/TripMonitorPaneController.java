package railview.simulation.graph.trainrunmonitor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.ui.components.BlockingTimeForTripChart;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.DraggableChart;
import railview.simulation.ui.utilities.Zoom;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

public class TripMonitorPaneController {
	@FXML
	private AnchorPane blockingTimePane, snapshotRoot;
	
	@FXML
	private Label eventLabel;
	
	@FXML
	private TableView<TableProperty> eventTable;
	
	private BlockingTimeForTripChart<Number, Number> tripChart;
	private StackPane snapshotPane;
	private SnapshotPaneController snapshotPaneController;
	
	private AbstractTrainSimulator train;
	private List<TimeDistance> timeDistances;
	
	@FXML
	public void initialize() {
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
		
		eventLabel.toFront();
		
		// initialize eventTable
		TableColumn eventItemCol = new TableColumn("Item");
		eventItemCol.setMinWidth(100);
		eventItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn eventValueCol = new TableColumn("Value");
		eventValueCol.setMinWidth(100);
		eventValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		eventTable.getColumns().addAll(eventItemCol, eventValueCol);
		eventValueCol.setCellFactory(TrainRunMonitorPaneController.createCellFactory());
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
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.snapshotPaneController.setInfrastructureServiceUtility(infraServiceUtility);
	}		

	/**
	 * draw the event points on the path in the snapshotPane
	 * 
	 * @param td
	 */
	public void drawEventOnSnap(TimeDistance td) {
		Coordinate coordinate = train.getFullPath().getCoordinate(
				Length.fromMeter(td.getDistance()));
		snapshotPaneController.setEventPoint(coordinate);
		snapshotPaneController.draw();
	}

	private BlockingTimeForTripChart<Number, Number> createBlockingTimeForTripChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		BlockingTimeForTripChart<Number, Number> chart = new BlockingTimeForTripChart<Number, Number>(
				xAxis, yAxis, eventLabel, eventTable, this);

		chart.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					chart.getBlockingTimeChartPlotChildren().clear();
					chart.getData().clear();

					drawTimeDistancesForTrip(train, timeDistances, chart);
				}

				chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						if (event.getButton().equals(MouseButton.SECONDARY)) {
							chart.getBlockingTimeChartPlotChildren().clear();
							chart.getData().clear();
							drawTimeDistancesForTrip(train, timeDistances, chart);
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
				drawTimeDistancesForTrip(train, timeDistances, chart);
		});

		blockingTimePane.heightProperty().addListener(
			(obs, oldVal, newVal) -> {
				chart.getData().clear();
				chart.getBlockingTimeChartPlotChildren().clear();
				drawTimeDistancesForTrip(train, timeDistances, chart);
		});
		
		xAxis.setSide(Side.TOP);

		return chart;
	}
	
	public void updateUI(AbstractTrainSimulator train,
			List<Coordinate> path,
			List<BlockingTime> blockingTime,
			List<TimeDistance> timeDistances,
			Map<TimeDistance, List<EventData>> events) {
		
		this.train = train;
		this.timeDistances = timeDistances;
		
		snapshotPaneController.setHighlightedPath(path);
		snapshotPaneController.setEventPoint(null);
		snapshotPaneController.draw();
		
		Time startTime = train.getTripSection().getStartTime();
		
		((NumberAxis) tripChart.getYAxis()).setTickLabelFormatter(new StringConverter<Number>() {
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
		
		if (tripChart.getData().isEmpty()) {
			try {
				tripChart.getData().clear();
				tripChart.getBlockingTimeChartPlotChildren().clear();
				tripChart.getXAxis().setAutoRanging(true);
				tripChart.getYAxis().setAutoRanging(true);
				drawTimeDistancesForTrip(train, timeDistances, tripChart);

				tripChart.setBlockingTime(blockingTime);

				tripChart.setEventsMap(events);

				tripChart.setAnimated(false);
				tripChart.setCreateSymbols(false);
				Thread.sleep(500);
				tripChart.getBlockingTimeChartPlotChildren().clear();
				tripChart.getData().clear();
				drawTimeDistancesForTrip(train, timeDistances, tripChart);
				// TODO arrows for events

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			tripChart.getData().clear();
			tripChart.getBlockingTimeChartPlotChildren().clear();
			tripChart.getXAxis().setAutoRanging(true);
			tripChart.getYAxis().setAutoRanging(true);
			drawTimeDistancesForTrip(train, timeDistances, tripChart);
			
			tripChart.setBlockingTime(blockingTime);
			tripChart.setEventsMap(events);

			tripChart.setAnimated(false);
			tripChart.setCreateSymbols(false);
		}
	}
	
	private void drawTimeDistancesForTrip(
			AbstractTrainSimulator train,
			List<TimeDistance> timeDistances,
			BlockingTimeForTripChart<Number, Number> chart) {
		
		if (train == null) {
			return;
		}
		
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (TimeDistance point : timeDistances) {
				courseForTimeSeries.getData().add(
						new Data<Number, Number>(point.getDistance(), (point.getSecond() * -1)));
			}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}
	}
}
