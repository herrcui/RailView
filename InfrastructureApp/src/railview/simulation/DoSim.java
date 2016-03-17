package railview.simulation;

import java.io.IOException;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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

	private void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = SimulationViewerController.class
					.getResource("SimulationViewer.fxml");
			loader.setLocation(location);
			this.rootLayout = (AnchorPane) loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();
		}
	}

	private Stage primaryStage;
	private AnchorPane rootLayout;
}
