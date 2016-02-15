package railview.simulation.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.UnitUtility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
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
	private LineChart chart1;
	
	@FXML
	public void initialize() {
        tabPane.setSide(Side.BOTTOM);
        
        anchorPane.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	chart1.setLayoutX((newSceneWidth.doubleValue() / 2)- (chart1.prefWidth(-1) / 2));
		    }
		});
        

        anchorPane.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	chart1.setLayoutY((newSceneHeight.doubleValue() / 2)- (chart1.prefHeight(-1) / 2));
		    }
		});
	}
	
	public void setTrainList(List<AbstractTrainSimulator> trainList) {
        this.trainList = trainList;
	}
	
	public void updateTrainTableList() {
		// TODO
		for (AbstractTrainSimulator trainSimulator : trainList) {
			String trainNumber = trainSimulator.getTrain().getNumber();
		}
	}
	
	public void drawRunningDynamics(AbstractTrainSimulator train) {
		// TODO
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			// draw
		}
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
	
	private List<AbstractTrainSimulator> trainList;
}
