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
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.util.CrudCSV;

public class LineStationEditorPaneController {
	private IInfrastructureServiceUtility serviceUtility;
	private Network network;
	private String path = null;

	private CrudCSV lineTb;
	private Line line;

	@FXML
	private TextField lineNameText, lineDespText;

	@FXML
	private TableView<Line> lineTableView;

	void loadData(IInfrastructureServiceUtility serviceUtility, String path) {
		this.serviceUtility = serviceUtility;
		this.network = (Network) serviceUtility.getNetworkService().allNetworks().toArray()[0];
		this.path = path;

		this.lineTb = new CrudCSV(this.path + "\\line.csv", ";");

		this.updateLineTableView(null);

		lineTableView.getSelectionModel().selectFirst();
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
			this.lineTb.delete(line.getIdInString());
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

			String[] values = {line.getIdInString(), line.getName(), line.getDescription()};
			this.lineTb.alter(line.getIdInString(), values);
		} else {
			// save a new line
			if (this.lineNameText.getText().trim().length() > 0) {
				Line inLine = this.network.createLine(null, this.lineNameText.getText(), this.lineDespText.getText());
				this.line = this.serviceUtility.getNetworkService().storeLine(inLine);

				String[] values = {line.getIdInString(), line.getName(), line.getDescription()};
				this.lineTb.insert(line.getIdInString(), values);
			}
			else {
				return;
			}
		}

		this.updateLineTableView(this.line);
	}
}
