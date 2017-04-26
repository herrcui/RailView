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
			Path path = Paths.get("C:\\Users\\cu.IEV-VWI962\\Downloads\\Lat_L_V_0_con.csv");
			
			List<TrainingData> trainingData = TrainingDataReader.readTrainingData(path);
			
			SupervisedLearning SL = new SupervisedLearning(trainingData,
					1,
					2,
					new double[] {6.276413371708169, 3.5712782411506234, 1.7687694577853579, 14.556417290067378});
					//new double[] {0, 0, 0, 0});
			
			
			SL.setDelta(0.001);
			SL.setLearningRate(0.001);
			SL.setMaxRound(100000);
			
			SL.learn(0.1);
			
			String s = "";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
