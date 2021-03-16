package railview.railmodel.infrastructure.railsys7;

import java.nio.file.Path;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;
import railapp.parser.railsys7.infrastructure.InfrastructureParser;

public class InfrastructureReader {
	public static InfrastructureReader getInstance(Path path) {
		return new InfrastructureReader(path);
	}

	private InfrastructureReader(Path path) {
		this.path = path;
	}

	public IInfrastructureServiceUtility initialize(boolean isCSVFormat) {
		IInfrastructureServiceUtility infraServiceUtility = new ServiceUtility();

		if (isCSVFormat) {
			railapp.parser.coremodel.infrastructure.InfrastructureParser parser =
				railapp.parser.coremodel.infrastructure.InfrastructureParser.getInstance(
					infraServiceUtility, this.path.toString());
			parser.parse();
		} else {
			InfrastructureParser parser = InfrastructureParser.getInstance(
					 this.path, infraServiceUtility);
			parser.parse();
		}
		return infraServiceUtility;
	}

	private Path path;
}
