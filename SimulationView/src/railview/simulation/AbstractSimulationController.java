package railview.simulation;

import javafx.application.Platform;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SingleSimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railapp.units.Time;

public abstract class AbstractSimulationController {
	protected void startSimulation() {
		if (isOnPauseCommand) {
			isOnPauseCommand = false;
		}

		if (isOnStopCommand) {
			isOnStopCommand = false;
		}

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

	protected void pauseSimulation() {
		if (! isOnPauseCommand) {
			isOnPauseCommand = true;
		}
	}

	protected void stopSimulation() {
		if (this.simulator != null) {
			this.simulator.stop();
			this.isOnStopCommand = true;
		}
	}

	protected abstract void updateUI();

	protected abstract void setTime(boolean isReplay);

	protected int UIPause = 100;
	protected Time updateTime = Time.getInstance(0, 0, 0);

	protected SingleSimulationManager simulator;

	protected IInfrastructureServiceUtility infraServiceUtility;
	protected IRollingStockServiceUtility rollingStockServiceUtility;
	protected ITimetableServiceUtility timeTableServiceUtility;

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

				if (simulator.getStatus() == SingleSimulationManager.TERMINATED && 
						updateTime.compareTo(simulator.getTime()) > 0) {
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
