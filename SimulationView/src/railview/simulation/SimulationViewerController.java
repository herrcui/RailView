package railview.simulation;

import java.io.IOException;
import java.net.URL;
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
import railapp.simulation.SingleSimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railview.simulation.SimulationFactory.ISimulationUpdateUI;
import railview.simulation.graph.GraphPaneController;
import railview.simulation.network.NetworkPaneController;
import railview.simulation.pyui.PythonPaneController;
import railview.simulation.setting.SettingPaneController;

/**
 * The controller class for the main user interface (SimulationViewer.fxml)
 * which appears, when you start the program.
 *
 */

public class SimulationViewerController implements ISimulationUpdateUI {

	@FXML
	private AnchorPane simulationPane, controlPane, menuPane;

	@FXML
	private Label timeLabel, activeLabel, terminatedLabel;

	@FXML
	private Button startButton, pauseButton, stopButton, graphButton,
			networkButton, editorButton, settingButton, lockButton,
			unlockButton;

	@FXML
	private Slider speedBar;

	private SimulationFactory simulationFactory;

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

			FXMLLoader settingpaneloader = new FXMLLoader();
			URL settingpanelocation = SettingPaneController.class
					.getResource("SettingPane.fxml");
			settingpaneloader.setLocation(settingpanelocation);
			settingPane = (AnchorPane) settingpaneloader.load();
			this.settingPaneController = settingpaneloader.getController();

			FXMLLoader pythonpaneloader = new FXMLLoader();
			URL pythonpanelocation = PythonPaneController.class
					.getResource("PythonPane.fxml");
			pythonpaneloader.setLocation(pythonpanelocation);
			pythonPane = (AnchorPane) pythonpaneloader.load();

			this.simulationPane.getChildren().addAll(
					networkPane,
					graphPane,
					settingPane,
					pythonPane);

