package railview.infrastructure.container;

import javafx.scene.layout.Pane;

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
				(this.maxY -y) / (this.maxY - this.minY));
		return mappedY;
	}
	
	private double maxX, minX, maxY, minY;
	
/**	
	public CoordinateMapper(double maxX, double minX, double maxY, double minY) {
		this.maxX = maxX;
		this.minX = minX;
		this.maxY = maxY;
		this.minY = minY;
		this.scale = Math.max(maxX - minX, maxY - minY);
	}

	public float mapToPaneX(double x, Pane canvas) {
		//float mappedX = (float) (canvas.getWidth() *
		//		(x - this.minX) / (this.maxX - this.minX));
		float mappedX = (float) (canvas.getWidth() *
				(x - this.minX) / this.scale);
		return mappedX;
	}

	public float mapToPaneY(double y, Pane canvas) {
		//float mappedY = (float) (canvas.getHeight() *
		//		(this.maxY -y) / (this.maxY - this.minY));
		float mappedY = (float) (canvas.getHeight() *
				(this.maxY -y) / this.scale);
		return mappedY;
	}

	private double maxX, minX, maxY, minY;
	private double scale;
**/
	
}
