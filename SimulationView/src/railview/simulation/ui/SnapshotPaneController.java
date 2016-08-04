package railview.simulation.ui;

import java.util.Collection;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.container.ColorPicker;
import railview.infrastructure.container.CoordinateMapper;
import railview.infrastructure.container.InfrastructureElementsPane;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;


public class SnapshotPaneController {
	
	@FXML
	private StackPane stackPane;
	
	private InfrastructureElementsPane elementPane;
	
	
	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.stackPane.getChildren().add(this.elementPane);

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
		this.elementPane.setElements(elements, new ColorPicker(Color.WHITE));
	}

}
