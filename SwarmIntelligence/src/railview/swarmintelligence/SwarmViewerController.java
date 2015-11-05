package railview.swarmintelligence;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.simulation.events.EventListener;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.Swarm;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Time;
import railview.infrastructure.container.GraphPaneController;
import railview.infrastructure.container.NetworkPaneController;
import railview.swarmintelligence.ui.SwarmSidebarController;

public class SwarmViewerController {

	@FXML
	private AnchorPane networkPaneRoot;

	@FXML
	private AnchorPane menuPane;

	@FXML
	private AnchorPane symbolPane;

	@FXML
	private Label timeLabel;

	@FXML
	private Label activeLabel;

	@FXML
	private Label terminatedLabel;

	@FXML
	private Button startButton;
	
	@FXML
	private Button pauseButton;
	
	@FXML
	private Button stopButton;

	@FXML
	private Slider speedBar;
	
	@FXML
	private Button sidebarOpenButton;

	@FXML
	private Button sidebarCloseButton;

	@FXML
	private Button graphButton;

	@FXML
	private Button swarmButton;

	@FXML
	private AnchorPane sideBar;

	@FXML
	private TableView<Swarm> informationTable;

	@FXML
	private TableColumn<Swarm, String> firstColumn;

	@FXML
	private TableColumn<Swarm, String> secondColumn;

	@FXML
	private TableColumn<AbstractTrainSimulator, String> thirdColumn;

	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();

			FXMLLoader sidebarloader = new FXMLLoader();
			URL sidebarlocation = SwarmSidebarController.class
					.getResource("SwarmSidebar.fxml");
			sidebarloader.setLocation(sidebarlocation);
			StackPane swarmSidebarPane = (StackPane) sidebarloader.load();
			this.swarmSidebarController = sidebarloader.getController();

			FXMLLoader graphpaneloader = new FXMLLoader();
			URL graphpanelocation = GraphPaneController.class
					.getResource("GraphPane.fxml");
			graphpaneloader.setLocation(graphpanelocation);
			graphPane = (AnchorPane) graphpaneloader.load();

			AnchorPane.setLeftAnchor(
					networkPane,
					(this.networkPaneRoot.prefWidth(-1) / 2)
							- (networkPane.prefWidth(-1) / 2));
			AnchorPane.setTopAnchor(
					networkPane,
					(this.networkPaneRoot.prefHeight(-1) / 2)
							- (networkPane.prefHeight(-1) / 2));

			AnchorPane.setLeftAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setTopAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setBottomAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setRightAnchor(swarmSidebarPane, 0.0);

			this.networkPaneRoot.getChildren().addAll(networkPane, graphPane);
			sideBar.getChildren().add(0, swarmSidebarPane);

			sideBar.setVisible(false);
			graphPane.setVisible(false);
			
			startButton.setDisable(false);
			pauseButton.setDisable(true);
			stopButton.setDisable(true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void startSimulation() {
		if (isPause) {
			isPause = false;
		}
		
		if (isStop) {
			isStop = false;
		}
		
		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);
		
		if (this.simulator != null){
			if (this.simulationThread == null || !this.simulationThread.isAlive()) {
				this.simulationThread = new Thread(this.simulator);
				this.simulationThread.start();
			}
			
			if (this.updateThread == null || !this.updateThread.isAlive()) {	
				this.updateThread = new Thread(() -> {
					(new SwarmUpdater()).periodicalUpdate(false);
				});
				this.updateThread.start();	
			}
		}
	}
	
	@FXML
	public void pauseSimulation() {
		if (this.simulator != null) {
			if (! isPause) {
				isPause = true;
				
				this.startButton.setDisable(false);
				this.pauseButton.setDisable(true);
				this.stopButton.setDisable(false);
			}
		}
	}
	
	@FXML
	public void stopSimulation() {
		if (this.simulator != null) {
			this.simulator.stop();
			this.swarmManager.stop();
		}
		
		this.isStop = true;
		
		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);

		try {
			this.simulationThread.join();
			this.updateThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
	}

