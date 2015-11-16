package railview.infrastructure.container;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {
	
	@FXML
	private AnchorPane firstLayer;
	
	@FXML
	private AnchorPane secondLayer;
	
	@FXML
	public void initialize() {
		secondLayer.setVisible(false);
		
	}
}
