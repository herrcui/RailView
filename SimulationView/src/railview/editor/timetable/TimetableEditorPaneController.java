package railview.editor.timetable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;
import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.IOperationalPoint;
import railapp.infrastructure.path.dto.AggregatedStationRoute;
import railapp.infrastructure.path.dto.StationRoute;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.parser.coremodel.infrastructure.InfrastructureParser;
import railapp.parser.coremodel.rollingstock.RollingStockParser;
import railapp.parser.coremodel.timetable.TimetableWriter;
import railapp.rollingstock.dto.TrainDefinition;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.entries.EntryUtilities;
import railapp.simulation.entries.SchedulingEntry;
import railapp.timetable.dto.EntryInfo;
import railapp.timetable.dto.TrainGroup;
import railapp.timetable.dto.Trip;
import railapp.timetable.dto.TripElement;
import railapp.timetable.scheduling.TrainGroupStationSequence;
import railapp.timetable.service.ITimetableServiceUtility;
import railapp.units.Duration;
import railapp.units.Time;

public class TimetableEditorPaneController {
    @FXML
    private AnchorPane timetableEditorPaneRoot, schedulingRoot, tripRoot;

    @FXML
    private Label folderLabel;

    @FXML
    private TextField nameTextField, despTextField, startTimeTextField, intervalTextField, numOfTrainsTextField, startNumTextField;

    @FXML
    private ComboBox<Station> fromCBBox, toCBBox;

    @FXML
    private ComboBox<TrainDefinition> trainDefCBBox;

    @FXML
    private TableView<AggregatedStationRoute> pathTableView;

    @FXML
    private TableView<Trip> tripsTableView;

    @FXML
    private TableView<TripElement> tripDetailTableView;

    @FXML
    private TableView<TrainGroup> trainGroupTableView;

    private SchedulingEntry schedulingEntry;

    private String path;

    private StationRoute[] stationRoutesInPath = null;

    private TrainGroup trainGroup = null;

    @SuppressWarnings("unchecked")
	public void initialize() {
    	TableColumn<TrainGroup, String> nameColumn = new TableColumn<TrainGroup, String>("Group Name");
    	nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

    	this.trainGroupTableView.getColumns().addAll(nameColumn);

    	this.trainGroupTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<TrainGroup>() {
			@Override
			public void onChanged(Change<? extends TrainGroup> change) {
				if (change.getList().size() > 0) {
					trainGroup = change.getList().get(0);
					updateTrainGroupUI(trainGroup);
				}
			}
		});


    	TableColumn<Trip, String> tripNumberColumn = new TableColumn<Trip, String>("Trip Number");
    	tripNumberColumn.setCellValueFactory(new PropertyValueFactory<>("number"));

