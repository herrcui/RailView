package railview.simulation;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.dto.Network;
import railapp.simulation.SingleSimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railview.controller.framework.AbstractSimulationController;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;
import railview.simulation.ui.ConfigurationPaneController;
import railview.simulation.ui.EditorPaneController;
import railview.simulation.ui.GraphPaneController;
import railview.simulation.ui.DialogPaneController;
import railview.infrastructure.container.NetworkPaneController;

/**
 * The controller class for the main user interface (SimulationViewer.fxml)
 * which appears, when you start the program.
 *
 */

public class SimulationViewerController extends AbstractSimulationController {

	@FXML
	private AnchorPane networkPaneRoot, rootPane, menuPane, symbolPane;

	@FXML
	private Label timeLabel, activeLabel, terminatedLabel;

	@FXML
	private Button startButton, pauseButton, stopButton, graphButton,
			networkButton, editorButton, settingButton, lockButton,
			unlockButton, openOne, openTwo, openThree;

	@FXML
	private Slider speedBar;

	/**
	 * initialize the class and load the networkPane, graphPane and editorPane.
	 * Listener for window resizing.
	 */
	@FXML
	public void initialize() {
		try {
			FXMLLoader networkpaneloader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			networkpaneloader.setLocation(location);
			networkPane = (StackPane) networkpaneloader.load();
			this.networkPaneController = networkpaneloader.getController();

			FXMLLoader graphpaneloader = new FXMLLoader();
			URL graphpanelocation = GraphPaneController.class
					.getResource("GraphPane.fxml");
			graphpaneloader.setLocation(graphpanelocation);
			graphPane = (AnchorPane) graphpaneloader.load();
			this.graphPaneController = graphpaneloader.getController();

			FXMLLoader editorpaneloader = new FXMLLoader();
			URL editorpanelocation = EditorPaneController.class
					.getResource("EditorPane.fxml");
			editorpaneloader.setLocation(editorpanelocation);
			editorPane = (AnchorPane) editorpaneloader.load();

			this.networkPaneRoot.getChildren().addAll(networkPane, graphPane,
					editorPane);

			networkPaneRoot.widthProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observableValue,
								Number oldSceneWidth, Number newSceneWidth) {
							symbolPane.setLayoutX((newSceneWidth.doubleValue() - symbolPane
									.getPrefWidth()) / 2);
							networkPane.setLayoutX((newSceneWidth.doubleValue() / 2)
									- (networkPane.prefWidth(-1) / 2));
							graphPane.setPrefWidth(newSceneWidth.doubleValue());
							editorPane.setPrefWidth(newSceneWidth.doubleValue());
						}
					});

			networkPaneRoot.heightProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observableValue,
								Number oldSceneHeight, Number newSceneHeight) {
							networkPane.setLayoutY((newSceneHeight
									.doubleValue() / 2)
									- (networkPane.prefHeight(-1) / 2));
							graphPane.setPrefHeight(newSceneHeight
									.doubleValue());
							editorPane.setPrefHeight(newSceneHeight
									.doubleValue());
						}
					});

			graphPane.setVisible(false);
			editorPane.setVisible(false);
			symbolPane.setOpacity(0.0);
			menuPane.setOpacity(1.0);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start the simulation and disable the startButton
	 */
	@FXML
	public void startSimulation() {
		super.startSimulation();

		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);

		while (true) {
			if (simulator.getInfrastructureSimulator() != null) {
				this.graphPaneController
						.setInfrastructureOccupancyAndPendingLogger(simulator
								.getInfrastructureSimulator()
								.getOccupancyAndPendingLogger());
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * enable all buttons but the pauseButton
	 */
	@FXML
	public void pauseSimulation() {
		super.pauseSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(false);
	}

	/**
	 * disable all Buttons but the startButton
	 */
	@FXML
	public void stopSimulation() {
		super.stopSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);
	}

	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}

	public void shutdown() {
		super.stopSimulation();
	}

	@FXML
	private void lockMenuPane() {

		menuPane.setOnMouseEntered(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				menuPane.setOpacity(1.0);
			}
		});
		menuPane.setOnMouseExited(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				menuPane.setOpacity(1.0);
			}
		});
		lockButton.setVisible(false);
		unlockButton.setVisible(true);
	}

	@FXML
	private void unlockMenuPane() {

		menuPane.setOnMouseEntered(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				FadeTransition fadeTransition1 = new FadeTransition(
						javafx.util.Duration.millis(500), menuPane);
				fadeTransition1.setFromValue(0);
				fadeTransition1.setToValue(1.0);
				fadeTransition1.play();
			}
		});

		menuPane.setOnMouseExited(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				FadeTransition fadeTransition2 = new FadeTransition(
						javafx.util.Duration.millis(500), menuPane);
				fadeTransition2.setFromValue(1.0);
				fadeTransition2.setToValue(0.0);
				fadeTransition2.play();
			}
		});

		unlockButton.setVisible(false);
		lockButton.setVisible(true);
	}

	@FXML
	private void appearSymbolPaneWhenHover() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
	}

	@FXML
	private void fadeSymbolPaneWhenLeaving() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

	}

	@FXML
	private void enterGraphPane() {
		editorPane.setVisible(false);
		graphPane.setVisible(true);
		networkPane.setVisible(false);
		menuPane.setVisible(false);

		graphPaneController.setActive(true);
		networkPaneController.setActive(false);
	}

	@FXML
	private void enterNetworkPane() {
		editorPane.setVisible(false);
		graphPane.setVisible(false);
		networkPane.setVisible(true);
		menuPane.setVisible(true);

		graphPaneController.setActive(false);
		networkPaneController.setActive(true);
	}

	@FXML
	private void enterEditorPane() {
		editorPane.setVisible(true);
		graphPane.setVisible(false);
		networkPane.setVisible(false);
		menuPane.setVisible(false);

		graphPaneController.setActive(false);
		networkPaneController.setActive(false);
	}

	@FXML
	private void onSettingButtonAction(ActionEvent event) {
		configController.setMaximized(true);
		configController.showAndWait();
	}

	/**
	 * method for loading the data (infrastructure, rollingstock, timetable)
	 * 
	 * @param event
	 */
	@FXML
	private void onLoadButtonAction(ActionEvent event) {
		DialogPaneController pathDialog = new DialogPaneController(null);
		pathDialog.showAndWait();
		this.initiateRailSys7Simulator(pathDialog.getInfrastructurePath(),
				pathDialog.getRollingStockPath(), pathDialog.getTimeTablePath());

		// TODO: check if it is successful
		this.networkPaneController
				.setInfrastructureServiceUtility(this.infraServiceUtility);

		this.graphPaneController
				.setInfrastructureServiceUtility(this.infraServiceUtility);

		this.graphPaneController.updateTrainMap(simulator.getTrainSimulators());
	}

	/**
	 * shows updates of the data on the status bar
	 */
	private void updateStatusBar() {
		timeLabel.setText("Simulation Time: " + this.updateTime.toString());

		int numActive = 0;
		int numTerminate = 0;

		if (simulator.getStatus() != SingleSimulationManager.INACTIVE) {
			for (EventListener listener : simulator.getListeners()) {
				if (listener instanceof AbstractTrainSimulator) {
					AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

					if (trainSimulator.getTerminateTime() != null) {
						if (trainSimulator.getTerminateTime().compareTo(
								this.updateTime) < 0) {
							numTerminate++;
						} else {
							if (trainSimulator.getActiveTime().compareTo(
									this.updateTime) < 0) {
								numActive++;
							}
						}
					} else {
						if (trainSimulator.getActiveTime() != null
								&& trainSimulator.getActiveTime().compareTo(
										this.updateTime) < 0) {
							numActive++;
						}
					}
				}
			}
		} // if (simulator.getStatus() != SimulationManager.INACTIVE)

		activeLabel.setText("Active Trains: " + numActive);
		terminatedLabel.setText("Terminated Trains: " + numTerminate);
	}

	/**
	 * initializes the simulator
	 * 
	 * @param infraPath
	 * @param rollingstockPath
	 * @param timetablePath
	 */
	private void initiateRailSys7Simulator(Path infraPath,
			Path rollingstockPath, Path timetablePath) {
		infraServiceUtility = InfrastructureReader.getRailSys7Instance(
				infraPath).initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks()
				.iterator().next();

		// Rollilngstock
		rollingStockServiceUtility = RollingStockReader.getRailSys7Instance(
				rollingstockPath).initialize();

		// Timetable
		timeTableServiceUtility = TimetableReader.getRailSys7Instance(
				timetablePath, infraServiceUtility, rollingStockServiceUtility,
				network).initialize();

		simulator = SingleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility, timeTableServiceUtility);

		SwarmManager swarmManager = SwarmManager.getInstance(simulator);
		this.networkPaneController.setSwarmManager(swarmManager);

		this.configController.setSimulator(simulator);
	}

	@Override
	protected void updateUI() {
		Map<AbstractTrainSimulator, List<Coordinate>> coordinates = simulator
				.getTrainCoordinates(this.updateTime);

		if (coordinates != null) {
			networkPaneController.updateTrainCoordinates(coordinates,
					this.updateTime);

			this.graphPaneController.updateTrainMap(this.simulator
					.getTrainSimulators());

			updateStatusBar();
		}
	}

	@Override
	protected void setTime(boolean isReplay) {
		Duration updateInterval = Duration.fromTotalMilliSecond(UIPause);
		if (simulator.getStatus() != SingleSimulationManager.INACTIVE) {
			updateInterval = Duration.fromTotalMilliSecond(MAXSpeed
					* speedBar.getValue() / 100);
			if (speedBar.getValue() == speedBar.getMin()) {
				updateInterval = Duration.fromTotalMilliSecond(UIPause);
			}

			if (speedBar.getValue() == speedBar.getMax()
					&& simulator.getTime() != null
					&& simulator.getStatus() != SingleSimulationManager.TERMINATED) {

				updateInterval = simulator.getTime().getDifference(updateTime);
			}
		}
		if (isReplay) {
			this.updateTime = this.updateTime.add(updateInterval);
		} else {
			if (simulator.getStatus() == SingleSimulationManager.RUNNING) { // not
																			// terminated
																			// yet
				this.updateTime = this.updateTime.add(updateInterval);
				if (this.updateTime.compareTo(simulator.getTime()) > 0) {
					this.updateTime = simulator.getTime(); // if update too
															// fast, slow down
				}
			} else {
				this.updateTime = this.updateTime.add(updateInterval);
			}
		}
	}

	private StackPane networkPane;
	private AnchorPane graphPane;
	private AnchorPane editorPane;
	private NetworkPaneController networkPaneController;
	private GraphPaneController graphPaneController;
	private ConfigurationPaneController configController = new ConfigurationPaneController();
	private int UIPause = 100;
	private int MAXSpeed = 20000; // 1 : 200
}
