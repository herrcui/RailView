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
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

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
	NumberAxis xAxis2 = new NumberAxis();	
	NumberAxis yAxis2 = new NumberAxis();
	
	@FXML
	public void initialize() {
        tabPane.setSide(Side.BOTTOM);

        updateChart();
        
        chart2 = createChart();
        
        final StackPane chartContainer = new StackPane();
		chartContainer.getChildren().add(chart2);
		
        final Rectangle zoomRect = new Rectangle();
        zoomRect.setManaged(false);
		zoomRect.setFill(Color.LIGHTSEAGREEN.deriveColor(0, 1, 1, 0.5));
		chartContainer.getChildren().add(zoomRect);
		
		setUpZooming(zoomRect, chart2);
		
        final Button zoomButton = new Button("Zoom");
		final Button resetButton = new Button("Reset");
		zoomButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                doZoom(zoomRect, chart2);
            }
        });
		resetButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                xAxis.setLowerBound(0);
                xAxis.setUpperBound(1000);
                yAxis.setLowerBound(0);
                yAxis.setUpperBound(1000);
                
                zoomRect.setWidth(0);
                zoomRect.setHeight(0);
            }
        });
		
		final HBox controls = new HBox(10);
		controls.setPadding(new Insets(10));
		controls.setAlignment(Pos.CENTER);
		
		final BooleanBinding disableControls = 
		        zoomRect.widthProperty().lessThan(5)
		        .or(zoomRect.heightProperty().lessThan(5));
		zoomButton.disableProperty().bind(disableControls);
		controls.getChildren().addAll(zoomButton, resetButton);
		
		runningPane.getChildren().add(controls);
		
	}
	
	private LineChart<Number, Number> createChart() {
	   xAxis = createAxis();
	    yAxis = createAxis();	    
	    final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
	    return chart ;
	}

    private NumberAxis createAxis() {
        final NumberAxis xAxis = new NumberAxis();
	    xAxis.setAutoRanging(false);

        return xAxis;
    }
    

	
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
//		xAxis.setLabel("Meter");
		xAxis.setLabel("meter");
		yAxis.setLabel("kmH");
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("course for velocity");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
		for(Map.Entry<Double,Double> entry : getCourseForVelocity(train).entrySet()) {
			CourseForVelocitySeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
	}
		chart1.getData().add(CourseForVelocitySeries);
		

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
	    chart1.getData().add(speedLimitSeries);
	    
		runningPane.getChildren().add(chart1);
		chart1.setLayoutX(500);
		chart1.setCreateSymbols(false);
		}
	}

	public void drawCourseforTimeTable(AbstractTrainSimulator train) {

		chart2 = new LineChart<>(xAxis2, yAxis2);
		xAxis2.setLabel("Meter");
		yAxis2.setLabel("Time in second");

		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		double y = -1;   
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));   
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for(Map.Entry<Double,Double> entry : getCourseForTime(train).entrySet()) {
				courseForTimeSeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
		}
			chart2.getData().add(courseForTimeSeries);
			runningPane.getChildren().add(chart2);
			chart2.setLayoutX(500);
			chart2.setLayoutY(400);
			chart2.setLegendVisible(false);
			chart2.setCreateSymbols(false);
		}
		
		
		
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
		return speedLimitMap;
	}
	
	
	
	
	private void setUpZooming(final Rectangle rect, final LineChart<Number, Number> zoomingNode) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
            }
        });
        zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
            }
        });
    }
    
    private void doZoom(Rectangle zoomRect, LineChart<Number, Number> chart) {
        Point2D zoomTopLeft = new Point2D(zoomRect.getX(), zoomRect.getY());
        Point2D zoomBottomRight = new Point2D(zoomRect.getX() + zoomRect.getWidth(), zoomRect.getY() + zoomRect.getHeight());
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0);
        double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() ;
        double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();
        xAxis.setLowerBound(xAxis.getLowerBound() + xOffset / xAxisScale);
        xAxis.setUpperBound(xAxis.getLowerBound() + zoomRect.getWidth() / xAxisScale);
        yAxis.setLowerBound(yAxis.getLowerBound() + yOffset / yAxisScale);
        yAxis.setUpperBound(yAxis.getLowerBound() - zoomRect.getHeight() / yAxisScale);
        System.out.println(yAxis.getLowerBound() + " " + yAxis.getUpperBound());
        zoomRect.setWidth(0);
        zoomRect.setHeight(0);
    }

	

	LineChart<Number, Number> chart1;
	LineChart<Number, Number> chart2;
	ObservableList<String> numbers = FXCollections.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap =
			new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
