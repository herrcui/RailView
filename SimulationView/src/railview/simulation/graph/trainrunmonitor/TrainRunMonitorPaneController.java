package railview.simulation.graph.trainrunmonitor;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.data.TrainRunDataManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.util.Callback;

/**
 * The controller class for TrainRunMonitorPane.fxml. The Pane gives a
 * selectable list of trains where you can see their blockingTimeChart.
 *
 */
public class TrainRunMonitorPaneController {
	@FXML
	private AnchorPane blockingTimePane, snapshotRoot, tripRoot, lineRoot, lineMonitorPane;

	@FXML
	private SplitPane tripMonitorPane;

	@FXML
	private ListView<String> trainNumbers, lineListView, stationListView;

	@FXML
	private TableView<TableProperty> trainInfoTable;

	@FXML
	private CheckBox selfEventCheckBox, inEventCheckBox, outEventCheckBox;

	private TripMonitorPaneController tripMonitorPaneController;
	private LineMonitorPaneController lineMonitorPaneController;
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap;
	private IInfrastructureServiceUtility infrastructureServiceUtility;
	private static HashMap<String, Line> lineMap = new HashMap<String, Line>();
	private TrainRunDataManager trainRunDataManager = new TrainRunDataManager();

	/**
	 * initialize the trainRunMonitorPane, add blockingTimeChart on top of it,
	 * add zoom function, load snapshotPane, add window resize listener, create
	 * eventTable and trainInfoTable
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@FXML
	public void initialize() {
		try {
			FXMLLoader tripMonitorPaneLoader = new FXMLLoader();
			URL location = TripMonitorPaneController.class.getResource("TripMonitorPane.fxml");
			tripMonitorPaneLoader.setLocation(location);
			tripMonitorPane = (SplitPane) tripMonitorPaneLoader.load();
			this.tripMonitorPaneController = tripMonitorPaneLoader.getController();

			this.tripRoot.getChildren().add(tripMonitorPane);
		} catch (IOException e) {
			e.printStackTrace();
		}

		trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(
					ObservableValue<? extends String> observable,
					String oldValue, String newValue) {

				lineRoot.setVisible(false);
				tripRoot.setVisible(true);

				AbstractTrainSimulator train = trainMap.get(
					trainNumbers.getSelectionModel().getSelectedItem().toString());

				trainInfoTable.setItems(generateTrainInfo(train, newValue));

				List<Coordinate> path = trainRunDataManager.getTrainPathCoordinates(train);

				List<BlockingTime> blockingTime = trainRunDataManager.getBlockingTimeStairway(train, null);

				Map<TimeDistance, List<EventData>> events = trainRunDataManager.getEvents(train);

				List<TimeDistance> timeDistances = trainRunDataManager.getTimeInDistance(train, null);

				tripMonitorPaneController.updateUI(train, path, blockingTime, timeDistances, events);
				// TODO put the logic in controller
				/*
				if (oldValue == null || !oldValue.equals(newValue)) {
					eventTable.getItems().clear();
				}
				*/
			}

		});

		// initialize trainInfoTable
		TableColumn trainItemCol = new TableColumn("Item");
		trainItemCol.setMinWidth(100);
		trainItemCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("item"));

		TableColumn trainValueCol = new TableColumn("Value");
		trainValueCol.setMinWidth(100);
		trainValueCol.setCellValueFactory(new PropertyValueFactory<TableProperty, String>("value"));

		trainInfoTable.getColumns().addAll(trainItemCol, trainValueCol);

		trainValueCol.setCellFactory(createCellFactory());

		try {
			FXMLLoader lineMonitorPaneLoader = new FXMLLoader();
			URL location = LineMonitorPaneController.class.getResource("LineMonitorPane.fxml");
			lineMonitorPaneLoader.setLocation(location);
			lineMonitorPane = (AnchorPane) lineMonitorPaneLoader.load();
			this.lineMonitorPaneController = lineMonitorPaneLoader.getController();

			this.lineRoot.getChildren().add(lineMonitorPane);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>> createCellFactory() {
		return new Callback<TableColumn<TableProperty, String>, TableCell<TableProperty, String>>() {
			@Override
			public TableCell<TableProperty, String> call(
					TableColumn<TableProperty, String> param) {
				TableCell<TableProperty, String> cell = new TableCell<>();
				Text text = new Text();
				cell.setGraphic(text);
				cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
				text.wrappingWidthProperty().bind(cell.widthProperty());
				text.textProperty().bind(cell.itemProperty());
				return cell;
			}
		};
	}


	public void setTrainMap(ConcurrentHashMap<String, AbstractTrainSimulator> trainMap) {
		this.trainMap = trainMap;
	}

	public void setTrainNumbers(ObservableList<String> numbers) {
		this.trainNumbers.setItems(numbers);
	}

	public void setInfrastructureServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.tripMonitorPaneController.setInfrastructureServiceUtility(infraServiceUtility);
		this.trainRunDataManager.setInfraServiceUtility(infraServiceUtility);

		this.infrastructureServiceUtility = infraServiceUtility;

		for (Line line : this.infrastructureServiceUtility.getNetworkService().allLines()) {
			lineMap.put(line.getDescription(), line);
		} // Kai

		ObservableList<String> lineList = FXCollections.observableArrayList();

		for (Line line : lineMap.values()) {
			lineList.add(line.getName());
		}
		lineListView.setItems(lineList);

		lineListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (oldValue == null || !oldValue.equals(newValue)) {
					stationListView.getItems().clear();
					//linePane.getChildren().clear();
				}
				lineRoot.setVisible(true);
				tripRoot.setVisible(false);

				String lineString = lineListView.getSelectionModel().getSelectedItem().toString();
				Line line = lineMap.get(lineString);

				Collection<Station> stations = infraServiceUtility.getLineService().findStationsByLine(line);
				HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeMap =
						trainRunDataManager.getBlockingTimeStairwaysInLine(line, trainMap.values());
				HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistanceMap =
						trainRunDataManager.getTimeDistancesInLine(line, trainMap.values());

				ObservableList<String> stationNameList = FXCollections.observableArrayList();
				for (Station station : stations) {
					stationNameList.add(station.getName());
				}
				stationListView.setItems(stationNameList);

				lineMonitorPaneController.updateUI(line, stations, blockingTimeMap, timeDistanceMap);
			}
		});
	}

	public static ObservableList<TableProperty> generateTrainInfo(
			AbstractTrainSimulator train, String trainNumber) {

		ObservableList<TableProperty> observableTrainInfoList = FXCollections.observableArrayList();
		observableTrainInfoList.add(new TableProperty("Train Number", trainNumber));
		observableTrainInfoList.add(new TableProperty("State",
			train.getTrain().getStatus() == SimpleTrain.ACTIVE ? "In operation ..."	: "Terminated"));
		List<TripElement> elements = train.getTripSection().getTripElements();
		observableTrainInfoList.add(new TableProperty("From",
			((InfrastructureObject) elements.get(0).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("To",
			((InfrastructureObject) elements.get(elements.size() - 1).
					getOperationalPoint()).getElement().getStation().getDescription()));
		observableTrainInfoList.add(new TableProperty("Start time",
			elements.get(0).getArriveTime().toString()));

		return observableTrainInfoList;
	}

}
