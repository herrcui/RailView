package railview.infrastructure;

import java.io.IOException;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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
		try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Root.class.getResource("InfrastructureElementOverview.fxml"));
            this.rootLayout = (BorderPane) loader.load();
            
            InfrastructureElementViewController controller = loader.getController();
			controller.setInfrastructureServiceUtility(serviceUtility);
			
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private Stage primaryStage;
    private BorderPane rootLayout;
    
    private static IInfrastructureServiceUtility serviceUtility;
}
