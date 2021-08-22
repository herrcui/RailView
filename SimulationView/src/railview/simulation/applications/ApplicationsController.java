package railview.simulation.applications;

import java.io.IOException;
import java.net.URL;

import railview.simulation.SimulationFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * The controller class of EditorPane.fxml. In the Pane, you can write and load
 * code on the left side and the outcome is on the right side.
 *
 */
public class ApplicationsController {
	@FXML
	private AnchorPane capacityOptRoot, capacityOptimizerPane;

	@FXML
	private AnchorPane pythonUIRoot, pythonUIPane;

	@FXML
	private AnchorPane calibrationRoot, calibrationPane;


	private CapacityOptimizerController capacityOptimizerController;

	private PythonUIController pythonUIController;

	private CalibrationController calibrationController;

	@FXML
	public void initialize() {
		try {
			FXMLLoader capacityOptLoader = new FXMLLoader();
			URL capacityOptLocation = CapacityOptimizerController.class
					.getResource("CapacityOptimizer.fxml");
			capacityOptLoader.setLocation(capacityOptLocation);
			capacityOptimizerPane = (AnchorPane) capacityOptLoader.load();
			this.capacityOptimizerController = capacityOptLoader.getController();

			this.capacityOptRoot.getChildren().add(capacityOptimizerPane);
			this.setAnchor(this.capacityOptimizerPane);

			FXMLLoader pythonUILoader = new FXMLLoader();
			URL pythonUILocation = PythonUIController.class.getResource("PythonUIPane.fxml");
			pythonUILoader.setLocation(pythonUILocation);
			pythonUIPane = (AnchorPane) pythonUILoader.load();
			this.pythonUIController = pythonUILoader.getController();

			this.pythonUIRoot.getChildren().add(pythonUIPane);
			this.setAnchor(this.pythonUIPane);

			FXMLLoader calibrationLoader = new FXMLLoader();
			URL calibrationLocation = PythonUIController.class.getResource("CalibrationPane.fxml");
			calibrationLoader.setLocation(calibrationLocation);
			calibrationPane = (AnchorPane) calibrationLoader.load();
			this.calibrationController = calibrationLoader.getController();

			this.calibrationRoot.getChildren().add(calibrationPane);
			this.setAnchor(this.calibrationPane);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setAnchor(Node component) {
		AnchorPane.setTopAnchor(component, 0.0);
		AnchorPane.setLeftAnchor(component, 0.0);
		AnchorPane.setRightAnchor(component, 0.0);
		AnchorPane.setBottomAnchor(component, 0.0);
	}

	public void setSimulationFactory(SimulationFactory factory) {
		this.capacityOptimizerController.setSimulationFactory(factory);
		this.pythonUIController.setSimulationFactory(factory);
		this.calibrationController.setSimulationFactory(factory);
	}
}
