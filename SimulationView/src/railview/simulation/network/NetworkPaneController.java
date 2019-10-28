package railview.simulation.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.signalling.dto.AbstractSignal;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Coordinate;
import railapp.units.Time;
import railview.simulation.ui.data.CoordinateMapper;
import railview.simulation.ui.utilities.NodeGestures;

/**
 * The controller class of NetworkPane.fxml. The Pane includes the elementPane and the trainPane.
 * 
 */
public class NetworkPaneController {
	@FXML
	private StackPane stackPane;

	private InfrastructureElementsPane elementPane;
	private TrainPane trainPane;
	private double pressedX, pressedY;
	   
	private boolean isActive = true;

	/**
	 * adds elementPane and trainPane to the stackPane
	 */
	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.trainPane = new TrainPane();
		this.stackPane.getChildren().add(this.elementPane);
		this.stackPane.getChildren().add(this.trainPane);
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		Collection<InfrastructureElement> elements = serviceUtility
				.getInfrastructureElementService().findElements();
		Collection<AbstractSignal> signals = new ArrayList<AbstractSignal>();

		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;

		for (InfrastructureElement element : elements) {
			for (Port port : element.getPorts()) {
				if (port.getCoordinate().getX() > maxX)
					maxX = port.getCoordinate().getX();
				if (port.getCoordinate().getX() < minX)
					minX = port.getCoordinate().getX();
				if (port.getCoordinate().getY() > maxY)
					maxY = port.getCoordinate().getY();
				if (port.getCoordinate().getY() < minY)
					minY = port.getCoordinate().getY();
			}
			
			signals.addAll(serviceUtility.getSignallingService().findAbstractSignalsByElement(element));
		}

		CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY,
				minY);

		this.elementPane.setCoordinateMapper(mapper);
		this.elementPane.setSignals(signals, Color.LIGHTGREEN);
		this.elementPane.setAndDrawElements(elements, Color.WHITE);
		
		this.trainPane.setCoordinateMapper(mapper);
	}

	/**
	 * updates the trainPane
	 * 
	 * @param map
	 * @param time
	 */
	public void updateTrainCoordinates(Map<AbstractTrainSimulator, List<Coordinate>> map,
			Time time) {
		if (isActive) {
			this.trainPane.updateTrainLocations(map, time);
		}
	}
	
	public void setSwarmManager(SwarmManager swarmManager) {
		this.trainPane.setSwarmManager(swarmManager);
	}
	
	public void setActive(boolean active) {
		this.isActive = active;
	}
	
	@FXML
	private void mouseEnter(){
		stackPane.setOnMousePressed(new EventHandler<MouseEvent>()
		        {
            public void handle(MouseEvent event)
            {
                pressedX = event.getX();
                pressedY = event.getY();
            }
        });

		stackPane.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
            	stackPane.setTranslateX(stackPane.getTranslateX() + event.getX() - pressedX);
            	stackPane.setTranslateY(stackPane.getTranslateY() + event.getY() - pressedY);

                event.consume();
            }
        });
	}
	
	@FXML
	private void scrollWheel(){
		NodeGestures elemNodeGestures = new NodeGestures(elementPane);
		NodeGestures trainNodeGestures = new NodeGestures(trainPane);
		
		stackPane.addEventFilter( ScrollEvent.ANY, elemNodeGestures.getOnScrollEventHandler());
		stackPane.addEventFilter( ScrollEvent.ANY, trainNodeGestures.getOnScrollEventHandler());
	}

}
	

