package railview.railmodel.infrastructure.railsys7;

import java.nio.file.Path;

import railapp.parser.railsys7.rollingstock.RollingStockParser;
import railapp.rollingstock.service.IRollingStockServiceUtility;

public class RollingStockReader {
	public static RollingStockReader getRailSys7Instance(Path path) {
		return new RollingStockReader(path);
	}

	private RollingStockReader(Path path) {
		this.path = path;
	}

	public IRollingStockServiceUtility initialize() {
		IRollingStockServiceUtility rollingStockServiceUtility =
			new railapp.rollingstock.service.ServiceUtility();

		RollingStockParser rollingStockParser = RollingStockParser.getInstance(
				this.path, rollingStockServiceUtility);
		rollingStockParser.parse();
		return rollingStockServiceUtility;
	}

	private Path path;
}
