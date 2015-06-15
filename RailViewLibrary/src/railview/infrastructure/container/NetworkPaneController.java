package railview.infrastructure.container;

import java.util.Collection;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.exception.NullIdException;
import railapp.infrastructure.service.IInfrastructureServiceUtility;

public class NetworkPaneController {
	@FXML
    private StackPane stackPane;
	@FXML
    private Label infraLabel;
	
	private InfrastructureElementsCanvas elementCanvas;
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {
		try {
			Collection<InfrastructureElement> elements = 
					serviceUtility.getInfrastructureElementService().findElements();

			double maxX = Double.MIN_VALUE;
			double minX = Double.MAX_VALUE;
			double maxY = Double.MIN_VALUE;
			double minY = Double.MAX_VALUE;
			
			for (InfrastructureElement element : elements) {
				for (Port port : element.getPorts()) {
					if (port.getCoordinate().getX() > maxX) maxX = port.getCoordinate().getX();
					if (port.getCoordinate().getX() < minX) minX = port.getCoordinate().getX();
					if (port.getCoordinate().getY() > maxY) maxY = port.getCoordinate().getY();
					if (port.getCoordinate().getY() < minY) minY = port.getCoordinate().getY();
				}
			}
			
			CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
			
			this.elementCanvas.setCoordinateMapper(mapper);
			this.elementCanvas.setElements(elements);
			
		} catch (NullIdException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
    private void initialize() {
		this.elementCanvas = new InfrastructureElementsCanvas();
		this.stackPane.getChildren().add(this.elementCanvas);
		this.elementCanvas.widthProperty().bind(this.stackPane.widthProperty());
		this.elementCanvas.heightProperty().bind(this.stackPane.heightProperty());
	}
}
