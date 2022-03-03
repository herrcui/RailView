package railview.simulation.editor.infrastructure;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class InfrastructureEditorPaneController {
	@FXML
	private AnchorPane infrastructureEditorPaneRoot, lineStationEditorPane;

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
}
