package railview.simulation.ui;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class SelectionRectangle extends Rectangle {


	public SelectionRectangle() {
		setFill(Color.GREEN.deriveColor(0, 1, 1, 0.5));
		setVisible(false);
		setManaged(false);
		setMouseTransparent(true);
	}
}
