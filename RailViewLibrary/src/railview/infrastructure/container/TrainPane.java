package railview.infrastructure.container;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Coordinate;
import railapp.units.Time;

public class TrainPane extends PannablePane {

	private CoordinateMapper mapper;

	public TrainPane() {
		this.widthProperty().addListener(observable -> draw(null));
		this.heightProperty().addListener(observable -> draw(null));
	}

	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}
	
	void updateTrainLocations(final Map<AbstractTrainSimulator, List<Coordinate>> map,
			Time time) {
		this.trainCoordinates = map;
		
		this.draw(time);
	}

	private void draw(Time time) {
		this.getChildren().clear();
		this.drawLines(trainCoordinates, time);
	}

	private void drawLines(Map<AbstractTrainSimulator, List<Coordinate>> map, Time time) {
		this.trainCoordinates = map;
		if (this.trainCoordinates != null) {
			for (Entry<AbstractTrainSimulator, List<Coordinate>> entry : this.trainCoordinates
					.entrySet()) {
				List<Coordinate> coordinateList = entry.getValue();
				Color color = Color.LIGHTGREEN;
				switch (entry.getKey().getPendingStatus()) {
					case NONE:
						color = Color.LIGHTGREEN;
						break;
					case OCCUPANCY:
						// color = Color.DODGERBLUE;
						color = Color.RED;
						break;
					case DEADLOCK:
						color = Color.YELLOW;
						break;
					case DISPATCHING:
						color = Color.BLUEVIOLET;
						break;
				}
				
				double lineLength = 0;
				if (coordinateList != null && coordinateList.size() > 0) {
					for (int i = 0; i < coordinateList.size() - 1; i++) {
						Line line = new Line();

						float startX = mapper.mapToPaneX(coordinateList.get(i).getX(), this);
						float startY = mapper.mapToPaneY(coordinateList.get(i).getY(), this);
						float endX = mapper.mapToPaneX(coordinateList.get(i+1).getX(), this);
						float endY = mapper.mapToPaneY(coordinateList.get(i+1).getY(), this);
						
						line.setStartX(startX);
						line.setStartY(startY);
						line.setEndX(endX);
						line.setEndY(endY);

						lineLength += Math.sqrt((endX - startX)*(endX - startX) + 
								(endY - startY)*(endY - startY));
						
						line.setStrokeWidth(0.4);		
						line.setStroke(color);

						this.getChildren().add(line);
					}
				}
			}
		}
	}

	private Map<AbstractTrainSimulator, List<Coordinate>> trainCoordinates;
}
