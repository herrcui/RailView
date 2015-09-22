package railview.infrastructure.container;

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
			Collection<Swarm> swarms) {
		this.trainCoordinates = coordinates;
		this.swarms = swarms;
		this.draw();
	}

	private void draw() {
		this.getChildren().clear();
		this.drawSwarms();
	}
	
	private void drawSwarms() {
		Set<Color> usedColor = new HashSet<Color>();
		if (this.trainCoordinates != null && this.swarms != null) {	// TODO: check not both are not null
			for (Swarm swarm : this.swarms) {
				Color color = this.getSwarmColor(swarm, usedColor);
				usedColor.add(color);
				for (AbstractTrainSimulator train : swarm.getTrains()) {
					this.drawTrain(train ,color);
				}
			}
		}
	}
	
	private Color getSwarmColor(Swarm swarm, Set<Color> usedColor) {
		Color color = this.swarmColorMap.get(swarm);
		if (color == null) {
			for (AbstractTrainSimulator train : swarm.getTrains()) {
				color = this.trainColorMap.get(train);
				if (color != null && !usedColor.contains(color)) {
					break;
				}
			}
			
			if (color == null) {
				color = generateRandomColor();
			}
			
			this.swarmColorMap.put(swarm, color);
			
			for (AbstractTrainSimulator train : swarm.getTrains()) {
				this.trainColorMap.put(train, color);
			}
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
				line.setFill(color);
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
	private Collection<Swarm> swarms;
	
	private Map<Swarm, Color> swarmColorMap = new HashMap<Swarm, Color>();
	private Map<AbstractTrainSimulator, Color> trainColorMap = new HashMap<AbstractTrainSimulator, Color>();
}