        TableColumn<Trip, Time> startTimeColumn = new TableColumn<Trip, Time>("Start Time");
        startTimeColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));

        this.tripsTableView.getColumns().addAll(tripNumberColumn, startTimeColumn);

        this.tripsTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Trip>() {
			@Override
			public void onChanged(Change<? extends Trip> change) {
				if (change.getList().size() > 0) {
				    Trip trip = change.getList().get(0);

				    ObservableList<TripElement> elementItems = FXCollections.<TripElement>observableArrayList();
				    elementItems.addAll(trip.getTripSections().get(0).getTripElements());
				    tripDetailTableView.setItems(elementItems);
				}
			}
		});

        TableColumn<TripElement, String> stationColumn = new TableColumn<TripElement, String>("Station");
        stationColumn.setCellValueFactory(new PropertyValueFactory<>("station"));

        TableColumn<TripElement, String> arriveColumn = new TableColumn<TripElement, String>("Arrive");
        arriveColumn.setCellValueFactory(new PropertyValueFactory<>("arriveTime"));

        TableColumn<TripElement, String> dwellColumn = new TableColumn<TripElement, String>("Dwell");
        dwellColumn.setCellValueFactory(new PropertyValueFactory<>("dwellTimespan"));

        TableColumn<TripElement, String> departureColumn = new TableColumn<TripElement, String>("Departure");
        departureColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));

        TableColumn<TripElement, String> stopColumn = new TableColumn<TripElement, String>("Stop");
        stopColumn.setCellValueFactory(new PropertyValueFactory<>("operationalPoint"));

        this.tripDetailTableView.getColumns().addAll(stationColumn, arriveColumn, dwellColumn, departureColumn, stopColumn);
    }

    private void updateTrainGroupUI(TrainGroup trainGroup) {
    	if (trainGroup != null) {
    		this.nameTextField.setText(trainGroup.getName());
    		this.despTextField.setText(trainGroup.getDescrption());
    		this.fromCBBox.getSelectionModel().select(trainGroup.getStart());
    		this.toCBBox.getSelectionModel().select(trainGroup.getEnd());
    		this.trainDefCBBox.getSelectionModel().select(trainGroup.getTrainDefintion());
    		this.startTimeTextField.setText(trainGroup.getStartTime().toStringInHMS());
    		this.intervalTextField.setText(Time.getInstance(0).add(trainGroup.getInterval()).toStringInHMS());
    		this.numOfTrainsTextField.setText("" + trainGroup.getNumOfTrains());
    		this.startNumTextField.setText(trainGroup.getTripList().size() > 0 ?
    				trainGroup.getTripList().get(0).getNumber().replaceAll(trainGroup.getName(), "") : "");

    		ObservableList<Trip> tripsData = FXCollections.observableArrayList();
    		tripsData.addAll(trainGroup.getTripList());
    		this.tripsTableView.setItems(tripsData);
    		this.tripsTableView.getSelectionModel().select(trainGroup.getTripList().size() > 0 ? 0 : -1);
    	} else {
    		this.nameTextField.setText("");
    		this.despTextField.setText("");
    		this.fromCBBox.getSelectionModel().select(-1);
    		this.toCBBox.getSelectionModel().select(-1);
    		this.trainDefCBBox.getSelectionModel().select(-1);
    		this.startTimeTextField.setText("");
    		this.intervalTextField.setText("");
    		this.numOfTrainsTextField.setText("1");
    		this.startNumTextField.setText("");
    		this.tripDetailTableView.getItems().clear();
    		this.tripsTableView.getItems().clear();
    	}
    }

    private void loadData() {
        IInfrastructureServiceUtility infraServiceUtility = new railapp.infrastructure.service.ServiceUtility();
        IRollingStockServiceUtility rollingStockServiceUtility = new railapp.rollingstock.service.ServiceUtility();

        InfrastructureParser.getInstance(infraServiceUtility, path + "\\infrastructure").parse();
        RollingStockParser.getInstance(rollingStockServiceUtility, path + "\\rollingstock").parse();

        this.schedulingEntry = SchedulingEntry.getInstance(infraServiceUtility, rollingStockServiceUtility);

        ITimetableServiceUtility timeTableServiceUtility = this.schedulingEntry.getUtilities().getTimeTableServiceUtility();

        ObservableList<TrainGroup> trainGroupData = FXCollections.observableArrayList();
        trainGroupData.addAll(timeTableServiceUtility.getTimetableService().findAllTrainGroups());
        this.trainGroupTableView.setItems(trainGroupData);
        if (trainGroupData.size() > 0) {
        	this.trainGroupTableView.getSelectionModel().select(0);
        }

        ObservableList<TrainDefinition> trainDefData = FXCollections.observableArrayList();
        trainDefData.addAll(rollingStockServiceUtility.getRollingStockService().findTrainDefinitionsByClass(null));
        this.trainDefCBBox.setItems(trainDefData);
        this.trainDefCBBox.getSelectionModel().select(0);;

        ObservableList<Station> stationData = FXCollections.observableArrayList();
        stationData.addAll(infraServiceUtility.getNetworkService().allStations());
        this.fromCBBox.setItems(stationData);
        this.toCBBox.setItems(stationData);
    }

    @FXML
    public void onSetWorkingFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(timetableEditorPaneRoot.getScene().getWindow());
        if (file != null) {
            this.path = file.toString();
            folderLabel.setText(this.path);
            this.loadData();
        }
    }

    @SuppressWarnings("unchecked")
    @FXML
    public void onSetTrainPath() {
        Station fromStation = this.fromCBBox.getValue();
        Station toStation = this.toCBBox.getValue();

        List<TrainGroupStationSequence> TGSequences = this.schedulingEntry.findTrainGroupStationSequences(fromStation.getName(), toStation.getName());
        for (TrainGroupStationSequence sequence : TGSequences) {
            System.out.println(sequence);
            System.out.println(sequence.getNumberOfAlternatives());
        }

        TrainGroupStationSequence anySequence = TGSequences.get(0);
        List<AggregatedStationRoute> anyPath = anySequence.sampleAlternative();
        this.stationRoutesInPath = new StationRoute[anyPath.size()];

        ObservableList<AggregatedStationRoute> aggregateRouteItems = FXCollections.<AggregatedStationRoute>observableArrayList();
        aggregateRouteItems.addAll(anyPath);

        TableColumn<AggregatedStationRoute, String> stationColumn = new TableColumn<AggregatedStationRoute, String>("Station");
        stationColumn.setCellValueFactory(new PropertyValueFactory<>("station"));

        TableColumn<AggregatedStationRoute, String> stationRoutesColumn = new TableColumn<AggregatedStationRoute, String>("Routes");
        stationRoutesColumn.setCellValueFactory(new PropertyValueFactory<AggregatedStationRoute, String>(""));
        stationRoutesColumn.setMinWidth(60);

        Callback<TableColumn<AggregatedStationRoute, String>, TableCell<AggregatedStationRoute, String>> cellFactoryRouteCBBoxColumn =
                new Callback<TableColumn<AggregatedStationRoute, String>, TableCell<AggregatedStationRoute, String>>() {
            @Override
            public TableCell<AggregatedStationRoute, String> call(final TableColumn<AggregatedStationRoute, String> param) {
                final TableCell<AggregatedStationRoute, String> cell = new TableCell<AggregatedStationRoute, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        if (empty) {
                            setGraphic(null);
                        }
                        else {
                            ComboBox<StationRoute> stationRouteComboBox = new ComboBox<StationRoute>();

                            ObservableList<StationRoute> stationRouteItems = FXCollections.<StationRoute>observableArrayList();
                            stationRouteItems.addAll(pathTableView.getItems().get(getIndex()).getStationRoutes());
                            stationRouteComboBox.setItems(stationRouteItems);
                            stationRouteComboBox.setPadding(new Insets(-1, 0, -1, 0));

                            stationRouteComboBox.valueProperty().addListener(new ChangeListener<StationRoute>() {
                            	@Override public void changed(ObservableValue<? extends StationRoute> ov, StationRoute oldV, StationRoute newV) {
                            		stationRoutesInPath[getIndex()] = newV;
                                }
                            });

                            stationRouteComboBox.getSelectionModel().select(0);
                            setGraphic(stationRouteComboBox);
                        }
                    }
                };

                return cell;
            }
        };

        stationRoutesColumn.setCellFactory(cellFactoryRouteCBBoxColumn);
        pathTableView.getColumns().addAll(stationColumn, stationRoutesColumn);
        pathTableView.setItems(aggregateRouteItems);
    }

    @FXML
    public void onCreateTrainGroup() {
    	this.updateTrainGroupUI(null);
		this.trainGroupTableView.getSelectionModel().select(-1);
		this.trainGroup = null;
    }

    @FXML
    public void onDeleteTrainGroup() {
    	if (this.trainGroup != null) {
			this.trainGroupTableView.getItems().remove(this.trainGroup);
			this.schedulingEntry.getUtilities().getTimeTableServiceUtility().getTimetableService().removeTrainGroup(this.trainGroup);
			this.trainGroup = null;
			this.updateTrainGroupUI(null);
		}
    }

    @FXML
    public void onSaveTimetable() {
    	ObservableList<TrainGroup> trainGroupItems = FXCollections.<TrainGroup>observableArrayList();
    	trainGroupItems.addAll(this.schedulingEntry.getUtilities().getTimeTableServiceUtility().getTimetableService().findAllTrainGroups());
    	this.trainGroupTableView.setItems(trainGroupItems);

    	this.updateTrainGroupUI(this.trainGroup);
    	TimetableWriter writer = TimetableWriter.getInstance(this.schedulingEntry.getUtilities().getTimeTableServiceUtility(),
    			this.path + "\\timetable\\timetable.railml");
    	try {
			writer.saveTimetable(null);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    @FXML
    public void onCreateTrainRun() {
    	int fictiveTime = 0;
    	List<EntryInfo> templateEntries = new ArrayList<EntryInfo>();
    	for (int i = 0 ; i< this.pathTableView.getItems().size(); i++) {
    		StationRoute stationRoute = this.stationRoutesInPath[i];
    		EntryInfo timetableEntry = this.schedulingEntry.createEntry(
    				stationRoute.getStation().getName(),
    				stationRoute.getOperationalPoints().get(0).getTrackId(),
        			stationRoute.getName(),
                    fictiveTime, 1e-6, 1e-6);
    		templateEntries.add(timetableEntry);
    	}

    	TrainDefinition trainDef = this.trainDefCBBox.getSelectionModel().getSelectedItem();
    	int[] min_runing_time_seconds = this.schedulingEntry.getMinRunningTime(templateEntries, trainDef.getCode());
    	String[] strTime = this.startTimeTextField.getText().split(":");
    	Time startTime = Time.getInstance(Integer.parseInt(strTime[0]),
    			Integer.parseInt(strTime[1]), Integer.parseInt(strTime[2]));
    	String[] strInterval = this.intervalTextField.getText().split(":");
    	Duration interval = Duration.fromTotalSecond(Integer.parseInt(strInterval[0]) * 3600 +
    			Integer.parseInt(strInterval[1]) * 60 + Integer.parseInt(strInterval[2]));

    	int startTrainNum = Integer.parseInt(this.startNumTextField.getText());
    	int numOfTrains = Integer.parseInt(this.numOfTrainsTextField.getText());
    	ObservableList<Trip> tripItems = FXCollections.<Trip>observableArrayList();

    	if (this.trainGroup == null) {
    		TrainGroup inTrainGroup = TrainGroup.create(this.nameTextField.getText(), this.despTextField.getText(),
    				this.fromCBBox.getValue(), this.toCBBox.getValue(),
    				trainDef,
    				startTime, startTime.add(Duration.fromTotalSecond(interval.getTotalSeconds() * (numOfTrains - 1))), numOfTrains);
    		this.trainGroup = this.schedulingEntry.getUtilities().getTimeTableServiceUtility().getTimetableService().storeTrainGroup(inTrainGroup);
    	}

    	for (int idx = 0; idx < numOfTrains; idx++) {
    		int trainNum = startTrainNum + idx*2;

            List<EntryInfo> tripEntries = new ArrayList<EntryInfo>();
            int depart_time = startTime.getHour()*3600 + startTime.getMinute()*60 + startTime.getSecond();
            for (int i = 0; i < templateEntries.size(); i++) {
                EntryInfo templateEntry = templateEntries.get(i);
                int dwellTime = 30;
                int minDwellTime = 30;
                int bufferTime = 60; // TODO

                depart_time += min_runing_time_seconds[i] * 1.1; // recovery time
                if (i > 0) {
                    depart_time += dwellTime;
                    depart_time += bufferTime;
                }

                EntryInfo tripEntry = this.schedulingEntry.createEntry(templateEntry.getStationId(),
                        templateEntry.getTrackId(),
                        templateEntry.getStationRouteId(),
                        depart_time,
                        dwellTime,
                        minDwellTime);

                tripEntries.add(tripEntry);
            }

            Trip trip = this.schedulingEntry.createTrip(tripEntries, this.nameTextField.getText() + trainNum, trainDef.getCode());
            startTime = startTime.add(interval);
            tripItems.add(trip);
            this.trainGroup.getTripList().add(trip);
    	}

    	this.tripsTableView.setItems(tripItems);
    }
}
