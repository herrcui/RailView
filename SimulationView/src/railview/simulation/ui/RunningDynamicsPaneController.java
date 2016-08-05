package railview.simulation.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.runingdynamics.Course;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Energy;
import railapp.units.UnitUtility;
import railview.simulation.ui.components.DraggableChart;
import railview.simulation.ui.components.ZoomOnlyX;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class RunningDynamicsPaneController {
	@FXML
	private AnchorPane speedprofilePane;

	@FXML
	private AnchorPane energyPane;
	
	@FXML
	private ListView<String> trainNumbers;

	@FXML
	public void initialize() {
		speedProfileChart = this.createChart();
		energyChart = this.createChart();

		trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {
				
				speedProfileChart.getData().clear();
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
				drawVelocity(trainMap.get(newValue), speedProfileChart);
				
				energyChart.getData().clear();
				energyChart.getXAxis().setAutoRanging(true);
				energyChart.getYAxis().setAutoRanging(true);
				drawEnergy(trainMap.get(newValue));

			}
		});
		
		speedprofilePane.getChildren().add(speedProfileChart);
		energyPane.getChildren().add(energyChart);

		new ZoomOnlyX(speedProfileChart, speedprofilePane);
		new ZoomOnlyX(energyChart, energyPane);		

		initializeChart(speedProfileChart);
		initializeChart(energyChart);
	}
	
	private void initializeChart(DraggableChart<Number, Number> chart) {
		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		AnchorPane.setBottomAnchor(chart, 0.0);
		
		chart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		chart.startEventHandlers();
	}
	
	@FXML
	private void resetSpeedProfile(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
			}
		}
	}
	
	void setTrainMap(
			ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}
	
	void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}
	
	private DraggableChart<Number, Number> createChart() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		DraggableChart<Number, Number> chart = new DraggableChart<>(xAxis, yAxis);
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		
		return chart;
	}
	
	private LineChart<Number, Number> drawVelocity(
			AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("speed profile (km/h)");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (Map.Entry<Double, Double> entry : getCourseForVelocity(train)
					.entrySet()) {
				CourseForVelocitySeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
			}
			chart.getData().add(CourseForVelocitySeries);
			XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();
			
			double y = -1;
			speedLimitSeries.getData().add(new Data<Number, Number>(0, y));
			for (Map.Entry<Double, Double> entry : getSpeedLimit(train)
					.entrySet()) {
				if (y >= 0) {
					speedLimitSeries.getData().add(
							new Data<Number, Number>(entry.getKey(), y));
				}
				speedLimitSeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
				y = entry.getValue();
			}
			chart.getData().add(speedLimitSeries);
			speedLimitSeries.nodeProperty().get().setStyle(
							"-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");

			chart.setLegendVisible(false);

			chart.setCreateSymbols(false);
		}
		return chart;
	}
	
	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForVelocity(
			AbstractTrainSimulator train) {
		Map<Double, Double> velocityMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double velocityInKmH = 0; // y
		for (DiscretePoint point : train.getWholeCoursePoints()) {
			velocityInKmH = point.getVelocity().getKilometerPerHour();
			meter += point.getDistance().getMeter();
			velocityMap.put(meter, velocityInKmH);
		}
		return velocityMap;
	}
	
	private LinkedHashMap<Double, Double> getSpeedLimit(
			AbstractTrainSimulator train) {
		// Velocity
		LinkedHashMap<Double, Double> speedLimitMap = new LinkedHashMap<Double, Double>();

		LinkPath path = train.getFullPath();

		double maxTrainKmH = train.getTrainDefinition().getMaxVelocity()
				.getKilometerPerHour();
		double maxKmH = Math
				.min(maxTrainKmH, path.getEdges().get(0).getLink()
						.getGeometry().getMaxVelocity().getKilometerPerHour());
		double lastMeter = 0;
		double meter = 0;

		for (LinkEdge edge : path.getEdges()) {
			if (edge.getLength().getMeter() == 0) {
				continue;
			}

			double linkKmH = edge.getLink().getGeometry().getMaxVelocity()
					.getKilometerPerHour();

			if (Math.abs(Math.min(maxTrainKmH, linkKmH) - maxKmH) >= UnitUtility.ERROR) {
				speedLimitMap.put(lastMeter, maxKmH);
				speedLimitMap.put(meter, maxKmH);

				maxKmH = Math.min(maxTrainKmH, linkKmH);
				lastMeter = meter;
			}

			meter += edge.getLength().getMeter();
		}

		speedLimitMap.put(lastMeter, maxKmH);
		speedLimitMap.put(meter, maxKmH);

		return speedLimitMap;
	}
	
	private void drawEnergy(AbstractTrainSimulator train) {
		XYChart.Series<Number, Number> courseForEnergy = new Series<Number, Number>();
		courseForEnergy.setName("energy consumption (KWH)");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (Map.Entry<Double, Double> entry : getCourseForEnergy(train).entrySet()) {
				courseForEnergy.getData().add(
						new Data<Number, Number>(entry.getKey(), entry.getValue()));
			}
			energyChart.getData().add(courseForEnergy);
			energyChart.setCreateSymbols(false);
		}
	}
	
	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForEnergy(AbstractTrainSimulator train) {
		Map<Double, Double> energyMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double accumlatedEnergy = 0; // y
		Energy[] energies = Course.calculateEnergy(train.getWholeCoursePoints(), train.getTrainDefinition());
		int index = 0;
		for (DiscretePoint point : train.getWholeCoursePoints()) {
			if (energies[index] != null) {
				accumlatedEnergy += energies[index].getKWH();
			}
			meter += point.getDistance().getMeter();
			energyMap.put(meter, accumlatedEnergy);
			
			index++;
		}
		
		return energyMap;
	}
	
	private DraggableChart<Number, Number> speedProfileChart;
	private DraggableChart<Number, Number> energyChart;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
}