	@FXML
	private void fadeRoot() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), menuPane);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

		FadeTransition fadeTransition2 = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition2.setToValue(0.0);
		fadeTransition2.play();
	}

	@FXML
	private void appear() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), menuPane);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
	}

	@FXML
	private void fadeMenu() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), menuPane);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

	}

	@FXML
	private void fadeSymbolPaneRoot() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();
	}

	@FXML
	private void appearSymbolPane() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(0.0);
		fadeTransition.setToValue(1.0);
		fadeTransition.play();
	}

	@FXML
	private void fadeSymbolPane() {
		FadeTransition fadeTransition = new FadeTransition(
				javafx.util.Duration.millis(500), symbolPane);
		fadeTransition.setFromValue(1.0);
		fadeTransition.setToValue(0.0);
		fadeTransition.play();

	}

	@FXML
	public void graphButtonClicked() {
		graphPane.setVisible(true);
		networkPane.setVisible(false);
	}

	@FXML
	public void swarmButtonClicked() {
		graphPane.setVisible(false);
		networkPane.setVisible(true);
	}

	@FXML
	public void handleButtonAction(ActionEvent event) {
		sideBar.setPickOnBounds(false);
		final double startWidth = sideBar.getWidth();
		final Animation hideSidebar = new Transition() {
			{
				setCycleDuration(javafx.util.Duration.millis(250));
			}

			protected void interpolate(double frac) {
				final double curWidth = startWidth * (1.0 - frac);
				sideBar.setTranslateX(-startWidth + curWidth);
			}
		};
		hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				sideBar.setVisible(false);
				sidebarOpenButton.setVisible(true);
			}
		});

		final Animation showSidebar = new Transition() {
			{
				setCycleDuration(javafx.util.Duration.millis(250));
			}

			protected void interpolate(double frac) {
				final double curWidth = startWidth * frac;
				sideBar.setTranslateX(-startWidth + curWidth);
			}
		};
		
		showSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				sidebarOpenButton.setVisible(false);
			}
		});

		if (showSidebar.statusProperty().get() == Animation.Status.STOPPED
				&& hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
			if (sideBar.isVisible()) {
				hideSidebar.play();
			} else {
				sideBar.setVisible(true);
				showSidebar.play();
			}
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

	public void setSwarmManager(SwarmManager swarmManager) {
		this.swarmManager = swarmManager;
	}

	public void updateSwarms(Collection<Swarm> swarms, Time time) {
		ObservableList<Swarm> data = FXCollections.observableArrayList();
		data.addAll(swarms);

		firstColumn.setCellValueFactory(celldata -> new SimpleStringProperty(
				celldata.getValue().getId().toString()));
		secondColumn.setCellValueFactory(celldata -> new SimpleStringProperty(
				celldata.getValue().getCreationTime().toString()));

		informationTable.setItems(data);
	}

	private AnchorPane graphPane;
	private StackPane networkPane;

	private NetworkPaneController networkPaneController;
	private SwarmSidebarController swarmSidebarController;

	private SimulationManager simulator;
	private SwarmManager swarmManager;
	private int UIPause = 100;
	private int MAXSpeed = 20000; // 1 : 200
	
	private boolean isPause = false;
	private boolean isStop = false;
	
	private Thread updateThread = null;
	private Thread simulationThread = null;

	class SwarmUpdater {
		private Time time = Time.getInstance(0, 0, 0);
		private boolean updateTerminated = false;

		boolean isUpdateTerminated() {
			return this.updateTerminated;
		}
		
		void periodicalUpdate(boolean isReplay) {			
			while (! updateTerminated) {
				if (isStop) {
					updateTerminated = true;
					break;
				}
				
				if (simulator.getStatus() == SimulationManager.TERMINATED && time.compareTo(simulator.getTime()) > 0) {
					updateTerminated = true;
					break;
				}
				
				if (!isPause) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Collection<Swarm> swarms = swarmManager.getSwarms(time);
							Map<AbstractTrainSimulator, List<Coordinate>> coordinates = simulator
									.getTrainCoordinates(time);
							networkPaneController.updateSwarms(coordinates, swarms,
									time);
							swarmSidebarController.updateSwarms(swarms, time);
							
							updateStatusBar(swarms);
						} // public void run() {
					}); // Platform.runLater(new Runnable() {
					
					this.setTime(isReplay);
				} // if (!isPause)
				
				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // while ((simulator.getTerminatedTime() == null || time.compareTo(simulator.getTerminatedTime()) < 0))
		} // periodicalUpdate(boolean isReplay)
	
		private void updateStatusBar(Collection<Swarm> swarms) {
			timeLabel.setText("Simulation Time: " + time.toString());
			
			int numActive = 0;
			int numTerminate = 0;
			
			if (simulator.getStatus() != SimulationManager.INACTIVE) {
				for (EventListener listener : simulator
						.getListeners()) {
					if (listener instanceof AbstractTrainSimulator) {
						AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

						if (trainSimulator.getTerminateTime() != null) {
							if (trainSimulator.getTerminateTime().compareTo(time) < 0) {
								numTerminate++;
							} else {
								if (trainSimulator.getActiveTime().compareTo(time) < 0) {
									numActive++;
								}
							}
						} else {
							if (trainSimulator.getActiveTime() != null && 
								trainSimulator.getActiveTime().compareTo(time) < 0) {
								numActive++;
							}
						}
					}
				}
			} // if (simulator.getStatus() != SimulationManager.INACTIVE)
			
			activeLabel.setText("Active Trains/Swarms: "
					+ numActive + "/" + swarms.size());
			terminatedLabel.setText("Terminated Trains: "
					+ numTerminate);
		}
		
		private void setTime(boolean isReplay) {
			Duration updateInterval = Duration.fromTotalMilliSecond(UIPause);
			if (simulator.getStatus() != SimulationManager.INACTIVE) {
				updateInterval = Duration.fromTotalMilliSecond(MAXSpeed * speedBar.getValue()/100);
				if (speedBar.getValue() == speedBar.getMin()) {
					updateInterval = Duration.fromTotalMilliSecond(UIPause);
				}
				
				if (speedBar.getValue() == speedBar.getMax() && simulator.getTime() != null) {
					updateInterval = simulator.getTime().getDifference(time);
				}
			}

			if (isReplay) {
				time = time.add(updateInterval);
			} else {
				if (simulator.getStatus() == SimulationManager.RUNNING) { // not terminated yet
					time = time.add(updateInterval);
					if (time.compareTo(simulator.getTime()) > 0) {
						time = simulator.getTime(); // if update too fast, slow down
					}
				} else {
					time = time.add(updateInterval);
				}
			}
		}
	}
}