package railview.railmodel.infrastructure.railsys7;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import railapp.parser.railsys7.rollingstock.RollingStockParser;
import railapp.rollingstock.service.IRollingStockServiceUtility;
import railview.railsys.data.RailsysData;

public class RollingStockReader {
	public static RollingStockReader getInstance(Path path) {
		return new RollingStockReader(path);
	}
	
	// Only used for testing
	public static RollingStockReader getInstance() {
		URL url = RailsysData.class.getResource("\\var-rollingstock");
		Path path;
		try {
			path = Paths.get(url.toURI());
			return new RollingStockReader(path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static RollingStockReader getInstanceHannover() {
		URL url = RailsysData.class.getResource("\\rs-hannover\\var-rollingstock");
		Path path;
		try {
			path = Paths.get(url.toURI());
			return new RollingStockReader(path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
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
