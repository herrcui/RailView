package railview.infrastructure.container;

import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Track;

public class Test_SecondLayer extends PannablePane {
	
	private CoordinateMapper mapper;
	
	public Test_SecondLayer() {

		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}
	
	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}
	
	private void draw() {
		
		this.getChildren().clear();
		
		this.drawLine();

	}

	private void drawLine() {
		
			Line line = new Line();
			
			line.setStartX(10);
			line.setStartY(100);
			line.setEndX(200);
			line.setEndY(100);
			
			
			this.getChildren().add(line);
		}
}