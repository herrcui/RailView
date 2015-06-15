package railview.infrastructure.container;

import java.util.Collection;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Track;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

public class InfrastructureElementsCanvas extends Canvas {
	private Collection<InfrastructureElement> elements;
	private CoordinateMapper mapper;
	
	InfrastructureElementsCanvas() {
		this.widthProperty().addListener(listener);
		this.heightProperty().addListener(listener);
	}
	
	void setCoordinateMapper(CoordinateMapper mapper) {
		this.mapper = mapper;
	}
	
	void setElements(Collection<InfrastructureElement> elements){
		this.elements = elements;
	}
	
	private void draw() {
		this.setStyle("-fx-background-color: yellow;");
		
		if (this.elements == null)
			return;
		
		for (InfrastructureElement element : this.elements) {
			this.drawInfrastructureElement(element);
		}
	}
	
	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			GraphicsContext gc = this.getGraphicsContext2D();
			gc.strokeLine(mapper.mapToPaneX(element.findPort(1).getCoordinate().getX(), this),
					mapper.mapToPaneY(element.findPort(1).getCoordinate().getY(), this),
					mapper.mapToPaneX(element.findPort(2).getCoordinate().getX(), this),
					mapper.mapToPaneY(element.findPort(2).getCoordinate().getY(), this));
		}
	}
	
	InvalidationListener listener = new InvalidationListener(){
		@Override
		public void invalidated(Observable observable) {
			draw();
		}           
	};
}
