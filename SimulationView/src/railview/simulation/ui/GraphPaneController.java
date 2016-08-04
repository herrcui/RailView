package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.events.ScheduledEvent;
import railapp.simulation.events.totrain.AbstractEventToTrain;
import railapp.simulation.events.totrain.UpdateLocationEvent;
import railapp.simulation.infrastructure.PartialRouteResource;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.ui.components.BlockingTimeChart;
import railview.simulation.ui.components.DraggableChart;
import railview.simulation.ui.components.Zoom;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TimeDistance;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

public class GraphPaneController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private AnchorPane runningPane;

	@FXML
	private AnchorPane runningDynamicsRoot;
	
	@FXML
	private AnchorPane runningdynamicsPane;
	
	@FXML
	private AnchorPane trainRunMonitorRoot;
	
	@FXML
	private AnchorPane trainRunMonitorPane;

	@FXML
	public void initialize() {
		tabPane.setSide(Side.BOTTOM);

		try {
			FXMLLoader runningDynamicsLoader = new FXMLLoader();
			URL location = RunningDynamicsPaneController.class
					.getResource("RunningDynamicsPane.fxml");
			runningDynamicsLoader.setLocation(location);
			this.runningdynamicsPane = (AnchorPane) runningDynamicsLoader.load();
			this.runningDynamicsPaneController = runningDynamicsLoader.getController();
			this.runningDynamicsPaneController.setTrainMap(this.trainMap);
			
			this.runningDynamicsRoot.getChildren().add(runningdynamicsPane);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FXMLLoader trainRunMonitorLoader = new FXMLLoader();
			URL location = TrainRunMonitorController.class
					.getResource("TrainRunMonitorPane.fxml");
			trainRunMonitorLoader.setLocation(location);
			this.trainRunMonitorPane = (AnchorPane) trainRunMonitorLoader.load();
			this.trainRunMonitorController = trainRunMonitorLoader.getController();
			this.trainRunMonitorController.setTrainMap(this.trainMap);
			
			this.trainRunMonitorRoot.getChildren().add(trainRunMonitorPane);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.trainRunMonitorController.setInfrastructureServiceUtility(infraServiceUtility);
	}

	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(),
					trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE
					&& !numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}
		
		this.runningDynamicsPaneController.setTrainNumbers(numbers);
		this.trainRunMonitorController.setTrainNumbers(numbers);
	}

	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}
	
	private RunningDynamicsPaneController runningDynamicsPaneController;
	
	private TrainRunMonitorController trainRunMonitorController;

	private ObservableList<String> numbers = FXCollections.observableArrayList();
	
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap = 
			new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
