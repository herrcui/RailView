package railview.infrastructure.container;

import java.util.Collection;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Track;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class InfrastructureElementsCanvas extends Canvas {
	private Collection<InfrastructureElement> elements;
	private CoordinateMapper mapper;
	private double pressedX, pressedY;
	final double SCALE_DELTA = 1.1;

	public InfrastructureElementsCanvas() {

		PannableCanvas canvas = new PannableCanvas();
		NodeGestures nodeGestures = new NodeGestures(canvas);

		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());

		this.addEventFilter(MouseEvent.MOUSE_PRESSED,
				nodeGestures.getOnMousePressedEventHandler());
		this.addEventFilter(MouseEvent.MOUSE_DRAGGED,
				nodeGestures.getOnMouseDraggedEventHandler());

		// MouseClickEvent();
		// ScrollEvent();

	}

	/**
	 * A method for an event by clicking/dragging the mouse. (panning)
	 */

	private void MouseClickEvent() {
		this.setOnMousePressed(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				pressedX = event.getX();
				pressedY = event.getY();
			}
		});

		this.setOnMouseDragged(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent event) {
				setTranslateX(getTranslateX() + event.getX() - pressedX);
				setTranslateY(getTranslateY() + event.getY() - pressedY);

				event.consume();
			}
		});

	}

	/**
	 * a method for an event by using the scroll wheel. (zooming) Only zooms the
	 * canvas though.
	 */
	private void ScrollEvent() {
		this.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent event) {
				event.consume();

				if (event.getDeltaY() == 0) {
					return;
				}

				double scaleFactor = (event.getDeltaY() > 0) ? SCALE_DELTA
						: 1 / SCALE_DELTA;

				setScaleX(getScaleX() * scaleFactor);
				setScaleY(getScaleY() * scaleFactor);
			}
		});

	}

	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}

	void setElements(Collection<InfrastructureElement> elements) {
		this.elements = elements;
	}

	private void draw() {

		if (this.elements == null)
			return;

		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}
	}

	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			GraphicsContext gc = this.getGraphicsContext2D();
			gc.strokeLine(
					mapper.mapToPaneX(element.findPort(1).getCoordinate()
							.getX(), this),
					mapper.mapToPaneY(element.findPort(1).getCoordinate()
							.getY(), this),
					mapper.mapToPaneX(element.findPort(2).getCoordinate()
							.getX(), this),
					mapper.mapToPaneY(element.findPort(2).getCoordinate()
							.getY(), this));
		}

	}

	InvalidationListener listener = new InvalidationListener() {
		@Override
		public void invalidated(Observable observable) {
			draw();
		}
	};
}
