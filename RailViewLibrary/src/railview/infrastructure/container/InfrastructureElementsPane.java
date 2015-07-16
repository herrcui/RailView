package railview.infrastructure.container;

import java.util.Collection;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Track;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Line;

public class InfrastructureElementsPane extends PannablePane {
	private Collection<InfrastructureElement> elements;
	private CoordinateMapper mapper;
	final double SCALE_DELTA = 1.1;
	
	public InfrastructureElementsPane() {

		NodeGestures nodeGestures = new NodeGestures(this);

		this.addEventFilter(MouseEvent.MOUSE_PRESSED,
				nodeGestures.getOnMousePressedEventHandler());
		this.addEventFilter(MouseEvent.MOUSE_DRAGGED,
				nodeGestures.getOnMouseDraggedEventHandler());

		this.widthProperty().addListener(observable -> draw());
		this.heightProperty().addListener(observable -> draw());
	
//		ScrollEvent();
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
		
		this.getChildren().clear();
		
		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}
	}
	
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

				draw();
			}
		});

	}

	private void drawInfrastructureElement(InfrastructureElement element) {
		
		if (element instanceof Track) {
			Line line = new Line();
			
			line.setStartX(mapper.mapToPaneX(element.findPort(1).getCoordinate()
					.getX(), this));
			line.setStartY(mapper.mapToPaneY(element.findPort(1).getCoordinate()
					.getY(), this));
			line.setEndX(mapper.mapToPaneX(element.findPort(2).getCoordinate()
					.getX(), this));
			line.setEndY(mapper.mapToPaneY(element.findPort(2).getCoordinate()
							.getY(), this));
			
			line.setStrokeWidth(0.2);
			
			this.getChildren().add(line);
		}
	}

}
