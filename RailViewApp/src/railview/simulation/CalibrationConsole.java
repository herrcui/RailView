package railview.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.TrainClass;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.MultipleSimulationManager;
import railapp.simulation.SingleSimulationManager;
import railapp.simulation.calibration.Calibrator;
import railapp.simulation.disturbances.DisturbanceDefinition;
import railapp.simulation.disturbances.DisturbanceType;
import railapp.simulation.train.TrainClassGroup;
import railapp.timetable.service.ITimetableServiceUtility;
import railapp.units.Time;
import railapp.util.random.DistributionDefinition;

public class CalibrationConsole {

	@SuppressWarnings("null")
	public static void main(String[] args) {
		//IInfrastructureServiceUtility infraServiceUtility = InfrastructureReader.getInstanceHannover().initialize();
		IInfrastructureServiceUtility infraServiceUtility = null;

		// Rollilngstock
		//IRollingStockServiceUtility rollingStockServiceUtility = RollingStockReader.getInstanceHannover().initialize();
		IRollingStockServiceUtility rollingStockServiceUtility = null;

		// Timetable
		// ITimetableServiceUtility timeTableServiceUtility = TimetableReader.getInstanceHannover(
		//		infraServiceUtility, rollingStockServiceUtility, network).initialize();
		ITimetableServiceUtility timeTableServiceUtility = null;

		Time from = Time.getInstance(7, 0, 0);
		Time to = Time.getInstance(8, 0, 0);

		System.out.println("... Single simulation started");
		SingleSimulationManager timetableSimulator = SingleSimulationManager.getInstance(
				infraServiceUtility, rollingStockServiceUtility, timeTableServiceUtility);
		timetableSimulator.setTimePeriod(from, to);
		timetableSimulator.run();
		System.out.println("... Single simulation accomplished");
		timetableSimulator.getDispatchingSystem().close();

		List<TrainClassGroup> trainClassGroups =
			buildTrainClassGroups(rollingStockServiceUtility.getRollingStockService().findAllTrainClasses());

		List<DisturbanceDefinition> distrubanceDefinitions = createDistributionDefinitions(trainClassGroups);

		System.out.println("... Multipl simulation started");
		MultipleSimulationManager multipleSimulator = MultipleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility,
				distrubanceDefinitions,
				50); // TODO size
		multipleSimulator.start(from, to);
		System.out.println("... Multipl simulation accomplished");

		List<DisturbanceDefinition> initialDefinitions = initializeDistributionDefinitions(trainClassGroups);

		Calibrator calibrator = Calibrator.getInstance(
				infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility,
				initialDefinitions,
				multipleSimulator.getLoggers());

		calibrator.calibrate(from, to, 0.001);
	}

	private static List<TrainClassGroup> buildTrainClassGroups(Collection<TrainClass> trainClassList) {
		List<TrainClassGroup> classGroups = new ArrayList<TrainClassGroup>();
		HashSet<TrainClass> classSet_S = new HashSet<TrainClass>();
		HashSet<TrainClass> classSet_NGz_FGz_Gz_Lz = new HashSet<TrainClass>();
		HashSet<TrainClass> classSet_NRz = new HashSet<TrainClass>();
		HashSet<TrainClass> classSet_FRz_Rz =  new HashSet<TrainClass>();

		for (TrainClass trainClass : trainClassList) {
			if (trainClass.getClassName().equals("S")) {
				classSet_S.add(trainClass);
			}

			if (trainClass.getClassName().equals("NGz") || trainClass.getClassName().equals("FGz") || trainClass.getClassName().equals("Lz") || trainClass.getClassName().equals("Gz")) {
				classSet_NGz_FGz_Gz_Lz.add(trainClass);
			}

			if (trainClass.getClassName().equals("NRz")) {
				classSet_NRz.add(trainClass);
			}

			if (trainClass.getClassName().equals("FRz") || trainClass.getClassName().equals("Rz")) {
				classSet_FRz_Rz.add(trainClass);
			}
		}

		classGroups.add(new TrainClassGroup("S", classSet_S));
		classGroups.add(new TrainClassGroup("Gz", classSet_NGz_FGz_Gz_Lz));
		classGroups.add(new TrainClassGroup("NRz", classSet_NRz));
		classGroups.add(new TrainClassGroup("FRz", classSet_FRz_Rz));

		return classGroups;
	}

	private static List<DisturbanceDefinition> createDistributionDefinitions(List<TrainClassGroup> trainClassGroups) {
		List<DisturbanceDefinition> distrubanceDefinitions = new ArrayList<DisturbanceDefinition>();
		for (TrainClassGroup trainClassGroup : trainClassGroups) {
			if (trainClassGroup.getName().equals("S")) {
				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.RUNNINGTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));


				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));
			}

			if (trainClassGroup.getName().equals("Gz")) {
				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.RUNNINGTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

			}

			if (trainClassGroup.getName().equals("NRz")) {
				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.RUNNINGTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

			}

			if (trainClassGroup.getName().equals("FRz")) {
				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.RUNNINGTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

				distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
						new ArrayList<>(Arrays.asList((Double) 0.05))),
					trainClassGroup,
					null));

			}
		}

		return distrubanceDefinitions;
	}

	private static List<DisturbanceDefinition> initializeDistributionDefinitions(List<TrainClassGroup> trainClassGroups) {
		List<DisturbanceDefinition> distrubanceDefinitions = new ArrayList<DisturbanceDefinition>();

		for (TrainClassGroup trainClassGroup : trainClassGroups) {
			distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.RUNNINGTIME_EXTENSION,
				DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
					new ArrayList<>(Arrays.asList((Double) 0.05))),
				trainClassGroup,
				null));

			distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.DWELLTIME_EXTENSION,
				DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
					new ArrayList<>(Arrays.asList((Double) 0.05))),
				trainClassGroup,
				null));

			distrubanceDefinitions.add(DisturbanceDefinition.create(DisturbanceType.ENTRY_DELAY,
				DistributionDefinition.createDistributionDefinition("ExponentialDistribution",
					new ArrayList<>(Arrays.asList((Double) 0.05))),
				trainClassGroup,
				null));

		}

		return distrubanceDefinitions;
	}
}
