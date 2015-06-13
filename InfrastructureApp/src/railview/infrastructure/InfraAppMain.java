package railview.infrastructure;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.editor.InfrastructureEditorController;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class InfraAppMain extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Example of Infrastructure Elements");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		serviceUtility = InfrastructureReader.getInstance().initialize();
		launch(args);
	}
	
	private void initRootLayout() {
		//this.initInfrastructureEditor();
		this.initInfrastructureView();
		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}
	
	private void initInfrastructureView() {
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            URL location = InfrastructureElementViewController.class.getResource("InfrastructureElementOverview.fxml");
            loader.setLocation(location);
            this.rootLayout = (AnchorPane) loader.load();
            
            InfrastructureElementViewController controller = loader.getController();
            controller.setInfrastructureServiceUtility(serviceUtility);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void initInfrastructureEditor() {
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            URL location = InfrastructureEditorController.class.getResource("InfrastructureEditor.fxml");
            loader.setLocation(location);
            this.rootLayout = (AnchorPane) loader.load();
            
            InfrastructureEditorController controller = loader.getController();
            controller.setInfrastructureServiceUtility(serviceUtility);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private Stage primaryStage;
    private AnchorPane rootLayout;
    
    private static IInfrastructureServiceUtility serviceUtility;
}