			simulationPane.widthProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observableValue,
								Number oldSceneWidth, Number newSceneWidth) {
							menuPane.setLayoutX((newSceneWidth.doubleValue() - menuPane
									.getPrefWidth()) / 2);
							networkPane.setLayoutX((newSceneWidth.doubleValue() / 2)
									- (networkPane.prefWidth(-1) / 2));
							graphPane.setPrefWidth(newSceneWidth.doubleValue());
							pythonPane.setPrefWidth(newSceneWidth.doubleValue());
							settingPane.setPrefWidth(newSceneWidth.doubleValue());
						}
					});

			simulationPane.heightProperty().addListener(
					new ChangeListener<Number>() {
						@Override
						public void changed(
								ObservableValue<? extends Number> observableValue,
								Number oldSceneHeight, Number newSceneHeight) {
							networkPane.setLayoutY(newSceneHeight.doubleValue()/2
									- networkPane.prefHeight(-1)/2);

							graphPane.setPrefHeight(newSceneHeight.doubleValue() - controlPane.getPrefHeight());
							pythonPane.setPrefHeight(newSceneHeight.doubleValue());
							settingPane.setPrefHeight(newSceneHeight.doubleValue());
						}
					});

			graphPane.setVisible(false);
			pythonPane.setVisible(false);
			settingPane.setVisible(false);

			menuPane.setOpacity(0.0);
			controlPane.setOpacity(1.0);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * start the simulation and disable the startButton
	 */
	@FXML
	public void startSimulation() {
		this.simulationFactory.startSimulation();

		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);

		while (true) {
			if (this.simulationFactory.getSimulator().getInfrastructureSimulator() != null) {
				this.graphPaneController
						.setInfrastructureOccupancyAndPendingLogger(this.simulationFactory.getSimulator()
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
		this.simulationFactory.pauseSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(false);
	}

	/**
	 * disable all Buttons but the startButton
	 */
	@FXML
	public void stopSimulation() {
		this.simulationFactory.stopSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);
	}

	public NetworkPaneController getNetworkPaneController() {
		return this.networkPaneController;
	}

	public void shutdown() {
		if (this.simulationFactory != null)
			this.simulationFactory.stopSimulation();
	}

	@FXML
	private void lockMenuPane() {

		controlPane.setOnMouseEntered(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				controlPane.setOpacity(1.0);
			}
		});
		controlPane.setOnMouseExited(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				controlPane.setOpacity(1.0);
			}
		});
		lockButton.setVisible(false);
		unlockButton.setVisible(true);
	}

	@FXML
	private void unlockMenuPane() {

		controlPane.setOnMouseEntered(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				FadeTransition fadeTransition1 = new FadeTransition(
						javafx.util.Duration.millis(500), controlPane);
				fadeTransition1.setFromValue(0);
				fadeTransition1.setToValue(1.0);
				fadeTransition1.play();
			}
		});

		controlPane.setOnMouseExited(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				FadeTransition fadeTransition2 = new FadeTransition(
						javafx.util.Duration.millis(500), controlPane);
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
				javafx.util.Duration.millis(500), menuPane);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
	}

	@FXML
	private void fadeSymbolPaneWhenLeaving() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), menuPane);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

	}

	@FXML
	private void enterGraphPane() {
		graphPane.setVisible(true);
		controlPane.setVisible(true);

		pythonPane.setVisible(false);
		networkPane.setVisible(false);
		settingPane.setVisible(false);

		//graphPaneController.setActive(true);
		//networkPaneController.setActive(false);
	}

	@FXML
	private void enterNetworkPane() {
		networkPane.setVisible(true);
		controlPane.setVisible(true);

		pythonPane.setVisible(false);
		graphPane.setVisible(false);
		settingPane.setVisible(false);

		//graphPaneController.setActive(false);
		//networkPaneController.setActive(true);
	}

	@FXML
	private void enterEditorPane() {
		pythonPane.setVisible(true);
		controlPane.setVisible(false);

		graphPane.setVisible(false);
		networkPane.setVisible(false);
		settingPane.setVisible(false);

		//graphPaneController.setActive(false);
		//networkPaneController.setActive(false);
	}

	@FXML
	private void enterSettingPane() {
		settingPane.setVisible(true);
		controlPane.setVisible(false);

		pythonPane.setVisible(false);
		graphPane.setVisible(false);
		networkPane.setVisible(false);

		//graphPaneController.setActive(false);
		//networkPaneController.setActive(false);
		//configController.setMaximized(true);
		//configController.showAndWait();
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

		this.simulationFactory = SimulationFactory.getInstance(pathDialog.getInfrastructurePath(),
				pathDialog.getRollingStockPath(),
				pathDialog.getTimeTablePath());

		this.simulationFactory.registerUpdateUI(this);

		this.settingPaneController.setSimulator(this.simulationFactory.getSimulator());

		// TODO: check if it is successful
		this.networkPaneController
				.setInfrastructureServiceUtility(this.simulationFactory.getInfraServiceUtility());

		this.graphPaneController
				.setInfrastructureServiceUtility(this.simulationFactory.getInfraServiceUtility());

		this.graphPaneController.updateTrainMap(this.simulationFactory.getSimulator().getTrainSimulators());
	}

	/**
	 * shows updates of the data on the status bar
	 */
	private void updateStatusBar() {
		timeLabel.setText("Simulation Time: " + this.simulationFactory.getUpdateTime().toString());

		int numActive = 0;
		int numTerminate = 0;

		if (this.simulationFactory.getSimulator().getStatus() != SingleSimulationManager.INACTIVE) {
			for (EventListener listener : this.simulationFactory.getSimulator().getListeners()) {
				if (listener instanceof AbstractTrainSimulator) {
					AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

					if (trainSimulator.getTerminateTime() != null) {
						if (trainSimulator.getTerminateTime().compareTo(
								this.simulationFactory.getUpdateTime()) < 0) {
							numTerminate++;
						} else {
							if (trainSimulator.getActiveTime().compareTo(
									this.simulationFactory.getUpdateTime()) < 0) {
								numActive++;
							}
						}
					} else {
						if (trainSimulator.getActiveTime() != null
								&& trainSimulator.getActiveTime().compareTo(
										this.simulationFactory.getUpdateTime()) < 0) {
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
	public void updateUI() {
		Map<AbstractTrainSimulator, List<Coordinate>> coordinates = this.simulationFactory.getSimulator()
				.getTrainCoordinates(this.simulationFactory.getUpdateTime());

		if (coordinates != null) {
			networkPaneController.updateTrainCoordinates(coordinates,
					this.simulationFactory.getUpdateTime());

			this.graphPaneController.updateTrainMap(this.simulationFactory.getSimulator().getTrainSimulators());

			updateStatusBar();
		}

		this.settingPaneController.updateMessages(
				this.simulationFactory.getSimulator().getDispatchingSystem().getDispCommunication().getReceivedMessages(),
				this.simulationFactory.getSimulator().getDispatchingSystem().getDispCommunication().getSentMessages());
	}

	@Override
	public void setTime(boolean isReplay) {
		Duration updateInterval = Duration.fromTotalMilliSecond(UIPause);
		if (this.simulationFactory.getSimulator().getStatus() != SingleSimulationManager.INACTIVE) {
			updateInterval = Duration.fromTotalMilliSecond(MAXSpeed
					* speedBar.getValue() / 100);
			if (speedBar.getValue() == speedBar.getMin()) {
				updateInterval = Duration.fromTotalMilliSecond(UIPause);
			}

			if (speedBar.getValue() == speedBar.getMax()
					&& this.simulationFactory.getSimulator().getTime() != null
					&& this.simulationFactory.getSimulator().getStatus() != SingleSimulationManager.TERMINATED) {

				updateInterval = this.simulationFactory.getSimulator().getTime().getDifference(this.simulationFactory.getUpdateTime());
			}
		}
		if (isReplay) {
			this.simulationFactory.setUpdateTime(this.simulationFactory.getUpdateTime().add(updateInterval));
		} else {
			if (this.simulationFactory.getSimulator().getStatus() == SingleSimulationManager.RUNNING) { // not
																			// terminated
																			// yet
				this.simulationFactory.setUpdateTime(this.simulationFactory.getUpdateTime().add(updateInterval));
				if (this.simulationFactory.getUpdateTime().compareTo(this.simulationFactory.getSimulator().getTime()) > 0) {
					this.simulationFactory.setUpdateTime(this.simulationFactory.getSimulator().getTime()); // if update too
															// fast, slow down
				}
			} else {
				this.simulationFactory.setUpdateTime(this.simulationFactory.getUpdateTime().add(updateInterval));
			}
		}
	}

	private StackPane networkPane;
	private AnchorPane graphPane;
	private AnchorPane settingPane;
	private AnchorPane pythonPane;

	private NetworkPaneController networkPaneController;
	private GraphPaneController graphPaneController;
	private SettingPaneController settingPaneController;

	private int UIPause = 100;
	private int MAXSpeed = 20000; // 1 : 200
}
