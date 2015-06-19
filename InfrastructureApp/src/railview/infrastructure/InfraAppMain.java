package railview.infrastructure;

import java.io.IOException;
import java.net.URL;

import railview.infrastructure.container.*;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.editor.InfrastructureEditorController;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class InfraAppMain extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Example of Infrastructure Elements");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		serviceUtility = InfrastructureReader.getInstance().initialize();
		launch(args);
	}

	private void initRootLayout() {

		InfrastructureEditorController controller = this
				.initInfrastructureEditor();
		// InfrastructureElementViewController controller =
		// this.initInfrastructureView();
		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			controller.setInfrastructureServiceUtility(serviceUtility);
			primaryStage.show();
		}
	}

	/**
	 * private InfrastructureElementViewController initInfrastructureView() {
	 * try { FXMLLoader loader = new FXMLLoader(); URL location =
	 * InfrastructureElementViewController
	 * .class.getResource("InfrastructureElementOverview.fxml");
	 * loader.setLocation(location); this.rootLayout = (AnchorPane)
	 * loader.load(); InfrastructureElementViewController controller =
	 * loader.getController(); return controller; } catch (IOException e) {
	 * e.printStackTrace(); return null; } }
	 **/

	private InfrastructureEditorController initInfrastructureEditor() {
		try {
			// Load person overview.
			FXMLLoader loader = new FXMLLoader();
			URL location = InfrastructureEditorController.class
					.getResource("InfrastructureEditor.fxml");
			loader.setLocation(location);
			// AnchorPane InfrastructureEditor = (AnchorPane) loader.load();
			this.rootLayout = (AnchorPane) loader.load();

			InfrastructureEditorController controller = loader.getController();
			return controller;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Stage primaryStage;
	private AnchorPane rootLayout;

	private static IInfrastructureServiceUtility serviceUtility;
}
