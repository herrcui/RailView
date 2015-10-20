package railview.swarmintelligence;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.omg.CORBA.Current;

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
import railview.infrastructure.container.NetworkPaneController;
import railview.infrastructure.container.SwarmPane;

public class SwarmViewerController {
	
	
	@FXML
	private AnchorPane networkPaneRoot;
	
	@FXML
	private AnchorPane menuPane;
	
	@FXML
	private Label timeLabel;
	
	@FXML
	private Label activeLabel;
	
	@FXML
	private Label terminatedLabel;
	
	@FXML
	private Button startButton;
	
	@FXML
	private Button controlButton;
	
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
	public void handleButtonAction(ActionEvent event) {
			sideBar.setPickOnBounds(false);
	          final double startWidth = sideBar.getWidth();
	          final Animation hideSidebar = new Transition() {
	            { setCycleDuration(javafx.util.Duration.millis(250)); }
	            protected void interpolate(double frac) {
	              final double curWidth = startWidth * (1.0 - frac);
	              sideBar.setTranslateX(-startWidth + curWidth);
	            }
	          };
	          hideSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
	            @Override public void handle(ActionEvent actionEvent) {
	            	sideBar.setVisible(false);
	                controlButton.setText("Show");
	            }
	          });
	  
	 
	          final Animation showSidebar = new Transition() {
	            { setCycleDuration(javafx.util.Duration.millis(250)); }
	            protected void interpolate(double frac) {
	              final double curWidth = startWidth * frac;
	              sideBar.setTranslateX(-startWidth + curWidth);
	            }
	          };
	          showSidebar.onFinishedProperty().set(new EventHandler<ActionEvent>() {
	            @Override public void handle(ActionEvent actionEvent) {
	              controlButton.setText("Hide");
	            }
	          });
	  
	          if (showSidebar.statusProperty().get() == Animation.Status.STOPPED && hideSidebar.statusProperty().get() == Animation.Status.STOPPED) {
	            if (sideBar.isVisible()) {
	              hideSidebar.play();
	            } else {
	            	sideBar.setVisible(true);
	              showSidebar.play();
	            }
	          }
	        }
	
	
	@FXML
	public void initialize() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = NetworkPaneController.class
					.getResource("NetworkPane.fxml");
			loader.setLocation(location);
			StackPane networkPane = (StackPane) loader.load();
			this.networkPaneController = loader.getController();
			sideBar.setVisible(false);
			AnchorPane.setLeftAnchor(networkPane, (this.networkPaneRoot.prefWidth(-1)/2)-(networkPane.prefWidth(-1)/2));
			AnchorPane.setTopAnchor(networkPane,(this.networkPaneRoot.prefHeight(-1)/2)-(networkPane.prefHeight(-1)/2));
			this.networkPaneRoot.getChildren().addAll(networkPane);
			
			

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void startSimulation() {
		if (this.simulator != null) {
			new Thread(this.simulator).start();
			
			Thread t = new Thread(() -> {
				(new SwarmUpdater()).periodicalUpdate(false); 
			});			
			t.setDaemon(true);
			t.start();

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
			

	
	@FXML
	public void replaySimulation() {
		if (this.simulator != null) {
			Thread t = new Thread(() -> {
				(new SwarmUpdater()).periodicalUpdate(true); 
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
	
	private NetworkPaneController networkPaneController;
	private SimulationManager simulator;
	private SwarmManager swarmManager;
	private Duration updateInterval = Duration.fromSecond(60);
	private int UIPause = 100;
	
	class SwarmUpdater {
		private Time time = Time.getInstance(0, 0, 0);
		boolean isUpdateCompleted = false;
		
		void periodicalUpdate(boolean isReplay) {
			while (! isUpdateCompleted) {
				Platform.runLater(new Runnable() {
					@Override public void run() {
						timeLabel.setText("Simulation Time: " + time.toString());

						Collection<Swarm> swarms = swarmManager.getSwarms(time);
						Map<AbstractTrainSimulator, List<Coordinate>> coordinates = simulator.getTrainCoordinates(time);
						networkPaneController.updateSwarms(coordinates, swarms, time);
						
						updateSwarms(swarms, time);
						
						if (simulator.getTime() != null || simulator.getTerminatedTime() != null) { 
							// simulator.getTime() != null: after initialization and before simulation finished
							// simulator.getTerminatedTime() != null: simulation finished
							int numActive = 0;
							int numTerminate = 0;
							for (EventListener listener : simulator.getListeners()) {
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
						
							if (numActive == 0 && swarms.size() == 1) {
								String s = "";
							}
							
							activeLabel.setText("Active Trains/Swarms: " + numActive + "/" + swarms.size());
							terminatedLabel.setText("Terminated Trains: " + numTerminate);
						} // if (simulator.getTime() != null)
						
						if (simulator.getTerminatedTime() != null &&
								time.compareTo(simulator.getTerminatedTime()) >= 0) {
							isUpdateCompleted = true;
						}
					}
				});
				
				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				if (isReplay) {
					time = time.add(updateInterval);
				} else {
					if (simulator.getTerminatedTime() == null) { // not terminated yet
						if (simulator.getTime() != null) { // initialization not completed yet
							time = time.add(updateInterval);
							if (time.compareTo(simulator.getTime()) > 0) {
								time = simulator.getTime(); // if update too fast, slow down
							}
						}
					} else {
						time = time.add(updateInterval);
					}
				}
			}
		} // periodicalUpdate(boolean isReplay)
	}
}