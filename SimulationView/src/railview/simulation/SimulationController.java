package railview.simulation;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.units.Duration;
import railapp.units.Time;
import railview.infrastructure.container.NetworkPaneController;

public class SimulationController {
	@FXML
	private AnchorPane networkPaneRoot;
	
	@FXML
	private Label timeLabel;
	
	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			StackPane networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			this.networkPaneRoot.getChildren().add(networkPane);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void startSimulation() {
		if (this.simulator != null) {
			new Thread(this.simulator).start();
			
			Thread t = new Thread(() -> {
				(new SimulationUpdater()).periodicalUpdate(); 
			});			
			t.setDaemon(true);
			t.start();
		}
	}
	
	@FXML
	public void replaySimulation() {
		if (this.simulator != null) {
			Thread t = new Thread(() -> {
				(new SimulationUpdater()).periodicalUpdate(); 
			});			
			t.setDaemon(true);
			t.start();
		}
	}
	
	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}
	
	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		this.networkPaneController
				.setInfrastructureServiceUtility(serviceUtility);
	}
	
	public void setSimulationManager(SimulationManager simulator) {
		this.simulator = simulator;
	}
	
	private NetworkPaneController networkPaneController;
	private SimulationManager simulator;
	private Duration updateInterval = Duration.fromSecond(60);
	private int UIPause = 100;
	
	class SimulationUpdater {
		private Time time = Time.getInstance(0, 0, 0);

		void periodicalUpdate() {
			while (time.compareTo(Time.getInstance(1, 23, 59, 59, 0)) < 0) {

				Platform.runLater(new Runnable() {
					@Override public void run() {
						timeLabel.setText("Simulation Time: " + time.toString());
						networkPaneController.updateTrainCoordinates(
								simulator.getTrainCoordinates(time), time);
					}
				});
				
				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				time = time.add(updateInterval);
				if (time.compareTo(simulator.getTime()) > 0) {
					time = simulator.getTime();
				}
			}
		}
	}
}
