package railview.railmodel.infrastructure.railsys7;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;
import railapp.parser.railsys7.infrastructure.InfrastructureParser;
import railview.railsys.data.RailsysData;

public class InfrastructureReader {
	public static InfrastructureReader getRailSys7Instance(Path path) {
		return new InfrastructureReader(path);
	}
	
	public static InfrastructureReader getRailSys7Instance(URL url) {
		Path path;
		try {
			path = Paths.get(url.toURI());
			return new InfrastructureReader(path);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static InfrastructureReader getInstanceHannover() {
		URL url = RailsysData.class.getResource("\\rs-hannover\\var-2011");
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
				 this.path, infraServiceUtility);
		parser.parse();
		return infraServiceUtility;
	}
	
	private Path path;
}
