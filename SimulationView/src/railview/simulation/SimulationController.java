package railview.simulation;

import java.io.IOException;
import java.net.URL;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railview.controller.framework.AbstractSimulationController;
import railview.infrastructure.container.NetworkPaneController;

public class SimulationController extends AbstractSimulationController {
	@FXML
	private AnchorPane networkPaneRoot;

	@FXML
	private Label timeLabel;

	@FXML
	private Label activeLabel;

	@FXML
	private Label terminatedLabel;

	@FXML
	private AnchorPane menuPane;

	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			StackPane networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			AnchorPane.setLeftAnchor(networkPane, (this.networkPaneRoot.prefWidth(-1)/2)-(networkPane.prefWidth(-1)/2));
			AnchorPane.setTopAnchor(networkPane,(this.networkPaneRoot.prefHeight(-1)/2)-(networkPane.prefHeight(-1)/2));
			this.networkPaneRoot.getChildren().add(networkPane);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void fadeRoot() {
	                FadeTransition fadeTransition
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setToValue(0.0);
	                fadeTransition.play();
	            }


	@FXML
	private void appear() {
	                FadeTransition fadeTransition
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setFromValue(0.0);
	                fadeTransition.setToValue(1.0);
	                fadeTransition.play();
	            }


	@FXML
	private void fadeMenu() {

	                FadeTransition fadeTransition
	                        = new FadeTransition(javafx.util.Duration.millis(500), menuPane);
	                fadeTransition.setFromValue(1.0);
	                fadeTransition.setToValue(0.0);
	                fadeTransition.play();

				}

	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}

	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility serviceUtility) {
		this.networkPaneController
				.setInfrastructureServiceUtility(serviceUtility);
	}

	@Override
	protected void updateUI() {
		networkPaneController.updateTrainCoordinates(
				simulator.getTrainCoordinates(this.updateTime), this.updateTime);
		updateStatusBar();
	}

	private void updateStatusBar() {
		timeLabel.setText("Simulation Time: " + this.updateTime.toString());

		int numActive = 0;
		int numTerminate = 0;

		if (simulator.getStatus() != SimulationManager.INACTIVE) {
			for (EventListener listener : simulator.getListeners()) {
				if (listener instanceof AbstractTrainSimulator) {
					AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

					if (trainSimulator.getTerminateTime() != null) {
						if (trainSimulator.getTerminateTime().compareTo(this.updateTime) < 0) {
							numTerminate++;
						} else {
							if (trainSimulator.getActiveTime().compareTo(this.updateTime) < 0) {
								numActive++;
							}
						}
					} else {
						if (trainSimulator.getActiveTime() != null &&
							trainSimulator.getActiveTime().compareTo(this.updateTime) < 0) {
							numActive++;
						}
					}
				}
			}
		} // if (simulator.getStatus() != SimulationManager.INACTIVE)

		activeLabel.setText("Active Trains: " + numActive);
		terminatedLabel.setText("Terminated Trains: " + numTerminate);
	}

	@Override
	protected void setTime(boolean isReplay) {
		if (isReplay) {
			this.updateTime = this.updateTime.add(updateInterval);
		} else {
			if (simulator.getStatus() == SimulationManager.RUNNING) { // not terminated yet
				this.updateTime = this.updateTime.add(updateInterval);
				if (this.updateTime.compareTo(simulator.getTime()) > 0) {
					this.updateTime = simulator.getTime(); // if update too fast, slow down
				}
			} else {
				this.updateTime = this.updateTime.add(updateInterval);
			}
		}
	}

	private NetworkPaneController networkPaneController;
	private SimulationManager simulator;
	private Duration updateInterval = Duration.fromSecond(60);
}
