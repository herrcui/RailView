package railview.infrastructure.container;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.Swarm;
import railapp.swarmintelligence.SwarmManager;
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
	
	void setSwarmManager(SwarmManager swarmManager) {
		this.swarmManager = swarmManager;
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
				Color color = this.getColor(entry.getKey());
				// color = this.getColorBySwarm(entry.getKey(), time);
				
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

						line.setStrokeWidth(0.4);		
						line.setStroke(color);

						this.getChildren().add(line);
					}
				}
			}
		}
	}
	
	private Color getColor(AbstractTrainSimulator train) {
		Color color = Color.LIGHTGREEN;
		switch (train.getPendingStatus()) {
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
		
		return color;
	}
	
	private Color getColorBySwarm(AbstractTrainSimulator train, Time time) {
		Swarm swarm = this.swarmManager.getSwarm(train, time);
		Color color = this.swarmColorMap.get(swarm);
		
		if (color == null) {
			if (swarm.getTrains().size() == 1) {
				color = this.COLOR_SINGLETRAIN;
				this.swarmColorMap.put(swarm, color);
				return color;
			}

			while (color == null ||
					this.usedColors.contains(color) ||
					color.equals(this.COLOR_SINGLETRAIN)) {
				Random random = new Random();
			    double red = random.nextInt(256);
			    double green = random.nextInt(256);
			    double blue = random.nextInt(256);

			    color = Color.rgb((int) red, (int) green, (int) blue);
			}

			this.swarmColorMap.put(swarm, color);
			this.usedColors.add(color);
		}

		return color;
	}

	private Map<AbstractTrainSimulator, List<Coordinate>> trainCoordinates;
	
	
	private Map<Swarm, Color> swarmColorMap = new HashMap<Swarm, Color>();
	private Set<Color> usedColors = new HashSet<Color>();
	private Color COLOR_SINGLETRAIN = Color.WHITESMOKE;
	private SwarmManager swarmManager;
}
