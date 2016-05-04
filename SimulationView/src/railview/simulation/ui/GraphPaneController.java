package railview.simulation.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;


public class GraphPaneController {
	
	@FXML
	private AnchorPane anchorPane;
	
	@FXML
	private TabPane tabPane;
	
	@FXML
	private AnchorPane runningPane;
	
	@FXML
	private AnchorPane speedprofilePane;
	
	@FXML
	private AnchorPane timeDistancePane;
	
	@FXML
	private ListView<String> trainNumbers;
	
	@FXML
	public void initialize() {
        tabPane.setSide(Side.BOTTOM);
        
        timeDistanceChart = createCourseForTimeChart();
        speedProfileChart = createVelocityChart();
        
        speedprofilePane.getChildren().add(speedProfileChart);
        timeDistancePane.getChildren().add(timeDistanceChart);    
        
        AnchorPane.setTopAnchor(timeDistanceChart, 0.0);
        AnchorPane.setLeftAnchor(timeDistanceChart, 0.0);
        AnchorPane.setRightAnchor(timeDistanceChart, 0.0);
        AnchorPane.setBottomAnchor(timeDistanceChart, 0.0);
        
        AnchorPane.setTopAnchor(speedProfileChart, 0.0);
        AnchorPane.setLeftAnchor(speedProfileChart, 0.0);
        AnchorPane.setRightAnchor(speedProfileChart, 0.0);
        AnchorPane.setBottomAnchor(speedProfileChart, 0.0);
  
        new ZoomOnlyX(speedProfileChart, speedprofilePane);
        new Zoom(timeDistanceChart, timeDistancePane);
	}
	
	@FXML
	private void resetSpeedProfile(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() == 2) {
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
			}
		}
	}
	
	@FXML
	private void resetTimeDistance(MouseEvent event) {
		if (event.getButton().equals(MouseButton.PRIMARY)) {
			if (event.getClickCount() == 2) {
				timeDistanceChart.getXAxis().setAutoRanging(true);
				timeDistanceChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	private LineChart<Number, Number> createVelocityChart() {
		NumberAxis xAxis = createXAxis();
	    NumberAxis yAxis = createYAxis();   
	    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
	    chart.setAnimated(false);
	    chart.setCreateSymbols(false);
	    trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) { 
    			System.out.println(getTrain(newValue));		
       		    drawVelocityTable(getTrain(newValue), chart); 

            }
        });
	    return chart ;
	}

	private LineChart<Number, Number> createCourseForTimeChart() {
		NumberAxis xAxis = createXAxis2();
	    NumberAxis yAxis = createYAxis2();  
	    LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
	    chart.setAnimated(false);
	    chart.setCreateSymbols(false);
	    trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) { 
    			System.out.println(getTrain(newValue));
    			drawCourseforTimeTable(getTrain(newValue), chart); 
    			yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
    			        @Override
    			        public String toString(Number value) {
    			        	while (value.doubleValue() != 0) {
    			            return String.format("%7.0f", -value.doubleValue());
    			        }
    			        	 return String.format("%7.0f", value.doubleValue());
    			        }
    			    });
            }
        });
	    xAxis.setSide(Side.TOP);
	    return chart ;
	}
	

    private NumberAxis createXAxis() {
        NumberAxis xAxis = new NumberAxis();
        return xAxis;
    }
    
    private NumberAxis createYAxis() {
    	NumberAxis yAxis = new NumberAxis();

        return yAxis;
    }
    
    private NumberAxis createXAxis2() {
        NumberAxis xAxis = new NumberAxis();
        return xAxis;
    }
    
    private NumberAxis createYAxis2() {
    	NumberAxis yAxis = new NumberAxis();
        return yAxis;
    }
    
    
/**
	
	public void updateChart() {
		  trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
	            @Override
	            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) { 
        			System.out.println(getTrain(newValue));		
//	            	drawSpeedLimitTable(getTrain(newValue));
           		    drawVelocityTable(getTrain(newValue));  
           		    drawCourseforTimeTable(getTrain(newValue));
	            }
	        });   
	}
**/	
	public void setTrainList(List<AbstractTrainSimulator> trainList) {
        this.updateTrainMap(trainList);
	}
	
	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new
			CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(), trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE &&
					!numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}
		trainNumbers.setItems(numbers);
		
	}
	
	
	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}
	

	public LineChart<Number, Number> drawVelocityTable(AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		chart.getData().clear(); 
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("course for velocity");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
		for(Map.Entry<Double,Double> entry : getCourseForVelocity(train).entrySet()) {
			CourseForVelocitySeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
	}
		chart.getData().add(CourseForVelocitySeries);
		XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();
		
		speedLimitSeries.setName("speedlimit");
		double y = -1;   
		speedLimitSeries.getData().add(new Data<Number, Number>(0, y));   
	    for(Map.Entry<Double,Double> entry: getSpeedLimit(train).entrySet()) {
	        if (y >= 0) {
	            speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), y));
	        }
	        speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
	        y = entry.getValue();
	    }
	    chart.getData().add(speedLimitSeries);

		chart.setCreateSymbols(false);
		}
		return chart;
	}

	public LineChart<Number, Number> drawCourseforTimeTable(AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		chart.getData().clear(); 
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;   
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));   
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for(Map.Entry<Double,Double> entry : getCourseForTime(train).entrySet()) {
				courseForTimeSeries.getData().add(new Data<Number, Number>(entry.getKey(), (entry.getValue()*-1)));
		}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}
		return chart;	
		
	}

	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForVelocity(AbstractTrainSimulator train) {
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
	
	// Map: Meter, TimeInSecond
	private Map<Double, Double> getCourseForTime(AbstractTrainSimulator train) {
		Map<Double, Double> timeMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double timeInSecond = 0; // y
		
		for (DiscretePoint point : train.getCourse().getPoints()) {
			timeInSecond += point.getDuration().getTotalSecond();
			meter += point.getDistance().getMeter();
			timeMap.put(meter, timeInSecond);
		}
		return timeMap;
	}
	
	private LinkedHashMap<Double, Double> getSpeedLimit(AbstractTrainSimulator train) {
		// Velocity
		LinkedHashMap<Double, Double> speedLimitMap = new LinkedHashMap<Double, Double>();
		
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
		
		speedLimitMap.put(lastMeter, maxKmH);
		speedLimitMap.put(meter, maxKmH);
		
		return speedLimitMap;
	}


	

	LineChart<Number, Number> timeDistanceChart;
	LineChart<Number, Number> speedProfileChart;
	ObservableList<String> numbers = FXCollections.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap =
			new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
