package railview.simulation.ui;

import java.util.Collection;
import java.util.List;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.units.Coordinate;
import railview.infrastructure.container.CoordinateMapper;
import railview.infrastructure.container.InfrastructureElementsPane;
import railview.infrastructure.container.NodeGestures;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * The controller class for SnapshotPane.fxml. The Pane shows a miniature copy of
 * the simulation in the TrainRunMonitorPane.
 */
public class SnapshotPaneController {

	@FXML
	private StackPane stackPane;

	private InfrastructureElementsPane elementPane;

	/**
	 * Add the elementPane on top of the snapshotPane
	 */
	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.stackPane.getChildren().add(this.elementPane);
	}

	public void setHighlightedPath(List<Coordinate> path) {
		this.elementPane.setHighlightedPath(path);
	}

	public void setEventPoint(Coordinate coordinate) {
		this.elementPane.setEventPoint(coordinate);
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

		CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);

		this.elementPane.setCoordinateMapper(mapper);
		this.elementPane.setAndDrawElements(elements, Color.WHITE);
	}

	/**
	 * use the draw method from infrastructureElementPane
	 */
	public void draw() {
		this.elementPane.draw();
	}

	@FXML
	private void mouseEnter() {
		stackPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				pressedX = event.getX();
				pressedY = event.getY();
			}
		});

		stackPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				stackPane.setTranslateX(stackPane.getTranslateX()
						+ event.getX() - pressedX);
				stackPane.setTranslateY(stackPane.getTranslateY()
						+ event.getY() - pressedY);

				event.consume();
			}
		});
	}

	@FXML
	private void scrollWheel() {
		NodeGestures elemNodeGestures = new NodeGestures(elementPane);

		stackPane.addEventFilter(ScrollEvent.ANY,
				elemNodeGestures.getOnScrollEventHandler());
	}

	private double pressedX, pressedY;
}
