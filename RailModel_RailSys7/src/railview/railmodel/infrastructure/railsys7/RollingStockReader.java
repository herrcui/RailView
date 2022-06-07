package railview.railmodel.infrastructure.railsys7;

import java.nio.file.Path;

import railapp.rollingstock.service.IRollingStockServiceUtility;

public class RollingStockReader {
	public static RollingStockReader getRailSys7Instance(Path path) {
		return new RollingStockReader(path);
	}

	private RollingStockReader(Path path) {
		this.path = path;
	}

	public IRollingStockServiceUtility initialize(boolean isCoreModel) {
		IRollingStockServiceUtility rollingStockServiceUtility =
			new railapp.rollingstock.service.ServiceUtility();

		if (isCoreModel) {
			railapp.parser.coremodel.rollingstock.RollingStockParser rollingStockParser =
				railapp.parser.coremodel.rollingstock.RollingStockParser.getInstance(
					rollingStockServiceUtility, this.path.toString());
			rollingStockParser.parse();
			return rollingStockServiceUtility;
		} else {
			railapp.parser.railsys7.rollingstock.RollingStockParser rollingStockParser =
				railapp.parser.railsys7.rollingstock.RollingStockParser.getInstance(
					this.path, rollingStockServiceUtility);
			rollingStockParser.parse();
			return rollingStockServiceUtility;
		}
	}

	private Path path;
}
