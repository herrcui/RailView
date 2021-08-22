package railview.simulation.applications;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.sun.javafx.charts.Legend;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.simulation.capacity.MovingBlockSpeedLimitsOptimizer;
import railapp.simulation.capacity.MovingBlockSpeedLimitsOptimizer.IUIController;
import railapp.simulation.capacity.OptimizeHeadwayInfo;
import railapp.simulation.runingdynamics.Course;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.UnitUtility;
import railview.simulation.SimulationFactory;
import railview.simulation.graph.runningdynamics.RunningDynamicsPaneController;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.TrainRunDataManager;
import railview.simulation.ui.utilities.DraggableChart;
import railview.simulation.ui.utilities.Zoom;

public class CapacityOptimizerController implements IUIController {
	@FXML
	private TextField textVStep, textLStep;

	@FXML
	private Button optimizeButton;

	@FXML
	private TableView<OptimizeHeadwayInfo> tableViewResult, tableViewOptimize;

	@FXML
	private AnchorPane speedLimitPane, headwayPane;

	private SimulationFactory simulationFactory;

	private DraggableChart<Number, Number> speedChart, headwayChart;

	private TrainRunDataManager trainRunDataManager = new TrainRunDataManager();

	@SuppressWarnings("unchecked")
	@FXML
	public void initialize() {
		// set tableview
		TableColumn<OptimizeHeadwayInfo, Double> optBottleneckColumn = new TableColumn<OptimizeHeadwayInfo, Double>("To Be Opt.Bottleneck");
		optBottleneckColumn.setCellValueFactory(new PropertyValueFactory<>("toBeOptimizedBottleneck"));

		TableColumn<OptimizeHeadwayInfo, Double> velocityColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Velocity");
        velocityColumn.setCellValueFactory(new PropertyValueFactory<>("velocity"));

        TableColumn<OptimizeHeadwayInfo, Double> meterColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Distance");
        meterColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));

        TableColumn<OptimizeHeadwayInfo, Double> optHeadwayColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Headway in Opt. Bottleneck");
        optHeadwayColumn.setCellValueFactory(new PropertyValueFactory<>("optimizedHeadway"));

        TableColumn<OptimizeHeadwayInfo, Double> bottleneckColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Bottleneck");
        bottleneckColumn.setCellValueFactory(new PropertyValueFactory<>("bottleneck"));

        TableColumn<OptimizeHeadwayInfo, Double> headwayColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Headway");
        headwayColumn.setCellValueFactory(new PropertyValueFactory<>("headway"));

        this.tableViewResult.getColumns().addAll(optBottleneckColumn, velocityColumn, meterColumn, optHeadwayColumn, bottleneckColumn, headwayColumn);

        TableColumn<OptimizeHeadwayInfo, Double> optBottleneckLogColumn = new TableColumn<OptimizeHeadwayInfo, Double>("To Be Opt.Bottleneck");
		optBottleneckLogColumn.setCellValueFactory(new PropertyValueFactory<>("toBeOptimizedBottleneck"));

		TableColumn<OptimizeHeadwayInfo, Double> velocityLogColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Velocity");
        velocityLogColumn.setCellValueFactory(new PropertyValueFactory<>("velocity"));

        TableColumn<OptimizeHeadwayInfo, Double> meterLogColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Distance");
        meterLogColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));

        TableColumn<OptimizeHeadwayInfo, Double> headwayLogColumn = new TableColumn<OptimizeHeadwayInfo, Double>("Headway");
        headwayLogColumn.setCellValueFactory(new PropertyValueFactory<>("headway"));

        this.tableViewOptimize.getColumns().addAll(optBottleneckLogColumn, velocityLogColumn, meterLogColumn, headwayLogColumn);

        this.speedChart = new DraggableChart<>(new NumberAxis(), new NumberAxis());
		this.initiateChart(this.speedChart, this.speedLimitPane);

		this.headwayChart =  new DraggableChart<>(new NumberAxis(), new NumberAxis());
		this.initiateChart(this.headwayChart, this.headwayPane);
	}

	private void initiateChart(DraggableChart<Number, Number> chart, AnchorPane pane) {
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		pane.getChildren().add(chart);
		new Zoom(chart, pane);

		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		AnchorPane.setBottomAnchor(chart, 0.0);
	}

	public void setSimulationFactory(SimulationFactory factory) {
		this.simulationFactory = factory;
	}

	@FXML
	protected void optimizeHeadway() {
		this.tableViewResult.getItems().clear();

		MovingBlockSpeedLimitsOptimizer optimizer = new MovingBlockSpeedLimitsOptimizer(
				this.simulationFactory.getInfraServiceUtility(),
				this.simulationFactory.getRollingStockServiceUtility(),
				this.simulationFactory.getTimeTableServiceUtility(),
				this.tableViewResult,
				this.tableViewOptimize,
				this);

		optimizer.start();
	}

	@SuppressWarnings("restriction")
	@Override
	public void updateOptimizationCharts(
			AbstractTrainSimulator train, LinkedHashMap<Double, Double> speedLimits, boolean isOrigin) {

		this.speedChart.getData().clear();
		this.speedChart.getXAxis().setAutoRanging(true);
		this.speedChart.getYAxis().setAutoRanging(true);

		// draw speed profile
		XYChart.Series<Number, Number> courseForVelocitySeries = new Series<Number, Number>();
		courseForVelocitySeries.setName("speed profile (km/h)");

		for (Map.Entry<Double, Double> entry : RunningDynamicsPaneController.getCourseForVelocity(train)
				.entrySet()) {
			courseForVelocitySeries.getData().add(
					new Data<Number, Number>(entry.getKey(), entry
							.getValue()));
		}
		speedChart.getData().add(courseForVelocitySeries);

		// draw speed limits
		XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();
		double y = -1;
		speedLimitSeries.getData().add(new Data<Number, Number>(0, y));

		for (Map.Entry<Double, Double> entry : speedLimits.entrySet()) {
			if (y >= 0) {
				speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), y));
			}
			speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
			y = entry.getValue();
		}

		speedChart.getData().add(speedLimitSeries);
		speedLimitSeries.nodeProperty().get().setStyle("-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");
		speedLimitSeries.setName("speed limits (km/h)");

		for(Node n : speedChart.getChildrenUnmodifiable()){
			if (n instanceof Legend) {
				for(Legend.LegendItem legendItem : ((Legend) n).getItems()){
					if (legendItem.getText().equals("speed limits (km/h)")) {
						legendItem.getSymbol().setStyle("-fx-background-color: #000000, white;");
					}
				}
			}
		}

		// drawHeadway
		//this.headwayChart.getData().clear();
		if (this.headwayChart.getData().size() > 1) {
			this.headwayChart.getData().remove(1);
		}

		this.headwayChart.getXAxis().setAutoRanging(true);
		this.headwayChart.getYAxis().setAutoRanging(true);

		List<BlockingTime> blockingTime = trainRunDataManager.getBlockingTimeStairway(train, null);
		XYChart.Series<Number, Number> headwaySeries = new Series<Number, Number>();

		for (BlockingTime entry : blockingTime) {
			headwaySeries.getData().add(new Data<Number, Number>(
				entry.getRelativeStartDistance(), entry.getDurationInSecond()));
			headwaySeries.getData().add(new Data<Number, Number>(
					entry.getRelativeEndDistance(), entry.getDurationInSecond()));
		}

		this.headwayChart.getData().add(headwaySeries);

		if (isOrigin) {
			headwaySeries.nodeProperty().get().setStyle("-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");
			headwaySeries.setName("Origin Headway (seconds)");
		} else {
			headwaySeries.nodeProperty().get().setStyle("-fx-stroke: #00ff00; -fx-stroke-dash-array: 0.1 5.0;");
			headwaySeries.setName("Optimized Headway (seconds)");
		}

		for(Node n : headwayChart.getChildrenUnmodifiable()){
			if (n instanceof Legend) {
				for(Legend.LegendItem legendItem : ((Legend) n).getItems()){
					if (legendItem.getText().equals("Origin Headway (seconds)")) {
						legendItem.getSymbol().setStyle("-fx-background-color: #000000, white;");
					}

					if (legendItem.getText().equals("Optimized Headway (seconds)")) {
						legendItem.getSymbol().setStyle("-fx-background-color: #00ff00, white;");
					}
				}
			}
		}
	}

	@FXML
	private void resetSpeedLimitChart(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				speedChart.getXAxis().setAutoRanging(true);
				speedChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	@FXML
	private void resetHeadwayChart(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				headwayChart.getXAxis().setAutoRanging(true);
				headwayChart.getYAxis().setAutoRanging(true);
			}
		}
	}
}
