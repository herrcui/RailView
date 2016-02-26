package railview.simulation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.dto.TrainClass;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.MultipleSimulationManager;
import railapp.simulation.SingleSimulationManager;
import railapp.simulation.disturbances.DistributionDefinition;
import railapp.simulation.disturbances.DisturbanceDefinition;
import railapp.simulation.disturbances.DisturbanceType;
import railapp.simulation.disturbances.TrainClassGroup;
import railapp.simulation.logs.DelayLogger;
import railapp.timetable.service.ITimetableServiceUtility;
import railapp.units.Duration;
import railapp.units.Time;
import raillapp.simulation.calibration.Calibrator;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;

public class CalibrationConsole {

	public static void main(String[] args) {
		IInfrastructureServiceUtility infraServiceUtility = InfrastructureReader.getInstanceHannover().initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();
		
		// Rollilngstock
		IRollingStockServiceUtility rollingStockServiceUtility = RollingStockReader.getInstanceHannover().initialize();
		
		// Timetable
		ITimetableServiceUtility timeTableServiceUtility = TimetableReader.getInstanceHannover(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();
		
		Time from = Time.getInstance(7, 0, 0);
		Time to = Time.getInstance(8, 0, 0);
		
		System.out.println("... Single simulation started");
		SingleSimulationManager timetableSimulator = SingleSimulationManager.getInstance(
				infraServiceUtility, rollingStockServiceUtility, timeTableServiceUtility);
			timetableSimulator.setTimePeriod(from, to);
			timetableSimulator.run();
		DelayLogger loggerWithoutDisturbance = timetableSimulator.getDelayLogger();
		System.out.println("... Single simulation accomplished");
		
		List<TrainClassGroup> trainClassGroups = 
			buildTrainClassGroups(rollingStockServiceUtility.getRollingStockService().findAllTrainClasses());
			
		List<DisturbanceDefinition> distrubanceDefinitions = createDistributionDefinitions(trainClassGroups);
		
		System.out.println("... Multipl simulation started");
		MultipleSimulationManager multipleSimulator = MultipleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility,
				distrubanceDefinitions,
				50); // TODO size
		multipleSimulator.start(from, to, loggerWithoutDisturbance);
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
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.RUNNINGTIME_EXTENSION,
						DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(30)),
						trainClassGroup,
						null));
				
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(15)),
					trainClassGroup,
					null));
				
				/*
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(15)),
					trainClass,
					null));
				*/
			}
			
			if (trainClassGroup.equals("Gz")) {
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.RUNNINGTIME_EXTENSION,
						DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(120)),
						trainClassGroup,
						null));
					
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(300)),
					trainClassGroup,
					null));
				/*
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(300)),
					trainClass,
					null));
				*/
			}
			
			if (trainClassGroup.getName().equals("NRz")) {
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.RUNNINGTIME_EXTENSION,
						DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(60)),
						trainClassGroup,
						null));
					
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(45)),
					trainClassGroup,
					null));
				/*
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(60)),
					trainClass,
					null));
				*/
			}
			
			if (trainClassGroup.getName().equals("FRz")) {
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.RUNNINGTIME_EXTENSION,
						DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(90)),
						trainClassGroup,
						null));
					
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.DWELLTIME_EXTENSION,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(60)),
					trainClassGroup,
					null));
				/*
				distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.ENTRY_DELAY,
					DistributionDefinition.getExpotentialInstance(0.10, Duration.fromTotalSecond(60)),
					trainClass,
					null));
				*/
			}
		}
			
		return distrubanceDefinitions;
	}
	
	private static List<DisturbanceDefinition> initializeDistributionDefinitions(List<TrainClassGroup> trainClassGroups) {
		List<DisturbanceDefinition> distrubanceDefinitions = new ArrayList<DisturbanceDefinition>();

		for (TrainClassGroup trainClassGroup : trainClassGroups) {
			distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.RUNNINGTIME_EXTENSION,
					DistributionDefinition.getExpotentialInstance(0.3, Duration.fromTotalSecond(600)),
					trainClassGroup,
					null));
			
			distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.DWELLTIME_EXTENSION,
				DistributionDefinition.getExpotentialInstance(0.3, Duration.fromTotalSecond(600)),
				trainClassGroup,
				null));
			/*
			distrubanceDefinitions.add(new DisturbanceDefinition(DisturbanceType.ENTRY_DELAY,
				DistributionDefinition.getExpotentialInstance(0, Duration.fromTotalSecond(0)),
				trainClass,
				null));
			*/
		}
			
		return distrubanceDefinitions;
	}
}