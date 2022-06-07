package railview.railmodel.infrastructure.railsys7;

import java.nio.file.Path;

import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.infrastructure.service.ServiceUtility;

public class InfrastructureReader {
	public static InfrastructureReader getInstance(Path path) {
		return new InfrastructureReader(path);
	}

	private InfrastructureReader(Path path) {
		this.path = path;
	}

	public IInfrastructureServiceUtility initialize(boolean isCoreModel) {
		IInfrastructureServiceUtility infraServiceUtility = new ServiceUtility();

		if (isCoreModel) {
			railapp.parser.coremodel.infrastructure.InfrastructureParser parser =
				railapp.parser.coremodel.infrastructure.InfrastructureParser.getInstance(
					infraServiceUtility, this.path.toString());
			parser.parse();
		} else {
			railapp.parser.railsys7.infrastructure.InfrastructureParser parser =
				railapp.parser.railsys7.infrastructure.InfrastructureParser.getInstance(
					 this.path, infraServiceUtility);
			parser.parse();
		}
		return infraServiceUtility;
	}

	private Path path;
}
