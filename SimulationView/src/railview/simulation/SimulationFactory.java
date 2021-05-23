package railview.simulation;

import java.util.ArrayList;
import java.nio.file.Path;

import javafx.application.Platform;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SingleSimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railapp.units.Time;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;

public class SimulationFactory {
	public static SimulationFactory getInstance(
			Path infraPath,	Path rollingstockPath, Path timetablePath, boolean isCoreModelFormat) {
		IInfrastructureServiceUtility infraServiceUtility =
			InfrastructureReader.getInstance(infraPath).initialize(isCoreModelFormat);

		IRollingStockServiceUtility rollingStockServiceUtility =
			RollingStockReader.getRailSys7Instance(rollingstockPath).initialize(isCoreModelFormat);;

		ITimetableServiceUtility timeTableServiceUtility =
			TimetableReader.getRailSys7Instance(
			timetablePath, infraServiceUtility, rollingStockServiceUtility).initialize(isCoreModelFormat);

		SingleSimulationManager simulator = SingleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility, timeTableServiceUtility);

		//simulator.setTimePeriod(Time.getInstance(0, 57, 0), Time.getInstance(1, 0, 0));
		//simulator.getDispatchingSystem().getDispCommunication().start();

		return new SimulationFactory(
				simulator, infraServiceUtility, rollingStockServiceUtility, timeTableServiceUtility);
	}

	public SingleSimulationManager getSimulator() {
		return simulator;
	}

	public IInfrastructureServiceUtility getInfraServiceUtility() {
		return infraServiceUtility;
	}

	public IRollingStockServiceUtility getRollingStockServiceUtility() {
		return rollingStockServiceUtility;
	}

	public ITimetableServiceUtility getTimeTableServiceUtility() {
		return timeTableServiceUtility;
	}

	public Time getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Time time) {
		this.updateTime = time;
	}

	private SimulationFactory(SingleSimulationManager simulator,
			IInfrastructureServiceUtility infraUtil,
			IRollingStockServiceUtility rsUtil,
			ITimetableServiceUtility ttUtil) {
		this.simulator = simulator;
		this.infraServiceUtility = infraUtil;
		this.rollingStockServiceUtility = rsUtil;
		this.timeTableServiceUtility = ttUtil;
		this.updateUIs = new ArrayList<ISimulationUpdateUI>();
	}

	public void startSimulation() {
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

	public void pauseSimulation() {
		if (! isOnPauseCommand) {
			isOnPauseCommand = true;
		}
	}

	public void stopSimulation() {
		if (this.simulator != null) {
			this.simulator.reset();
			this.simulator.getDispatchingSystem().close();
			this.isOnStopCommand = true;
		}
	}

	public void registerUpdateUI(ISimulationUpdateUI updateUI) {
		this.updateUIs.add(updateUI);
	}

	protected void updateUI() {
		for (ISimulationUpdateUI ui : this.updateUIs) {
			ui.updateUI();
		}
	}

	protected void setTime(boolean isReplay) {
		for (ISimulationUpdateUI ui : this.updateUIs) {
			ui.setTime(isReplay);
		}
	}

	protected int UIPause = 100;
	protected Time updateTime = Time.getInstance(0, 0, 0);

	protected SingleSimulationManager simulator;
	private ArrayList<ISimulationUpdateUI> updateUIs;

	protected IInfrastructureServiceUtility infraServiceUtility;
	protected IRollingStockServiceUtility rollingStockServiceUtility;
	protected ITimetableServiceUtility timeTableServiceUtility;

	private boolean isOnPauseCommand = false;
	private boolean isOnStopCommand = false;
	private boolean isUpdating = false;

	private Thread updateThread = null;
	private Thread simulationThread = null;

	class Updater {
		SimulationFactory factory = null;

		Updater(SimulationFactory factory) {
			this.factory = factory;
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
							factory.updateUI();
						} // public void run() {
					}); // Platform.runLater(new Runnable() {

					factory.setTime(isReplay);
				} // if (!isPause)

				try {
					Thread.sleep(UIPause);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} // while ((simulator.getTerminatedTime() == null || time.compareTo(simulator.getTerminatedTime()) < 0))
		} // periodicalUpdate(boolean isReplay)
	}

	public interface ISimulationUpdateUI {
		void updateUI();
		void setTime(boolean isReplay);
	}
}
