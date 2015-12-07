package railview.swarmintelligence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Transition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.input.MouseEvent;
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
import railview.controller.framework.AbstractSimulationController;
import railview.swarmintelligence.ui.GraphPaneController;
import railview.infrastructure.container.NetworkPaneController;
import railview.swarmintelligence.ui.SwarmSidebarController;

public class SwarmViewerController extends AbstractSimulationController {

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
	private Button lockButton;
	
	@FXML
	private Button unlockButton;

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
			this.graphPaneController = graphpaneloader.getController();

			AnchorPane.setLeftAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setTopAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setBottomAnchor(swarmSidebarPane, 0.0);
			AnchorPane.setRightAnchor(swarmSidebarPane, 0.0);

			this.networkPaneRoot.getChildren().addAll(networkPane, graphPane);
			sideBar.getChildren().add(0, swarmSidebarPane);

			networkPaneRoot.widthProperty().addListener(new ChangeListener<Number>() {
			    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
			    	symbolPane.setLayoutX((newSceneWidth.doubleValue()- symbolPane.getPrefWidth())/2);
			    	networkPane.setLayoutX((newSceneWidth.doubleValue() / 2)- (networkPane.prefWidth(-1) / 2));
			    	graphPane.setPrefWidth(newSceneWidth.doubleValue());
			    }
			});

			networkPaneRoot.heightProperty().addListener(new ChangeListener<Number>() {
			    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
			    	sidebarOpenButton.setLayoutY(newSceneHeight.doubleValue()/2 - menuPane.getPrefHeight());
			    	sidebarCloseButton.setLayoutY(newSceneHeight.doubleValue()/2 - menuPane.getPrefHeight());
			    	networkPane.setLayoutY((newSceneHeight.doubleValue() / 2)- (networkPane.prefHeight(-1) / 2));
			       	graphPane.setPrefHeight(newSceneHeight.doubleValue());
			    }
			});
			
			sideBar.setVisible(false);
			graphPane.setVisible(false);
			symbolPane.setOpacity(0.0);

			startButton.setDisable(false);
			pauseButton.setDisable(true);
			stopButton.setDisable(true);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void startSimulation() {
		super.startSimulation();

		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);
	}

	@FXML
	public void pauseSimulation() {
		super.pauseSimulation();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(false);
	}

	@FXML
	public void stopSimulation() {
		super.stopSimulation();
		this.swarmManager.stop();

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);
	}
	
	@FXML
	public void lock(){
		
		menuPane.setOnMouseEntered(new EventHandler<MouseEvent>(){

            public void handle(MouseEvent event)
            {
            	menuPane.setOpacity(1.0);
            }
		});
		menuPane.setOnMouseExited(new EventHandler<MouseEvent>(){

            public void handle(MouseEvent event)
            {
            	menuPane.setOpacity(1.0);
            }
		});
		lockButton.setVisible(false);
		unlockButton.setVisible(true);
	}
	
	@FXML
	public void unlock(){
		
			menuPane.setOnMouseEntered(new EventHandler<MouseEvent>(){

	            public void handle(MouseEvent event)
	            {
			FadeTransition fadeTransition1 = new FadeTransition(
					javafx.util.Duration.millis(500), menuPane);
			fadeTransition1.setFromValue(0);
			fadeTransition1.setToValue(1.0);
			fadeTransition1.play();
	            }
			});
			
			menuPane.setOnMouseExited(new EventHandler<MouseEvent>(){

	            public void handle(MouseEvent event)
	            {
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

	public void setSwarmManager(SwarmManager swarmManager) {
		this.swarmManager = swarmManager;
		this.graphPaneController.setSwarmManager(swarmManager);
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

	@Override
	protected void updateUI() {
		Collection<Swarm> swarms = swarmManager.getSwarms(updateTime);
		Map<AbstractTrainSimulator, List<Coordinate>> coordinates = simulator
				.getTrainCoordinates(updateTime);
		this.networkPaneController.updateSwarms(coordinates, swarms, updateTime);
		this.swarmSidebarController.updateSwarms(swarms, updateTime);
		this.graphPaneController.setScatterChart(updateTime);

		updateStatusBar(swarms);
	}

	private void updateStatusBar(Collection<Swarm> swarms) {
		timeLabel.setText("Simulation Time: " + this.updateTime.toString());

		int numActive = 0;
		int numTerminate = 0;

		if (simulator.getStatus() != SimulationManager.INACTIVE) {
			for (EventListener listener : simulator
					.getListeners()) {
				if (listener instanceof AbstractTrainSimulator) {
					AbstractTrainSimulator trainSimulator = (AbstractTrainSimulator) listener;

					if (trainSimulator.getTerminateTime() != null) {
						if (trainSimulator.getTerminateTime().compareTo(updateTime) < 0) {
							numTerminate++;
						} else {
							if (trainSimulator.getActiveTime().compareTo(updateTime) < 0) {
								numActive++;
							}
						}
					} else {
						if (trainSimulator.getActiveTime() != null &&
							trainSimulator.getActiveTime().compareTo(updateTime) < 0) {
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

	@Override
	protected void setTime(boolean isReplay) {
		Duration updateInterval = Duration.fromTotalMilliSecond(UIPause);
		if (simulator.getStatus() != SimulationManager.INACTIVE) {
			updateInterval = Duration.fromTotalMilliSecond(MAXSpeed * speedBar.getValue()/100);
			if (speedBar.getValue() == speedBar.getMin()) {
				updateInterval = Duration.fromTotalMilliSecond(UIPause);
			}

			if (speedBar.getValue() == speedBar.getMax() &&
				simulator.getTime() != null &&
				simulator.getStatus() != SimulationManager.TERMINATED) {

				updateInterval = simulator.getTime().getDifference(updateTime);
			}
		}

		if (isReplay) {
			this.updateTime = updateTime.add(updateInterval);
		} else {
			if (simulator.getStatus() == SimulationManager.RUNNING) { // not terminated yet
				updateTime = updateTime.add(updateInterval);
				if (updateTime.compareTo(simulator.getTime()) > 0) {
					updateTime = simulator.getTime(); // if update too fast, slow down
				}
			} else {
				updateTime = updateTime.add(updateInterval);
			}
		}
	}

	private AnchorPane graphPane;
	private StackPane networkPane;

	private NetworkPaneController networkPaneController;
	private SwarmSidebarController swarmSidebarController;
	private GraphPaneController graphPaneController;

	private SwarmManager swarmManager;
	private int UIPause = 100;
	private int MAXSpeed = 20000; // 1 : 200
}