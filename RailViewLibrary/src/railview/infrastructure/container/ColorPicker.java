package railview.infrastructure.container;

import javafx.scene.paint.Color;

public class ColorPicker {
	public ColorPicker(double startValue, double endValue, Color startColor, Color endColor) {
		this.startValue = startValue;
		this.endValue = endValue;
		this.startColor = startColor;
		this.endColor = endColor;
	}
	
	public ColorPicker(Color color) {
		this.startColor = color;
		this.endColor = color;
	}
	
	public Color getColor() {
		return this.startColor;
	}
	
	private double startValue;
	private double endValue;
	private Color startColor;
	private Color endColor;
}
