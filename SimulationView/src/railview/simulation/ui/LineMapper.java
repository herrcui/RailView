package railview.simulation.ui;

import javafx.scene.layout.Pane;

public class LineMapper {

	private double maxX, minX, maxY, minY;

	public LineMapper(double maxX, double minX, double maxY, double minY) {
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
	}

	public double mapToPaneX(double x, Pane canvas) {
		float mappedX = (float) (canvas.getWidth() * (x - this.minX) / (this.maxX - this.minX));
		return mappedX;
	}

	public double mapToPaneY(double y, Pane canvas) {
		float mappedY = (float) (canvas.getHeight() * (this.maxY - y) / (this.maxY - this.minY));
		return mappedY;
	}
}
