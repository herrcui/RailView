package railview.dataanalysis;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import ml.supervise.SupervisedLearning;
import ml.supervise.TrainingData;
import ml.supervise.TrainingDataReader;

public class PSDLearning {
	public static void main(String[] args) {
		try { 
			Path path = Paths.get("C:\\temp\\Lat_R_V_1212_con.csv");

			List<TrainingData> trainingData = TrainingDataReader.readTrainingData(path);

			SupervisedLearning SL = new SupervisedLearning(trainingData,
					1,
					2,
					new double[] {0.05262765218052027, 0.07783881658756395, 0.3489742126034168, 0.348953243164028});
					//new double[] {0, 0, 0, 0});

			SL.setMaxRound(1000000);

			SL.learn(0.1);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
