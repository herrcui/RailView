package railview.editor.timetable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
import railapp.rollingstock.dto.TrainDefinition;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.entries.EntryUtilities;
import railapp.simulation.entries.SchedulingEntry;
import railapp.timetable.dto.EntryInfo;
import railapp.timetable.scheduling.TrainGroupStationSequence;
import railapp.timetable.service.ITimetableServiceUtility;

public class TimetableEditorPaneController {
	@FXML
	private AnchorPane timetableEditorPaneRoot, schedulingRoot, tripRoot;

	@FXML
	private Label folderLabel;

	@FXML
	private ComboBox<Station> fromCBBox, toCBBox;

	@FXML
	private ComboBox<TrainDefinition> trainDefCBBox;

	@FXML
	private TableView<AggregatedStationRoute> pathTableView;

	private SchedulingEntry schedulingEntry;

	private String path;

	public void initialize() {

	}

	private void loadData() {
		IInfrastructureServiceUtility infraServiceUtility = new railapp.infrastructure.service.ServiceUtility();
		IRollingStockServiceUtility rollingStockServiceUtility = new railapp.rollingstock.service.ServiceUtility();

		InfrastructureParser.getInstance(infraServiceUtility, path + "\\infrastructure").parse();
		RollingStockParser.getInstance(rollingStockServiceUtility, path + "\\rollingstock").parse();

		this.schedulingEntry = SchedulingEntry.getInstance(infraServiceUtility, rollingStockServiceUtility);

		ObservableList<TrainDefinition> trainDefData = FXCollections.observableArrayList();
		trainDefData.addAll(rollingStockServiceUtility.getRollingStockService().findTrainDefinitionsByClass(null));
		this.trainDefCBBox.setItems(trainDefData);

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
		Station fromStation = (Station) this.fromCBBox.getValue();
		Station toStation = (Station) this.toCBBox.getValue();

		List<TrainGroupStationSequence> TGSequences = this.schedulingEntry.findTrainGroupStationSequences(fromStation.getName(), toStation.getName());
		for (TrainGroupStationSequence sequence : TGSequences) {
            System.out.println(sequence);
            System.out.println(sequence.getNumberOfAlternatives());
        }

        TrainGroupStationSequence anySequence = TGSequences.get(0);
        List<AggregatedStationRoute> anyPath = anySequence.sampleAlternative();

        int fictiveTime = 0;
        List<EntryInfo> entries = new ArrayList<EntryInfo>();

        for (int i = 0; i < anyPath.size(); i++) {
            AggregatedStationRoute aggregatedRoute = anyPath.get(i);

            StationRoute anyStationRoute = aggregatedRoute.getStationRoutes().get(0);
            String stationName = anyStationRoute.getStation().getName();
            String stationRouteName = anyStationRoute.getName();

            String stop_name = this.schedulingEntry.findOperationalPointsByStationRoute(stationName, stationRouteName).get(0).getTrackId();

            EntryInfo timetableEntry = this.schedulingEntry.createEntry(stationName, stop_name, stationRouteName,
                    fictiveTime, 1e-6, 1e-6);
            System.out.println(timetableEntry);
            entries.add(timetableEntry);

            fictiveTime += 600;
        }

        ObservableList<AggregatedStationRoute> aggregateRouteitems = FXCollections.<AggregatedStationRoute>observableArrayList();
        aggregateRouteitems.addAll(anyPath);

        TableColumn<AggregatedStationRoute, String> aggregateRouteColumn = new TableColumn<AggregatedStationRoute, String>("routes");
        aggregateRouteColumn.setCellValueFactory(new PropertyValueFactory<AggregatedStationRoute, String>(""));
        aggregateRouteColumn.setMinWidth(60);

        Callback<TableColumn<AggregatedStationRoute, String>, TableCell<AggregatedStationRoute, String>> cellFactoryRouteCBBoxColumn =
        		new Callback<TableColumn<AggregatedStationRoute, String>, TableCell<AggregatedStationRoute, String>>() {
        	@Override
            public TableCell<AggregatedStationRoute, String> call(final TableColumn<AggregatedStationRoute, String> param) {
        		final TableCell<AggregatedStationRoute, String> cell = new TableCell<AggregatedStationRoute, String>() {
        			@Override
                    public void updateItem(String item, boolean empty) {
        				super.updateItem(item, empty);

        				if (empty)
                        {
                            setGraphic(null);
                        }
                        else
                        {
                        	ComboBox<StationRoute> stationRouteComboBox = new ComboBox<StationRoute>();

                        	ObservableList<StationRoute> stationRouteItems = FXCollections.<StationRoute>observableArrayList();
                        	stationRouteItems.addAll(pathTableView.getItems().get(getIndex()).getStationRoutes());
                        	stationRouteComboBox.setItems(stationRouteItems);

                            stationRouteComboBox.setPadding(new Insets(-1, 0, -1, 0));
                            stationRouteComboBox.setMinWidth(aggregateRouteColumn.getWidth() - 6);
                            stationRouteComboBox.setPrefWidth(aggregateRouteColumn.getWidth() - 6);
                            stationRouteComboBox.setMaxWidth(aggregateRouteColumn.getWidth() - 6);

                            setGraphic(stationRouteComboBox);
                        }
        			}
        		};

        		return cell;
            }
        };

        aggregateRouteColumn.setCellFactory(cellFactoryRouteCBBoxColumn);
        pathTableView.getColumns().addAll(aggregateRouteColumn);
        pathTableView.setItems(aggregateRouteitems);
	}
}
