package railview.simulation.ui;

import javafx.scene.layout.Pane;

public class LineMapper {
	
	private double maxX, minX;
	
	public LineMapper(double maxX, double minX) {
		this.maxX = maxX;
		this.minX = minX;
	}
	
	public float mapToPaneX(double x, Pane canvas) {
		float mappedX = (float) (canvas.getWidth() *
				(x - this.minX) / (this.maxX - this.minX));
		return mappedX;
	}
}
