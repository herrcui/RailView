package railview.infrastructure;

import java.io.IOException;
import java.net.URL;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railview.infrastructure.editor.InfrastructureEditorController;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railsys.data.RailsysData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class InfraAppMain extends Application {

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Example of Infrastructure Elements");
		this.initRootLayout();
	}

	public static void main(String[] args) {
		infraServiceUtility = InfrastructureReader.getRailSys7Instance(
			RailsysData.class.getResource("\\var-2011")).initialize();
		
		launch(args);
	}

	private void initRootLayout() {

		InfrastructureEditorController controller = this
				.initInfrastructureEditor();
		
		if (this.rootLayout != null) {
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);

			controller.setInfrastructureServiceUtility(infraServiceUtility);
			primaryStage.show();
			
/**		    final double initWidth  = scene.getWidth();
		    final double initHeight = scene.getHeight();
		    Scale scale = new Scale();
		    scale.xProperty().bind(scene.widthProperty().divide(initWidth));
		    scale.yProperty().bind(scene.heightProperty().divide(initHeight));
		    scale.setPivotX(0); scale.setPivotY(0);
		    rootLayout.getTransforms().addAll(scale);
	**/
		}
	}

	private InfrastructureEditorController initInfrastructureEditor() {
		try {
			FXMLLoader loader = new FXMLLoader();
			URL location = InfrastructureEditorController.class
					.getResource("InfrastructureEditor.fxml");
			loader.setLocation(location);

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

	private static IInfrastructureServiceUtility infraServiceUtility;
}
