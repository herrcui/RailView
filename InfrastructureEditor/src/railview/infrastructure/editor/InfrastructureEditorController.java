package railview.infrastructure.editor;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.container.NetworkPaneController;

public class InfrastructureEditorController {	
	@FXML
    private AnchorPane networkPaneRoot;
	
	@FXML
	public void initialize() {
		try {
            FXMLLoader loader = new FXMLLoader();
            URL location = NetworkPaneController.class.getResource("NetworkPane.fxml");
            loader.setLocation(location);
            StackPane networkPane = (StackPane) loader.load();
            
            this.networkPaneController = loader.getController();
            this.networkPaneRoot.getChildren().add(networkPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {
	   this.networkPaneController.setInfrastructureServiceUtility(serviceUtility);
	}
	
	private NetworkPaneController networkPaneController;
}
