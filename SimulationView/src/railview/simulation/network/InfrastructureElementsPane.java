package railview.simulation.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import railapp.infrastructure.dto.Station;
import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.RelativePosition;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.element.dto.Turnout;
import railapp.infrastructure.signalling.dto.AbstractSignal;
import railapp.units.Coordinate;
import railview.simulation.setting.UIInfrastructureSetting;
import railview.simulation.ui.data.CoordinateMapper;
import railview.simulation.ui.utilities.PannablePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * The class responsible to draw the InfrastructureElement, the events and the signals.
 *
 */
public class InfrastructureElementsPane extends PannablePane {
	private Collection<InfrastructureElement> elements;
	private Collection<AbstractSignal> signals;
	private Collection<Station> stations;
	private CoordinateMapper mapper;
	private Color elementColor;
	private Color signalColor = Color.RED;
	private UIInfrastructureSetting uiInfraSetting;
	private List<Coordinate> path = null;
	private Coordinate eventPoint = null;

	private double signalWidth = 0.1; // 0.2

	public InfrastructureElementsPane() {
		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	}

	public void setUIInfraSetting(UIInfrastructureSetting uiInfraSetting) {
		this.uiInfraSetting = uiInfraSetting;
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

	public void setElements(Collection<InfrastructureElement> elements,
			Color elementColor) {
		this.elements = elements;
		this.elementColor = elementColor;
	}

	public void setSignals(Collection<AbstractSignal> signals, Color signalColor) {
		this.signals = signals;
		this.signalColor = signalColor;
	}

	public void draw() {
		if (this.elements == null)
			return;

		this.getChildren().clear();

		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}

		if (this.signals != null) { // for some panes, it is not necessary to
									// draw signals.
			for (AbstractSignal signal : this.signals) {
				this.drawSignal(signal);
			}
		}

		if (this.stations != null) {
			this.drawStations();
		}

		this.drawPath();
		this.drawEventPoint();
	}

	public void setStations(Collection<Station> allStations) {
		this.stations = allStations;
	}

	private void drawStations() {
		if (!this.uiInfraSetting.isShowStation()) {
			return;
		}

		for (Station station : this.stations) {
			Text dataText = new Text(station.getDescription());
			dataText.setLayoutX(mapper.mapToPaneX(station.getCoordinate().getX(), this));
			dataText.setLayoutY(mapper.mapToPaneY(station.getCoordinate().getY(), this));
			dataText.setFill(this.elementColor);
			dataText.setFont(new Font(4));

			this.getChildren().add(dataText);
		}
	}

	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			List<Coordinate> coordinates = ((Track) element)
					.getCoordinateAtLink12();

			this.drawLink(coordinates, this.elementColor, this.uiInfraSetting.getElementWidth());
		} else {
			if (element instanceof Turnout) {
				List<Coordinate> coordinates = element.findLink(1, 2).getCoordinates();
				this.drawLink(coordinates, this.elementColor, this.uiInfraSetting.getElementWidth());

				coordinates = element.findLink(1, 3).getCoordinates();
				this.drawLink(coordinates, this.elementColor, this.uiInfraSetting.getElementWidth());
			} else {
				List<Coordinate> coordinates = element.findLink(1, 3).getCoordinates();
				this.drawLink(coordinates, this.elementColor, this.uiInfraSetting.getElementWidth());

				coordinates = element.findLink(2, 4).getCoordinates();
				this.drawLink(coordinates, this.elementColor, this.uiInfraSetting.getElementWidth());
			}
		}
	}

	private void drawLink(List<Coordinate> coordinates, Color color,
			double width) {
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

	private void drawSignal(AbstractSignal signal) {
		RelativePosition position = signal.getPositions().get(0);
		double percentage = position.getDistance().getMeter()
				/ position.getLink().getTotalLength().getMeter();

		List<Coordinate> coordinates = position.getLink().getCoordinates();
		Coordinate startCoor = coordinates.get(coordinates.size() - 2);
		Coordinate endCoor = coordinates.get(coordinates.size() - 1);

		List<Double> coorDists = new ArrayList<Double>();
		double totalCoorDist = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			double coorDist = Coordinate.getDistance(coordinates.get(i),
					coordinates.get(i + 1));
			totalCoorDist += coorDist;
			coorDists.add(totalCoorDist);
		}

		for (int i = 0; i < coordinates.size() - 1; i++) {
			if (coorDists.get(i) / totalCoorDist >= percentage) {
				startCoor = coordinates.get(i);
				endCoor = coordinates.get(i + 1);
				break;
			}
		}

		Coordinate signalCoordinate = position.getLink().getCoordinate(position.getDistance());

		double signalX = mapper.mapToPaneX(signalCoordinate.getX(), this);
		double signalY = mapper.mapToPaneY(signalCoordinate.getY(), this);

		double xStart = mapper.mapToPaneX(startCoor.getX(), this);
		double yStart = mapper.mapToPaneY(startCoor.getY(), this);
		double xEnd = mapper.mapToPaneX(endCoor.getX(), this);
		double yEnd = mapper.mapToPaneY(endCoor.getY(), this);


		double a = 0.4;
		double b = 0.4;
		double c = 0.5 * a; // radius of the circle

		double xCoordinateP0;
		double yCoordinateP0;
		double xCoordinateP1;
		double yCoordinateP1;
		double xCoordinateP2;
		double yCoordinateP2;
		double xCoordinateP3;
		double yCoordinateP3;

		double length = Math.sqrt(Math.pow((yEnd - yStart), 2)+ Math.pow((xEnd - xStart), 2));

		// P0 - P3 parallel to line
		// P1 - P2 perpendicular to line
		// Here all the coordinates are screen coordinates, increase to buttom and right

		xCoordinateP0 = signalX - (a * ((yEnd - yStart)/ length));
		yCoordinateP0 = signalY + (a * ((xEnd - xStart)/ length));

		xCoordinateP1 = signalX - ((a-(b/2.0)) * ((yEnd - yStart)/ length));
		yCoordinateP1 = signalY + ((a-(b/2.0)) * ((xEnd - xStart)/ length));

		xCoordinateP2 = signalX - ((a+(b/2.0)) * ((yEnd - yStart)/ length));
		yCoordinateP2 = signalY + ((a+(b/2.0)) * ((xEnd - xStart)/ length));

		xCoordinateP3 = xCoordinateP0 + ((xEnd-xStart) * a*2 / length);
		yCoordinateP3 = yCoordinateP0 + ((yEnd-yStart)* a*2 / length);

		Line line1 = new Line();
		line1.setStartX(xCoordinateP0);
		line1.setStartY(yCoordinateP0);
		line1.setEndX(xCoordinateP3);
		line1.setEndY(yCoordinateP3);
		line1.setStroke(signalColor);
		line1.setStrokeWidth(this.signalWidth);

		Line line2 = new Line();
		line2.setStartX(xCoordinateP2);
		line2.setStartY(yCoordinateP2);
		line2.setEndX(xCoordinateP1);
		line2.setEndY(yCoordinateP1);
		line2.setStroke(signalColor);
		line2.setStrokeWidth(this.signalWidth);


		Circle circle = new Circle();
		circle.setRadius(c);
		circle.setCenterX(xCoordinateP3);
		circle.setCenterY(yCoordinateP3);
		circle.setStrokeWidth(this.signalWidth);
		circle.setStroke(signalColor);

		this.getChildren().addAll(line1, line2, circle);
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
