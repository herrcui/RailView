package railview.simulation;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SingleSimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;
import railview.railsys.data.RailsysData;
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
		/*
		infraServiceUtility = InfrastructureReader.getRailSys7Instance(RailsysData.class.getResource("\\var-2011")).initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();

		// Rollilngstock
		rollingStockServiceUtility = RollingStockReader.getInstance().initialize();

		// Timetable
		timeTableServiceUtility = TimetableReader.getInstance(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();

		simulator = SingleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility);
		 */
		launch(args);
	}

	private void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = SimulationController.class
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

	private static SingleSimulationManager simulator;
	private static IInfrastructureServiceUtility infraServiceUtility;
	private static IRollingStockServiceUtility rollingStockServiceUtility;
	private static ITimetableServiceUtility timeTableServiceUtility;
}
