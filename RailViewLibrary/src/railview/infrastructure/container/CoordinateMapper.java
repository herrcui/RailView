package railview.infrastructure.container;

import javafx.scene.canvas.Canvas;

public class CoordinateMapper {
	public CoordinateMapper(double maxX, double minX, double maxY, double minY) {
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
	}
	
	public float mapToPaneX(double x, Canvas canvas) {
		float mappedX = (float) (canvas.getWidth() *
				(x - this.minX) / (this.maxX - this.minX));
		return mappedX;
	}
	
	public float mapToPaneY(double y, Canvas canvas) {
		float mappedY = (float) (canvas.getHeight() * 
				(y - this.minY) / (this.maxY - this.minY));
		return mappedY;
	}
	
	private double maxX, minX, maxY, minY;
}
