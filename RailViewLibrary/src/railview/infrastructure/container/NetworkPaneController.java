package railview.infrastructure.container;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;

public class NetworkPaneController {
	private static final double MAX_SCALE = 10.0d;
	private static final double MIN_SCALE = .1d;
	@FXML
	private StackPane anchorPane;
	@FXML
	private Label infraLabel;

	final double SCALE_DELTA = 1.1;
	private InfrastructureElementsPane elementPane;
	private TrainPane trainPane;
	private Test_SecondLayer secondLayer;
	private Test_JavaFXanimations animation;
	private double pressedX, pressedY;

	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.trainPane = new TrainPane();
		this.secondLayer = new Test_SecondLayer();
		this.animation = new Test_JavaFXanimations();
		this.anchorPane.getChildren().add(this.secondLayer);
		this.anchorPane.getChildren().add(this.animation);
	//	this.anchorPane.getChildren().add(this.elementPane);
		this.anchorPane.getChildren().add(this.trainPane);

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
		this.secondLayer.setCoordinateMapper(mapper);
		this.trainPane.setCoordinateMapper(mapper);
	}
/**
	@FXML
	private void mousePress() {
		NodeGestures nodeGestures = new NodeGestures(elementPane);

		anchorPane.addEventFilter(MouseEvent.MOUSE_PRESSED,
				nodeGestures.getOnMousePressedEventHandler());
		anchorPane.addEventFilter(MouseEvent.MOUSE_DRAGGED,
				nodeGestures.getOnMouseDraggedEventHandler());
	}
	**/
	@FXML
	private void mouseclick(){
		anchorPane.setOnMousePressed(new EventHandler<MouseEvent>()
		        {
            public void handle(MouseEvent event)
            {
                pressedX = event.getX();
                pressedY = event.getY();
            }
        });

        anchorPane.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
            	anchorPane.setTranslateX(anchorPane.getTranslateX() + event.getX() - pressedX);
            	anchorPane.setTranslateY(anchorPane.getTranslateY() + event.getY() - pressedY);

                event.consume();
            }
        });
	}
	
	@FXML
	private void ScrollWheel(){
		NodeGestures nodeGestures = new NodeGestures(elementPane);
		NodeGestures nodeGestures2 = new NodeGestures(secondLayer);
		NodeGestures nodeGestures3 = new NodeGestures(animation);
		anchorPane.addEventFilter( ScrollEvent.ANY, nodeGestures.getOnScrollEventHandler());
		anchorPane.addEventFilter( ScrollEvent.ANY, nodeGestures2.getOnScrollEventHandler());
		anchorPane.addEventFilter( ScrollEvent.ANY, nodeGestures3.getOnScrollEventHandler());

	
/**	  @FXML private void ScrollEvent(){ anchorPane.setOnScroll(new
	        EventHandler<ScrollEvent>() {
	  @Override public void handle(ScrollEvent event) { event.consume();
	
	           if (event.getDeltaY() == 0) { return; }
	 
	           double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA : 1
	           / SCALE_DELTA;
	 
	           anchorPane.setScaleX(anchorPane.getScaleX() * scaleFactor);
	           anchorPane.setScaleY(anchorPane.getScaleY() * scaleFactor);
	 
	           } }); }
**/	
	}

	public void updateTrainCoordinates(Map<AbstractTrainSimulator, List<Coordinate>> map) {
		this.trainPane.setCoordinateMap(map);
	}
}
	

