package railview.railmodel.infrastructure.railsys7;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

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
