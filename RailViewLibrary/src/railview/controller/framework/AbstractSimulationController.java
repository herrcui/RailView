package railview.controller.framework;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import railapp.simulation.SimulationManager;
import railapp.units.Time;

public abstract class AbstractSimulationController {
	@FXML
	protected Button startButton;

	@FXML
	protected Button pauseButton;

	@FXML
	protected Button stopButton;

	@FXML
	final public void startSimulation() {
		if (isOnPauseCommand) {
			isOnPauseCommand = false;
		}

		if (isOnStopCommand) {
			isOnStopCommand = false;
		}

		this.startButton.setDisable(true);
		this.pauseButton.setDisable(false);
		this.stopButton.setDisable(false);

		if (!this.isUpdating) {
			if (this.simulationThread == null || !this.simulationThread.isAlive()) {
				this.simulationThread = new Thread(this.simulator);
				this.simulationThread.start();
			}

			if (this.updateThread == null || !this.updateThread.isAlive()) {
				this.updateThread = new Thread(() -> {
					(new Updater(this)).periodicalUpdate(false);
				});

				isUpdating = true;
				this.updateThread.start();
			}
		}
	}

	@FXML
	final public void pauseSimulation() {
		if (! isOnPauseCommand) {
			isOnPauseCommand = true;

			this.startButton.setDisable(false);
			this.pauseButton.setDisable(true);
			this.stopButton.setDisable(false);
		}
	}

	@FXML
	public void stopSimulation() {
		this.simulator.stop();

		this.isOnStopCommand = true;

		this.startButton.setDisable(false);
		this.pauseButton.setDisable(true);
		this.stopButton.setDisable(true);

		try {
			this.simulationThread.join();
			this.updateThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		};
	}

	final public void setSimulationManager(SimulationManager simulator) {
		this.simulator = simulator;
	}

	protected abstract void updateUI();

	protected abstract void setTime(boolean isReplay);

	protected int UIPause = 100;
	protected Time updateTime = Time.getInstance(0, 0, 0);

	private SimulationManager simulator;

	private boolean isOnPauseCommand = false;
	private boolean isOnStopCommand = false;
	private boolean isUpdating = false;

	private Thread updateThread = null;
	private Thread simulationThread = null;

	class Updater {
		AbstractSimulationController controller = null;

		Updater(AbstractSimulationController controller) {
			this.controller = controller;
		}

		void periodicalUpdate(boolean isReplay) {
			while (isUpdating) {
				if (isOnStopCommand) {
					isUpdating = false;
					break;
				}

				if (simulator.getStatus() == SimulationManager.TERMINATED && updateTime.compareTo(simulator.getTime()) > 0) {
					isUpdating = false;
					break;
				}

				if (!isOnPauseCommand) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							controller.updateUI();
						} // public void run() {
					}); // Platform.runLater(new Runnable() {

					controller.setTime(isReplay);
				} // if (!isPause)

				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // while ((simulator.getTerminatedTime() == null || time.compareTo(simulator.getTerminatedTime()) < 0))
		} // periodicalUpdate(boolean isReplay)
	}
}
