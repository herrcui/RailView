package railview.simulation;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


/**
 * main class of the whole program
 *
 */

public class DoSim extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("DoSim");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		launch(args);
	}

	// initializes the rootLayout by calling the class SimulationController and SimulationViewer.fxml
	private void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = SimulationViewerController.class
					.getResource("SimulationViewer.fxml");
			loader.setLocation(location);
			this.rootLayout = (AnchorPane) loader.load();
			
			this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent> () {
				@Override
				public void handle(WindowEvent event) {
					SimulationViewerController controller = 
							loader.<SimulationViewerController>getController();
					controller.shutdown();
				}
			});
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);			
			primaryStage.setScene(scene);
			
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();
		    primaryStage.setWidth(bounds.getWidth());
		    primaryStage.setHeight(bounds.getHeight());
		    primaryStage.setMaximized(true);

			primaryStage.show();
		}
	}

	private Stage primaryStage;
	private AnchorPane rootLayout;
}
