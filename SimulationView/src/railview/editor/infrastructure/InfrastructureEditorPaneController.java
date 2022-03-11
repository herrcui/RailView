package railview.editor.infrastructure;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;
import railapp.parser.coremodel.infrastructure.InfrastructureParser;

public class InfrastructureEditorPaneController {
	@FXML
	private AnchorPane infrastructureEditorPaneRoot, lineStationEditorPane;

	@FXML
	private Label folderLabel;

	private IInfrastructureServiceUtility serviceUtility;
	private String path;

	private LineStationEditorPaneController lineStationEditorPaneController;

	public void initialize() {
		try {
			FXMLLoader lineStationEditorPaneLoader = new FXMLLoader();
			URL location = InfrastructureEditorPaneController.class.getResource("LineStationEditorPane.fxml");
			lineStationEditorPaneLoader.setLocation(location);
			lineStationEditorPane = (AnchorPane) lineStationEditorPaneLoader.load();

			this.lineStationEditorPaneController = lineStationEditorPaneLoader.getController();

			this.infrastructureEditorPaneRoot.getChildren().add(lineStationEditorPane);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadData() {
		this.serviceUtility = new ServiceUtility();
		InfrastructureParser parser = InfrastructureParser.getInstance(serviceUtility, path);
		if (!parser.parse()) {
			parser.createEmpty();

		}

		this.lineStationEditorPaneController.loadData(serviceUtility, path);
	}

	@FXML
	public void onSetWorkingFolder() {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File file = directoryChooser.showDialog(infrastructureEditorPaneRoot.getScene().getWindow());
		if (file != null) {
			folderLabel.setText(file.toString());
			this.path = file.toString() + "\\infrastructure";
			this.loadData();
		}
	}
}
