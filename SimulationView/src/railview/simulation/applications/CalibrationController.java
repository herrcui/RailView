package railview.simulation.applications;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import railapp.simulation.calibration.deterministic.Calibrator;
import railview.simulation.SimulationFactory;

public class CalibrationController {
	@FXML
	private TextField textPath, textRound, textA, textB, textC, textAcc, textBrake;

	@FXML
	private Button calibrateButton;

	@FXML
	private Label labelResult;

	@FXML
	private TextArea textLog;

	private SimulationFactory simulationFactory;

	private Calibrator calibrator;

	@FXML
	public void initialize() {

	}

	public void setSimulationFactory(SimulationFactory factory) {
		this.simulationFactory = factory;
	}

	@FXML
	private void onCalibrate() {
		this.textLog.clear();

		List<Double> parameters = new ArrayList<Double>();
        parameters.add(this.parseParameter(textA));
        parameters.add(this.parseParameter(textB));
        parameters.add(this.parseParameter(textC));
        parameters.add(this.parseParameter(textAcc));
        parameters.add(this.parseParameter(textBrake));

        this.calibrator = Calibrator.getInstance(
        		this.simulationFactory.getInfraServiceUtility(),
        		this.simulationFactory.getRollingStockServiceUtility(),
        		this.simulationFactory.getTimeTableServiceUtility(),
        		parameters,
        		this.textPath.getText(),
        		this.textLog,
        		Integer.parseInt(this.textRound.getText()));
        //this.calibrator.start();

        this.calibrator.run();
	}

	private double parseParameter(TextField text) {
		try {
			return Double.parseDouble(text.getText());
		} catch (Exception e) {
			return Double.MAX_VALUE;
		}
	}
}
