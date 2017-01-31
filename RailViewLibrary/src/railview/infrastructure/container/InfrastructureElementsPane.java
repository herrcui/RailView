package railview.infrastructure.container;

import java.util.Collection;
import java.util.List;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.element.dto.Turnout;
import railapp.units.Coordinate;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class InfrastructureElementsPane extends PannablePane {
	private Collection<InfrastructureElement> elements;
	private CoordinateMapper mapper;
	private Color elementColor;
	
	private List<Coordinate> path = null;
	private Coordinate eventPoint = null;
	
	private double elementWidth = 0.1;

	public InfrastructureElementsPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}

	public void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}
	
	public void setHighlightedPath(List<Coordinate> path) {
		this.path = path;
	}
	
	public void setEventPoint(Coordinate coordinate) {
		this.eventPoint = coordinate;
	}

	public void setAndDrawElements(Collection<InfrastructureElement> elements, Color elementColor) {
		this.elements = elements;
		this.elementColor = elementColor;
		this.draw();
	}
	
	public ObservableList<Node> getChrildren() {
		return this.getChrildren();
	}
	
	public void setElements(Collection<InfrastructureElement> elements) {
		this.elements = elements;
	}

	public void draw() {
		if (this.elements == null)
			return;

		this.getChildren().clear();

		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}
		
		this.drawPath();
		this.drawEventPoint();
	}

	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			List<Coordinate> coordinates = ((Track) element)
					.getCoordinateAtLink12();
			this.drawLink(coordinates, this.elementColor, elementWidth);
		} else {
			if (element instanceof Turnout) {
				List<Coordinate> coordinates = element.findLink(1, 2)
						.getCoordinates();
				Coordinate middlePoint = Coordinate.fromXY(0.5 * (coordinates
						.get(0).getX() + coordinates.get(1).getX()),
						0.5 * (coordinates.get(0).getY() + coordinates.get(1)
								.getY()));
				this.drawLink(coordinates, this.elementColor, elementWidth);
				coordinates = element.findLink(1, 3).getCoordinates();
				coordinates.add(1, middlePoint);
				this.drawLink(coordinates, this.elementColor, elementWidth);
			} else {
				List<Coordinate> coordinates = element.findLink(1, 3)
						.getCoordinates();
				this.drawLink(coordinates, this.elementColor, elementWidth);
				coordinates = element.findLink(2, 4).getCoordinates();
				this.drawLink(coordinates, this.elementColor, elementWidth);
			}
		}
	}

	private void drawLink(List<Coordinate> coordinates, Color color, double width) {
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Line line = new Line();

			line.setStartX(mapper.mapToPaneX(coordinates.get(i).getX(), this));
			line.setStartY(mapper.mapToPaneY(coordinates.get(i).getY(), this));
			line.setEndX(mapper.mapToPaneX(coordinates.get(i + 1).getX(), this));
			line.setEndY(mapper.mapToPaneY(coordinates.get(i + 1).getY(), this));

			line.setStroke(color);
			line.setStrokeWidth(width);

			this.getChildren().add(line);
		}
	}
	
	private void drawPath() {
		if (path == null) {
			return;
		} else {
			drawLink(path, Color.CYAN, 0.4);
		}
	}
	
	private void drawEventPoint() {
		if (this.eventPoint != null) {
			Circle circle = new Circle();
			
			circle.setCenterX((mapper.mapToPaneX(eventPoint.getX(), this)));
			circle.setCenterY((mapper.mapToPaneY(eventPoint.getY(), this)));
			circle.setFill(Color.RED);
			circle.setRadius(0.6);
			
			this.getChildren().add(circle);
		}
	}
}
