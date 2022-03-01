package railview.simulation.editor.infrastructure;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;
import railapp.parser.coremodel.infrastructure.InfrastructureParser;
import railapp.util.CrudCSV;

public class InfrastructureEditorPaneController {
	private IInfrastructureServiceUtility serviceUtility;
	private Network network;
	private boolean isUpdateCSV = true;
	private String path = "D:\\Temp\\PULSimEditor_Test\\infrastructure";

	private CrudCSV lineTb;
	private Line line;

	@FXML
	private TextField lineNameText, lineDespText;

	@FXML
	private TableView<Line> lineTableView;

	@SuppressWarnings("unchecked")
	public void initialize() {
		// TODO
		this.serviceUtility = new ServiceUtility();
		this.network = this.parseNetwork("MyNetworkName");

		if (isUpdateCSV) {
			InfrastructureParser parser = InfrastructureParser.getInstance(
					serviceUtility,
					this.path);
			parser.parse();

			this.lineTb = new CrudCSV(this.path + "\\line.csv", ";");
		}

		TableColumn<Line, Integer> lindIdColumn = new TableColumn<Line, Integer>("Id");
		lindIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

		TableColumn<Line, String> lindNameColumn = new TableColumn<Line, String>("Name");
		lindNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Line, String> lindDespColumn = new TableColumn<Line, String>("Description");
		lindDespColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

		lineTableView.getColumns().addAll(lindIdColumn, lindNameColumn, lindDespColumn);

		ObservableList<Line> lineData = FXCollections.observableArrayList();
		lineData.addAll(this.serviceUtility.getNetworkService().allLines());
		lineTableView.setItems(lineData);
	}

	private Network parseNetwork(String networkName) {
		Network network = Network.createNetwork(null, networkName);
		this.serviceUtility.getNetworkService().storeNetwork(network);
		return network;
	}

	@FXML
	private void onNewLine(ActionEvent event) {
		Line inLine = this.network.createLine(null, this.lineNameText.getText(), this.lineDespText.getText());
		this.line = this.serviceUtility.getNetworkService().storeLine(inLine);

		if (this.isUpdateCSV) {
			String[] values = {line.getId().toString(), line.getName(), line.getDescription()};
			this.lineTb.insert(line.getId(), values);
		}
	}

	@FXML
	private void onDeleteLine(ActionEvent event) {
		if (line != null) {
			this.lineTb.delete(line.getId());
			this.serviceUtility.getLineService().removeLine(line);
		}
	}

	@FXML
	private void onUpdateLine(ActionEvent event) {
		if (line != null) {
			this.line.setName(this.lineNameText.getText());
			this.line.setDescription(this.lineDespText.getText());
			String[] values = {line.getId().toString(), line.getName(), line.getDescription()};
			this.lineTb.alter(line.getId(), values);
		}
	}
}
