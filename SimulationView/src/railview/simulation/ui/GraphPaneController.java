package railview.simulation.ui;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railapp.units.Time;
import railapp.units.UnitUtility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {
	
	@FXML
	private AnchorPane anchorPane;
	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private AnchorPane runningPane;

	
	@FXML
	private ListView<String> trainNumbers;
	
	NumberAxis yAxisChart1 = new NumberAxis();
	NumberAxis xAxis = new NumberAxis();	
	NumberAxis yAxis = new NumberAxis();

	
	@FXML
	public void initialize() {
        tabPane.setSide(Side.BOTTOM);
        
/**        anchorPane.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	chart1.setLayoutX((newSceneWidth.doubleValue() / 2)- (chart1.prefWidth(-1) / 2));
		    }
		});
        

        anchorPane.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	chart1.setLayoutY((newSceneHeight.doubleValue() / 2)- (chart1.prefHeight(-1) / 2));
		    }
		});
 **/       
 
        updateChart();
	}
	
	public void updateChart() {
		  trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
	            @Override
	            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) { 
        			System.out.println(getTrain(newValue));		
	            	drawSpeedLimitTable(getTrain(newValue));
           		    drawVelocityTable(getTrain(newValue));         		    	            		  
	            }
	        });   
	}
	
	public void setTrainList(List<AbstractTrainSimulator> trainList) {
        this.updateTrainMap(trainList);
	}
	
	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		// TODO
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new
			CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);

		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(), trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			numbers.add(trainNumber);
			}
		}
		trainNumbers.setItems(numbers);
		
	}
	
	
	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}
	

	public void drawVelocityTable(AbstractTrainSimulator train) {
	
		chart1 = new LineChart<>(xAxis, yAxis);
		xAxis.setLabel("Meter");
		yAxis.setLabel("velocityinKmH");
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
		for(Map.Entry<Double,Double> entry : getCourseForVelocity(train).entrySet()) {
			CourseForVelocitySeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
	}
		chart1.getData().add(CourseForVelocitySeries);
		runningPane.getChildren().add(chart1);
		chart1.setLayoutX(500);
		}
	}
	
	public void drawSpeedLimitTable(AbstractTrainSimulator train) {

		chart2 = new LineChart<>(xAxis, yAxisChart1);
		xAxis.setLabel("Meter");
		yAxisChart1.setLabel("maxKmH");
		XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();
		for(Map.Entry<Double,Double> entry: getSpeedLimit(train).entrySet()) {
			speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
		}
		chart2.getData().add(speedLimitSeries);
		runningPane.getChildren().addAll(chart2);
		chart2.setLayoutX(500);
		chart2.setLayoutY(350);
			
	}
	
	
	
	private Map<Double, Double> getCourseForVelocity(AbstractTrainSimulator train) {
		// Velocity
		
		Map<Double, Double> velocityMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double velocityInKmH = 0; // y
		for (DiscretePoint point : train.getCourse().getPoints()) {
			velocityInKmH = point.getVelocity().getKilometerPerHour();
			meter += point.getDistance().getMeter();
			velocityMap.put(meter, velocityInKmH);
		}
		return velocityMap;
	}
	
	private Map<Double, Double> getSpeedLimit(AbstractTrainSimulator train) {
		// Velocity
		Map<Double, Double> speedLimitMap = new LinkedHashMap<Double, Double>();
		
		LinkPath path = train.getTripSection().getFullPath();
		
		double maxTrainKmH = train.getTrainDefinition().getMaxVelocity().getKilometerPerHour();
		double maxKmH = Math.min(maxTrainKmH, 
			path.getLinkEdges().get(0).getLink().getGeometry().getMaxVelocity().getKilometerPerHour());
		double lastMeter = 0;
		double meter = 0;
		
		for (LinkEdge edge : path.getLinkEdges()) {
			if (edge.getLength().getMeter() == 0) {
				continue;
			}
			
			double linkKmH = edge.getLink().getGeometry().getMaxVelocity().getKilometerPerHour();
			
			if (Math.abs(Math.min(maxTrainKmH, linkKmH) - maxKmH) >= UnitUtility.ERROR) {
				speedLimitMap.put(lastMeter, maxKmH);
				speedLimitMap.put(meter, maxKmH);
				
				maxKmH = Math.min(maxTrainKmH, linkKmH);
				lastMeter = meter;
			}
			
			meter += edge.getLength().getMeter();
		}
		return speedLimitMap;
	}

	LineChart<Number, Number> chart1;
	LineChart<Number, Number> chart2;
	ObservableList<String> numbers = FXCollections.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap =
			new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
