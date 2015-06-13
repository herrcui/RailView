package railview.infrastructure.editor;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
            
            NetworkPaneController networkPaneController = loader.getController();
            networkPaneController.setInfrastructureServiceUtility(serviceUtility);
            
            this.networkPaneRoot.getChildren().add(networkPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
		this.networkPaneController.setStage(stage);
	}	
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility serviceUtility) {
	   this.serviceUtility = serviceUtility;
	}
	
	private NetworkPaneController networkPaneController;
	private IInfrastructureServiceUtility serviceUtility;
	private Stage stage;
}
