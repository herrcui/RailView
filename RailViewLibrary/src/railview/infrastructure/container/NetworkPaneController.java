package railview.infrastructure.container;

import java.util.Collection;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.exception.NullIdException;
import railapp.infrastructure.service.IInfrastructureServiceUtility;

public class NetworkPaneController {
	@FXML
    private StackPane stackPane;
	@FXML
    private Label infraLabel;
	
	private double maxX, minX, maxY, minY;
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {
		this.serviceUtility = serviceUtility;
		try {
			Collection<InfrastructureElement> elements = 
					this.serviceUtility.getInfrastructureElementService().findElements();
			this.maxX = Double.MIN_VALUE;
			this.minX = Double.MAX_VALUE;
			this.maxY = Double.MIN_VALUE;
			this.minY = Double.MAX_VALUE;
			for (InfrastructureElement element : elements) {
				for (Port port : element.getPorts()) {
					if (port.getCoordinate().getX() > this.maxX) this.maxX = port.getCoordinate().getX();
					if (port.getCoordinate().getX() < this.minX) this.minX = port.getCoordinate().getX();
					if (port.getCoordinate().getY() > this.maxY) this.maxY = port.getCoordinate().getY();
					if (port.getCoordinate().getY() < this.minY) this.minY = port.getCoordinate().getY();
				}
			}

			this.drawInfrastructureElement(elements);
		} catch (NullIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML
    private void initialize() {
		
	}
	
	private void drawInfrastructureElement(Collection<InfrastructureElement> elements) {
		this.stackPane.setStyle("-fx-background-color: yellow;");
		
		Group root = new Group();
		Scene scene = new Scene(root, this.stackPane.getWidth(), this.stackPane.getHeight());
		
		for (InfrastructureElement element : elements) {
			this.drawInfrastructureElement(element);
		}
		
		stage.setScene(scene);
        stage.show();
		
		this.infraLabel.setText("number of elements: " + elements.size());
	}
	
	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			Line line = new Line();
			line.setStartX(mapToPaneX(element.findPort(1).getCoordinate().getX()));
			line.setStartY(mapToPaneX(element.findPort(1).getCoordinate().getY()));
			line.setEndX(mapToPaneY(element.findPort(2).getCoordinate().getX()));
			line.setEndY(mapToPaneY(element.findPort(2).getCoordinate().getY()));
			this.stackPane.getChildren().add(line);
		}
	}
	
	private float mapToPaneX(double x) {
		return (float) (this.stackPane.getWidth() * x / (this.maxX - this.minX));
	}
	
	private float mapToPaneY(double y) {
		return (float) (this.stackPane.getHeight() * y / (this.maxY - this.minY));
	}
	
	private IInfrastructureServiceUtility serviceUtility;
	private Stage stage;
}
