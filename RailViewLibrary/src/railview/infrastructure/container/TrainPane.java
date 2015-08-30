package railview.infrastructure.container;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.shape.Line;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;

public class TrainPane extends PannablePane {

	private CoordinateMapper mapper;

	public TrainPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}

	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}

	private void draw() {
		this.getChildren().clear();

		this.drawLines(coordinateMap);
	}

	private void drawLines(Map<AbstractTrainSimulator, List<Coordinate>> map) {
		this.coordinateMap = map;
		if (this.coordinateMap != null) {
			for (Entry<AbstractTrainSimulator, List<Coordinate>> entry : this.coordinateMap
					.entrySet()) {
				List<Coordinate> coordinateList = entry.getValue();
				if (coordinateList != null) {

					for (int i = 0; i < coordinateMap.size(); i++) {
						Line line = new Line();
						line.setStartX(mapper.mapToPaneX(coordinateList.get(i)
								.getX(), this));
						line.setStartY(mapper.mapToPaneX(coordinateList.get(i)
								.getY(), this));
						line.setEndX(mapper.mapToPaneX(coordinateList
								.get(i+1).getX(), this));
						line.setEndY(mapper.mapToPaneX(coordinateList
								.get(i+1).getY(), this));

						this.getChildren().add(line);
					}
				}
			}
		}
	}

	void setCoordinateMap(Map<AbstractTrainSimulator, List<Coordinate>> map) {
		this.coordinateMap = map;

		if (this.coordinateMap != null) {
			for (Entry<AbstractTrainSimulator, List<Coordinate>> entry : this.coordinateMap
					.entrySet()) {
				List<Coordinate> coordinateList = entry.getValue();
				if (coordinateList != null) {
					System.out.println(entry.getKey().getTrain().getNumber()
							+ ": X: " + coordinateList.get(1).getX() + ": Y: "
							+ coordinateList.get(1).getY());
				}
			}
		}
	}

	private Map<AbstractTrainSimulator, List<Coordinate>> coordinateMap;
}
