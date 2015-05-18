package railview.railmodel.infrastructure.railsys7;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import railapp.infrastructure.exception.NullIdException;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;
import railapp.parser.railsys7.infrastructure.InfrastructureParser;
import railview.railsys.data.RailsysData;

public class InfrastructureReader {
	public static InfrastructureReader getInstance(Path path) {
		return new InfrastructureReader(path);
	}
	
	// Only used for testing
	public static InfrastructureReader getInstance() {
		URL url = RailsysData.class.getResource("\\var-2011");
		Path path;
		try {
			path = Paths.get(url.toURI());
			return new InfrastructureReader(path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private InfrastructureReader(Path path) {
		this.path = path;
	}

	public IInfrastructureServiceUtility initialize() {
		IInfrastructureServiceUtility infraServiceUtility = new ServiceUtility();

		InfrastructureParser parser = InfrastructureParser.getInstance(
				infraServiceUtility, this.path);
		try {
			parser.parse();
			return infraServiceUtility;
		} catch (NullIdException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private Path path;
}
