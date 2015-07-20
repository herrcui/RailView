package railview.infrastructure.container;

import java.util.List;

import javafx.animation.PathTransition;
import javafx.animation.Timeline;
import javafx.application.Application.Parameters;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.VLineTo;
import javafx.util.Duration;

public class JavaFXanimations extends PannablePane {

	private CoordinateMapper mapper;
	
	public JavaFXanimations() {
		applyAnimation();
	}
	
	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}
	

	private Path generatePath() {
		final Path path = new Path();
		path.getElements().add(new MoveTo(70, 70));
		path.getElements().add(new HLineTo(350));
		path.getElements().add(new VLineTo(200));
		path.getElements().add(new VLineTo(70));
		path.getElements().add(new HLineTo(500));
		path.setOpacity(0);
		return path;
	}


	private PathTransition generatePathTransition(final Shape shape,
			final Path path) {
		final PathTransition pathTransition = new PathTransition();
		pathTransition.setDuration(Duration.seconds(8.0));
		pathTransition.setDelay(Duration.seconds(2.0));
		pathTransition.setPath(path);
		pathTransition.setNode(shape);
		pathTransition
				.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
		pathTransition.setCycleCount(Timeline.INDEFINITE);
		pathTransition.setAutoReverse(true);
		return pathTransition;
	}

	void applyAnimation() {
		final Rectangle r = new Rectangle();
		r.setX(45);
		r.setY(57.5);
		r.setWidth(50);
		r.setHeight(25);
		r.setArcWidth(10);
		r.setArcHeight(10);

		r.setFill(Color.DARKRED);
		final Path path = generatePath();
		this.getChildren().add(path);
		this.getChildren().add(r);
		this.getChildren().add(new Circle(70, 70, 5));
		this.getChildren().add(new Circle(350, 70, 5));
		this.getChildren().add(new Circle(350, 200, 5));
		this.getChildren().add(new Circle(500,70,5));
		final PathTransition transition = generatePathTransition(r, path);
		transition.play();
	}
}