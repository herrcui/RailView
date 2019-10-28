package railview.simulation.ui.data;

import javafx.scene.layout.Pane;

/**
 * A class to map the x and y coordinates to the Pane.
 */
public class CoordinateMapper {
	
	public CoordinateMapper(double maxX, double minX, double maxY, double minY) {
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
	}
	
	public float mapToPaneX(double x, Pane canvas) {
		float mappedX = (float) (canvas.getWidth() *
				(x - this.minX) / (this.maxX - this.minX));
		return mappedX;
	}
	
	public float mapToPaneY(double y, Pane canvas) {
		float mappedY = (float) (canvas.getHeight() * 
				(this.maxY - y) / (this.maxY - this.minY));
		return mappedY;
	}
	
	private double maxX, minX, maxY, minY;
	
}
