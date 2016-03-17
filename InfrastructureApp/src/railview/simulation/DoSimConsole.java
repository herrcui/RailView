package railview.simulation;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.simulation.SingleSimulationManager;
import railapp.timetable.service.ITimetableServiceUtility;
import railview.railmodel.infrastructure.railsys7.InfrastructureReader;
import railview.railmodel.infrastructure.railsys7.RollingStockReader;
import railview.railmodel.infrastructure.railsys7.TimetableReader;
import railview.railsys.data.RailsysData;

public class DoSimConsole {
	public static void main(String[] args) {
		IInfrastructureServiceUtility infraServiceUtility = InfrastructureReader.getRailSys7Instance(
				RailsysData.class.getResource("\\var-2011")).initialize();
		Network network = infraServiceUtility.getNetworkService().allNetworks().iterator().next();
		
		// Rollilngstock
		IRollingStockServiceUtility rollingStockServiceUtility = RollingStockReader.getInstance().initialize();
		
		// Timetable
		ITimetableServiceUtility timeTableServiceUtility = TimetableReader.getInstance(
				infraServiceUtility, rollingStockServiceUtility, network).initialize();
		
		SingleSimulationManager simulator = SingleSimulationManager.getInstance(infraServiceUtility,
				rollingStockServiceUtility,
				timeTableServiceUtility);
		
		simulator.run();
	}
}
