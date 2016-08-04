package railview.simulation.ui;

import java.util.Collection;
import java.util.List;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.element.dto.Turnout;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.logs.InfrastructureOccupancyAndPendingLogger;
import railapp.units.Coordinate;
import railview.infrastructure.container.ColorPicker;
import railview.infrastructure.container.CoordinateMapper;
import railview.infrastructure.container.InfrastructureElementsPane;
import railview.infrastructure.container.NodeGestures;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
public class OccupancyAndPendingPaneController {
	@FXML
	private StackPane infraRoot;
	
	private InfrastructureElementsPane elementPane;
	
	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.infraRoot.getChildren().add(this.elementPane);
	}
	
	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility infraServiceUtility) {
		Collection<InfrastructureElement> elements = infraServiceUtility
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
		this.draw(elements, mapper);
	}
	
	@FXML
	private void mouseEnter(){
		infraRoot.setOnMousePressed(new EventHandler<MouseEvent>()
		        {
            public void handle(MouseEvent event)
            {
                pressedX = event.getX();
                pressedY = event.getY();
            }
        });

		infraRoot.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
            	infraRoot.setTranslateX(infraRoot.getTranslateX() + event.getX() - pressedX);
            	infraRoot.setTranslateY(infraRoot.getTranslateY() + event.getY() - pressedY);

                event.consume();
            }
        });
	}
	
	@FXML
	private void scrollWheel(){
		NodeGestures elemNodeGestures = new NodeGestures(elementPane);
		
		infraRoot.addEventFilter( ScrollEvent.ANY, elemNodeGestures.getOnScrollEventHandler());
	}
	
	private void draw(Collection<InfrastructureElement> elements, CoordinateMapper mapper) {
		if (elements == null)
			return;

		this.elementPane.getChildren().clear();

		for (InfrastructureElement element : elements) {
			this.drawInfrastructureElement(element, mapper);
		}
	}
	
	private void drawInfrastructureElement(InfrastructureElement element, CoordinateMapper mapper) {
		if (element instanceof Track) {
			List<Coordinate> coordinates = ((Track) element)
					.getCoordinateAtLink12();
			this.drawLink(coordinates, mapper);
		} else {
			if (element instanceof Turnout) {
				List<Coordinate> coordinates = element.findLink(1, 2)
						.getCoordinates();
				Coordinate middlePoint = Coordinate.fromXY(0.5 * (coordinates
						.get(0).getX() + coordinates.get(1).getX()),
						0.5 * (coordinates.get(0).getY() + coordinates.get(1)
								.getY()));
				this.drawLink(coordinates, mapper);
				coordinates = element.findLink(1, 3).getCoordinates();
				coordinates.add(1, middlePoint);
				this.drawLink(coordinates, mapper);
			} else {
				List<Coordinate> coordinates = element.findLink(1, 3)
						.getCoordinates();
				this.drawLink(coordinates, mapper);
				coordinates = element.findLink(2, 4).getCoordinates();
				this.drawLink(coordinates, mapper);
			}
		}
	}

	private void drawLink(List<Coordinate> coordinates, CoordinateMapper mapper) {
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Line line = new Line();

			line.setStartX(mapper.mapToPaneX(coordinates.get(i).getX(), this.elementPane));
			line.setStartY(mapper.mapToPaneY(coordinates.get(i).getY(), this.elementPane));
			line.setEndX(mapper.mapToPaneX(coordinates.get(i + 1).getX(), this.elementPane));
			line.setEndY(mapper.mapToPaneY(coordinates.get(i + 1).getY(), this.elementPane));

			line.setStroke(Color.RED);
			line.setStrokeWidth(0.1);

			this.elementPane.getChildren().add(line);
		}
	}

	private double pressedX, pressedY;
	private InfrastructureOccupancyAndPendingLogger logger;
}
