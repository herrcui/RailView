package railview.editor.infrastructure;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Network;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.units.Coordinate;
import railapp.util.CrudCSV;

public class LineStationEditorPaneController {
	private IInfrastructureServiceUtility serviceUtility;
	private Network network;
	private String path = null;

	private CrudCSV lineTb, stationTb;
	private Line line;

	private Station station;

	@FXML
	private TextField lineNameText, lineDespText, stationNameText, stationDespText, xText, yText;

	@FXML
	private TableView<Line> lineTableView;

	@FXML
	private TableView<Station> stationTableView;

	void loadData(IInfrastructureServiceUtility serviceUtility, String path) {
		this.serviceUtility = serviceUtility;
		this.network = (Network) serviceUtility.getNetworkService().allNetworks().toArray()[0];
		this.path = path;

		this.lineTb = new CrudCSV(this.path + "\\line.csv", ";");
		this.stationTb = new CrudCSV(this.path + "\\station.csv", ";");

		this.updateLineTableView(null);
		this.updateStationTableView(null);

		lineTableView.getSelectionModel().selectFirst();
		stationTableView.getSelectionModel().selectFirst();
	}

	@SuppressWarnings("unchecked")
	public void initialize() {
		TableColumn<Line, String> lindNameColumn = new TableColumn<Line, String>("Name");
		lindNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Line, String> lindDespColumn = new TableColumn<Line, String>("Description");
		lindDespColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

		lineTableView.getColumns().addAll(lindNameColumn, lindDespColumn);

		lineTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Line>() {
			@Override
			public void onChanged(Change<? extends Line> change) {
				if (change.getList().size() > 0) {
				    line = change.getList().get(0);

				    lineNameText.setText(line == null? "" : line.getName());
				    lineDespText.setText(line == null? "" : line.getDescription());
				}
			}
		});

		TableColumn<Station, String> stationNameColumn = new TableColumn<Station, String>("Name");
		stationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Station, String> stationDespColumn = new TableColumn<Station, String>("Description");
		stationDespColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

		stationTableView.getColumns().addAll(stationNameColumn, stationDespColumn);

		stationTableView.getSelectionModel().getSelectedItems().addListener(new ListChangeListener<Station>() {
			@Override
			public void onChanged(Change<? extends Station> change) {
				if (change.getList().size() > 0) {
				    station = change.getList().get(0);

				    stationNameText.setText(station == null? "" : station.getName());
				    stationDespText.setText(station == null? "" : station.getDescription());
				    xText.setText(station == null? "" : Double.toString(station.getCoordinate().getX()));
				    yText.setText(station == null? "" : Double.toString(station.getCoordinate().getY()));
				}
			}
		});
	}

	private void updateLineTableView(Line line) {
		lineTableView.getItems().removeAll(lineTableView.getItems());
		lineTableView.getItems().addAll(this.serviceUtility.getNetworkService().allLines());

		if (line != null) {
			lineTableView.getSelectionModel().select(line);
		} else {
			lineTableView.getSelectionModel().selectFirst();
		}
	}

	private void updateStationTableView(Station station) {
		stationTableView.getItems().removeAll(stationTableView.getItems());
		stationTableView.getItems().addAll(this.serviceUtility.getNetworkService().allStations());

		if (station != null) {
			stationTableView.getSelectionModel().select(station);
		} else {
			stationTableView.getSelectionModel().selectFirst();
		}
	}

	@FXML
	private void onNewLine(ActionEvent event) {
		// only applied in user interface
		this.lineNameText.setText("");
		this.lineDespText.setText("");

		this.lineTableView.getSelectionModel().select(-1);
		this.line = null;
	}

	@FXML
	private void onDeleteLine(ActionEvent event) {
		if (line != null) {
			this.lineTb.delete(line.getName());
			this.serviceUtility.getNetworkService().removeLine(line);
			this.line = null;
			this.updateLineTableView(null);
		}
	}

	@FXML
	private void onSaveLine(ActionEvent event) {
		if (this.line != null) {
			// update an existing line
			this.line.setName(this.lineNameText.getText());
			this.line.setDescription(this.lineDespText.getText());

			String[] values = {Integer.toString(line.getId()), line.getName(), line.getDescription()};
			this.lineTb.alter(Integer.toString(line.getId()), values);
		} else {
			// save a new line
			if (this.lineNameText.getText().trim().length() > 0) {
				Line inLine = this.network.createLine(null, this.lineNameText.getText(), this.lineDespText.getText());
				this.line = this.serviceUtility.getNetworkService().storeLine(inLine);

				String[] values = {Integer.toString(line.getId()), line.getName(), line.getDescription()};
				this.lineTb.alter(Integer.toString(line.getId()), values);
			}
			else {
				return;
			}
		}

		this.updateLineTableView(this.line);
	}

	@FXML
	private void onNewStation(ActionEvent event) {
		// only applied in user interface
		this.stationNameText.setText("");
		this.stationDespText.setText("");

		this.stationTableView.getSelectionModel().select(-1);
		this.station = null;
	}

	@FXML
	private void onDeleteStation(ActionEvent event) {
		if (station != null) {
			this.stationTb.delete(line.getName());
			this.serviceUtility.getNetworkService().removeStation(station);
			this.station = null;
			this.updateStationTableView(null);
		}
	}

	@FXML
	private void onSaveStation(ActionEvent event) {
		if (this.station != null) {
			// update an existing station
			this.station.setName(this.stationNameText.getText());
			this.station.setDescription(this.stationDespText.getText());
			this.station.setCoordinate(Coordinate.fromXY(Double.parseDouble(this.xText.getText()),
					Double.parseDouble(this.yText.getText())));

			String[] values = {Integer.toString(station.getId()),
					station.getName(),
					station.getDescription(),
					Double.toString(station.getCoordinate().getX()),
					Double.toString(station.getCoordinate().getY())};
			this.stationTb.alter(Integer.toString(station.getId()), values);
		} else {
			// save a new station
			if (this.stationNameText.getText().trim().length() > 0) {
				Station inStation = this.network.createStation(null,
						this.stationNameText.getText(),
						this.stationDespText.getText(),
						Coordinate.fromXY(Double.parseDouble(this.xText.getText()),
								Double.parseDouble(this.yText.getText())));

				this.station = this.serviceUtility.getNetworkService().storeStation(inStation);

				String[] values = {Integer.toString(station.getId()),
						station.getName(),
						station.getDescription(),
						Double.toString(station.getCoordinate().getX()),
						Double.toString(station.getCoordinate().getY())};
				this.stationTb.alter(Integer.toString(station.getId()), values);
			}
			else {
				return;
			}
		}

		this.updateStationTableView(this.station);
	}
}
