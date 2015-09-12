package railview.swarmintelligence;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.swarmintelligence.SwarmManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;
import railview.simulation.SimulationController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class SwarmApplication extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Swarm Viewer");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		infraServiceUtility = InfrastructureReader.getInstance().initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();
		
		// Rollilngstock
		rollingStockServiceUtility = RollingStockReader.getInstance().initialize();
		
		// Timetable
		timeTableServiceUtility = TimetableReader.getInstance(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();
		
		simulator = SimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility);
		
		swarmManager = SwarmManager.getInstance(simulator); 
		
		launch(args);
	}
	
	private void initRootLayout() {
		SwarmViewerController controller = this.initializeSwarmViewerController();
		
		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			controller.setInfrastructureServiceUtility(infraServiceUtility);
			controller.setSimulationManager(simulator);
			controller.setSwarmManager(swarmManager);
			
			primaryStage.show();
			
		    final double initWidth  = scene.getWidth();
		    final double initHeight = scene.getHeight();
		    Scale scale = new Scale();
		    scale.xProperty().bind(scene.widthProperty().divide(initWidth));
		    scale.yProperty().bind(scene.heightProperty().divide(initHeight));
		    scale.setPivotX(0); scale.setPivotY(0);
		    rootLayout.getTransforms().addAll(scale);
		}
	}
	
	private SwarmViewerController initializeSwarmViewerController() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = SwarmViewerController.class
					.getResource("SwarmViewer.fxml");
			loader.setLocation(location);

			this.rootLayout = (AnchorPane) loader.load();

			SwarmViewerController controller = loader.getController();
			return controller;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Stage primaryStage;
	private AnchorPane rootLayout;

	private static SimulationManager simulator;
	private static SwarmManager swarmManager;
	private static IInfrastructureServiceUtility infraServiceUtility;
	private static IRollingStockServiceUtility rollingStockServiceUtility;
	private static ITimetableServiceUtility timeTableServiceUtility;
}
