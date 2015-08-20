package railview.simulation;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;

public class DoSimConsole {
	public static void main(String[] args) {
		IInfrastructureServiceUtility infraServiceUtility = InfrastructureReader.getInstance().initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();
		
		// Rollilngstock
		IRollingStockServiceUtility rollingStockServiceUtility = RollingStockReader.getInstance().initialize();
		
		// Timetable
		ITimetableServiceUtility timeTableServiceUtility = TimetableReader.getInstance(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();
		
		SimulationManager simulator = SimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility);
		
		simulator.run();
	}
}
