package railview.railmodel.infrastructure.railsys7;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import railapp.infrastructure.dto.Network;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.parser.railsys7.timetable.TimetableParser;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railapp.timetable.service.ITimetableServiceUtility;

public class TimetableReader {
	public static TimetableReader getRailSys7Instance(Path path,
			IInfrastructureServiceUtility infraServiceUtility,
			IRollingStockServiceUtility rollingStockServiceUtility,
			Network network) {
		return new TimetableReader(path, infraServiceUtility, rollingStockServiceUtility, network);
	}

	private TimetableReader(Path path,
			IInfrastructureServiceUtility infraServiceUtility,
			IRollingStockServiceUtility rollingStockServiceUtility,
			Network network) {
		this.path = path;
		this.infraServiceUtility = infraServiceUtility;
		this.rollingStockServiceUtility = rollingStockServiceUtility;
		this.network = network;
	}

	public ITimetableServiceUtility initialize() {
		ITimetableServiceUtility timeTableServiceUtility =
			new railapp.timetable.service.ServiceUtility();

		TimetableParser timeTableParser = TimetableParser.getInstance(
				path, timeTableServiceUtility, infraServiceUtility, rollingStockServiceUtility, network);
		timeTableParser.parse();
		return timeTableServiceUtility;
	}

	private Path path;
	private IInfrastructureServiceUtility infraServiceUtility;
	private IRollingStockServiceUtility rollingStockServiceUtility;
	private Network network;
}
