package railview.infrastructure.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.swarmintelligence.Swarm;
import railapp.units.Coordinate;
import railapp.units.Time;

public class SwarmPane extends PannablePane {
	private CoordinateMapper mapper;

	public SwarmPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}

	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}

	public void updateSwarms(Map<AbstractTrainSimulator, List<Coordinate>> coordinates,
			Collection<Swarm> swarms,
			Time time) {
		this.trainCoordinates = coordinates;
		this.swarms = swarms;
		this.draw();
	}

	private void draw() {
		this.getChildren().clear();
		this.drawSwarms();
	}

	private void drawSwarms() {
		for (Swarm swarm : this.swarms) {
			Color color = this.getSwarmColor(swarm);

			Collection<AbstractTrainSimulator> trains = swarm.getTrains();

			for (AbstractTrainSimulator train : trains) {
				this.drawTrain(train ,color);
			}
		}
	}

	private Color getSwarmColor(Swarm swarm) {
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
				color = generateRandomColor();
			}

			this.swarmColorMap.put(swarm, color);
			this.usedColors.add(color);
		}

		return color;
	}

	private Color generateRandomColor() {
	    Random random = new Random();
	    double red = random.nextInt(256);
	    double green = random.nextInt(256);
	    double blue = random.nextInt(256);

	    Color color = Color.rgb((int) red, (int) green, (int) blue);
	    return color;
	}

	private void drawTrain(AbstractTrainSimulator train, Color color) {
		List<Coordinate> coordinateList = this.trainCoordinates.get(train);

		if (coordinateList != null && coordinateList.size() > 0) {
			for (int i = 0; i < coordinateList.size() - 1; i++) {
				Line line = new Line();

				line.setStartX(mapper.mapToPaneX(coordinateList.get(i)
						.getX(), this));
				line.setStartY(mapper.mapToPaneY(coordinateList.get(i)
						.getY(), this));
				line.setEndX(mapper.mapToPaneX(coordinateList
						.get(i+1).getX(), this));
				line.setEndY(mapper.mapToPaneY(coordinateList
						.get(i+1).getY(), this));

				line.setStrokeWidth(0.3);
				line.setStroke(color);

				this.getChildren().add(line);
			}
		}
	}

	private Map<AbstractTrainSimulator, List<Coordinate>> trainCoordinates;
	private Collection<Swarm> swarms = new ArrayList<Swarm>();

	private Set<Color> usedColors = new HashSet<Color>();
	private Map<Swarm, Color> swarmColorMap = new HashMap<Swarm, Color>();

	private Color COLOR_SINGLETRAIN = Color.WHITESMOKE;
}
