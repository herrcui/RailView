package railview.infrastructure.container;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;

public class TrainPane extends PannablePane {
	
	public TrainPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}
	
	private void draw() {

	}

	void setCoordinateMap(Map<AbstractTrainSimulator, List<Coordinate>> map) {
		this.coordinateMap = map;
		
		if (this.coordinateMap != null) {
			for (Entry<AbstractTrainSimulator, List<Coordinate>> entry : this.coordinateMap.entrySet()) {
				List<Coordinate> coordinateList = entry.getValue();
				if (coordinateList != null) {
					System.out.println(entry.getKey().getTrain().getNumber() + 
							": X: " + coordinateList.get(1).getX() +
							": Y: " + coordinateList.get(1).getY());
				}
			}
		}
	}
	
	private Map<AbstractTrainSimulator, List<Coordinate>> coordinateMap;
}
