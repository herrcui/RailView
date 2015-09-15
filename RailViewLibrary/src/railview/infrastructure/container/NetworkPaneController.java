package railview.infrastructure.container;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.Swarm;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Coordinate;
import railapp.units.Time;

public class NetworkPaneController {
	@FXML
	private StackPane stackPane;

	final double SCALE_DELTA = 1.1;
	private InfrastructureElementsPane elementPane;
	private TrainPane trainPane;
	private SwarmPane swarmPane;
	private double pressedX, pressedY;

	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.trainPane = new TrainPane();
		this.swarmPane = new SwarmPane();
		this.stackPane.getChildren().add(this.elementPane);
		this.stackPane.getChildren().add(this.trainPane);
		this.stackPane.getChildren().add(this.swarmPane);
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		Collection<InfrastructureElement> elements = serviceUtility
				.getInfrastructureElementService().findElements();

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
		}

		CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY,
				minY);

		this.elementPane.setCoordinateMapper(mapper);
		this.elementPane.setElements(elements);
		this.trainPane.setCoordinateMapper(mapper);
		this.swarmPane.setCoordinateMapper(mapper);
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
		NodeGestures swarmNodeGestures = new NodeGestures(swarmPane);
		
		stackPane.addEventFilter( ScrollEvent.ANY, elemNodeGestures.getOnScrollEventHandler());
		stackPane.addEventFilter( ScrollEvent.ANY, trainNodeGestures.getOnScrollEventHandler());
		stackPane.addEventFilter( ScrollEvent.ANY, swarmNodeGestures.getOnScrollEventHandler());
	}

	public void updateTrainCoordinates(Map<AbstractTrainSimulator, List<Coordinate>> map,
			Time time) {
		this.trainPane.updateTrainLocations(map, time);
	}
	
	public void updateSwarms(Map<AbstractTrainSimulator, List<Coordinate>> map, Collection<Swarm> swarms) {
		this.swarmPane.updateSwarms(map, swarms);
	}
}
	

