package railview.simulation.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import railapp.infrastructure.element.dto.InfrastructureElement;
import railapp.infrastructure.element.dto.Port;
import railapp.infrastructure.element.dto.Track;
import railapp.infrastructure.element.dto.Turnout;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
import railapp.simulation.logs.InfrastructureOccupancyAndPendingLogger;
import railapp.units.Coordinate;
import railapp.units.Duration;
import railapp.units.Percentage;
import railview.infrastructure.container.CoordinateMapper;
import railview.infrastructure.container.InfrastructureElementsPane;
import railview.infrastructure.container.NodeGestures;
import javafx.animation.AnimationTimer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
public class OccupancyAndPendingPaneController {
	
	@FXML
	private StackPane infraRoot;
	
	@FXML
	private Rectangle pendingBar, occupancyBar;
	
	private InfrastructureElementsPane elementPane;
	
	@FXML
	public void initialize() {
		this.elementPane = new InfrastructureElementsPane();
		this.infraRoot.getChildren().add(this.elementPane);
		pendingBar.setLayoutX(infraRoot.getPrefWidth()/2 - pendingBar.getWidth()/2);
		occupancyBar.setLayoutX(infraRoot.getPrefWidth()/2 + occupancyBar.getWidth()/2);
		
		
		AnimationTimer timer = new AnimationTimer() {
			private long lastUpdateTime = 0; 
			
            @Override
            public void handle(long now) {
            	// TODO Using switch
            	
    			if (isActive && now - lastUpdateTime >= 1000_000_000) {
    				switch (type) {
						case OCCUPANCY:
							drawOccupancy();
							occupancyBar.setFill(Color.GREY);
							pendingBar.setFill(Color.WHITE);
							break;
						case PENDING:
							drawPending();
							occupancyBar.setFill(Color.WHITE);
							pendingBar.setFill(Color.GREY);
							break;
						default:
							drawOccupancy();
							occupancyBar.setFill(Color.GREY);
							pendingBar.setFill(Color.WHITE);
							break;
    				}
    				lastUpdateTime = now;
    			}
            }
        };
		
		timer.start();
	}
	
	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility infraServiceUtility) {
		Collection<InfrastructureElement> elements = infraServiceUtility
				.getInfrastructureElementService().findElements();

		double maxX = Double.MIN_VALUE;
		double minX = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;

		for (InfrastructureElement element : elements) {
			for (Port port : element.getPorts()) {
				if (port.getCoordinate().getX() > maxX)
					maxX = port.getCoordinate().getX();
				if (port.getCoordinate().getX() < minX)
					minX = port.getCoordinate().getX();
				if (port.getCoordinate().getY() > maxY)
					maxY = port.getCoordinate().getY();
				if (port.getCoordinate().getY() < minY)
					minY = port.getCoordinate().getY();
			}
		}

		CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY,
				minY);

		this.elementPane.setCoordinateMapper(mapper);
		this.elementPane.setElements(elements);
		
		this.elements = elements;
		this.mapper = mapper;
	}
	
	public void setLogger(InfrastructureOccupancyAndPendingLogger logger) {
		this.logger = logger;
	}
	
	
	@FXML
	private void mouseEnter(){
		infraRoot.setOnMousePressed(new EventHandler<MouseEvent>()
		        {
            public void handle(MouseEvent event)
            {
                pressedX = event.getX();
                pressedY = event.getY();
            }
        });

		infraRoot.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {
            	infraRoot.setTranslateX(infraRoot.getTranslateX() + event.getX() - pressedX);
            	infraRoot.setTranslateY(infraRoot.getTranslateY() + event.getY() - pressedY);

                event.consume();
            }
        });
	}
	
	@FXML
	private void scrollWheel(){
		NodeGestures elemNodeGestures = new NodeGestures(elementPane);
		
		infraRoot.addEventFilter( ScrollEvent.ANY, elemNodeGestures.getOnScrollEventHandler());
	}
	

	private void drawOccupancy() {
		if (this.elements == null)
			return;

		this.elementPane.getChildren().clear();
		
		this.type = OCCUPANCY;

		for (InfrastructureElement element : elements) {
			this.drawInfrastructureElement(element);
		}
		
		elementPane.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	for (InfrastructureElement element : elements) {
					drawInfrastructureElement(element);
				}
		    }
		});
		elementPane.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	for (InfrastructureElement element : elements) {
					drawInfrastructureElement(element);
				}
		    }
		});
	}
	

	private void drawPending() {
		if (this.elements == null)
			return;

		this.elementPane.getChildren().clear();
		
		this.type = PENDING;

		for (InfrastructureElement element : elements) {
			this.drawInfrastructureElement(element);
		}
		
		elementPane.widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		    	for (InfrastructureElement element : elements) {
					drawInfrastructureElement(element);
				}
		    }
		});
		elementPane.heightProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
		    	for (InfrastructureElement element : elements) {
					drawInfrastructureElement(element);
				}
		    }
		});
	}
	
	public void navigate() {
		// TODO
		if (!isActive) {
			this.isActive = true;
		}
		
		if (this.type == -1) {
			this.type = OCCUPANCY;
		} else if (this.type == OCCUPANCY) {
			this.type = PENDING;
		} else if (this.type == PENDING) {
			this.type = OCCUPANCY;
		}
	}
	
	
	void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	
	private void drawInfrastructureElement(InfrastructureElement element) {
		if (element instanceof Track) {
			List<Coordinate> coordinates = ((Track) element).getCoordinateAtLink12();
			TreeMap<Double, Duration> opMap = null;
			
			if (logger != null) {
				if (type == OCCUPANCY) {
					opMap = logger.getOccupancyMap((Track) element);
				}
				
				if (type == PENDING) {
					opMap = logger.getPendingMap((Track) element);
				}
			}
			
			if (opMap != null) {
				this.drawTrackLinkWithOPMap(coordinates, mapper, opMap);
			} else {
				this.drawLink(coordinates, mapper, null);
			}
		} else {
			Duration duration = null;
			if (logger != null) {
				if (type == OCCUPANCY) {
					duration = logger.getJunctionOccupancyDuration(element);
				}
				
				if (type == PENDING) {
					duration = logger.getJunctionPendingDuration(element);
				}
			}
			
			if (element instanceof Turnout) {
				List<Coordinate> coordinates = element.findLink(1, 2).getCoordinates();
				Coordinate middlePoint = Coordinate.fromXY(0.5 * (coordinates
						.get(0).getX() + coordinates.get(1).getX()),
						0.5 * (coordinates.get(0).getY() + coordinates.get(1)
								.getY()));
				this.drawLink(coordinates, mapper, duration);
				coordinates = element.findLink(1, 3).getCoordinates();
				coordinates.add(1, middlePoint);
				this.drawLink(coordinates, mapper, duration);
			} else {
				List<Coordinate> coordinates = element.findLink(1, 3)
						.getCoordinates();
				this.drawLink(coordinates, mapper, duration);
				coordinates = element.findLink(2, 4).getCoordinates();
				this.drawLink(coordinates, mapper, duration);
			}
		}
	}

	private void drawTrackLinkWithOPMap(List<Coordinate> coordinates, CoordinateMapper mapper, TreeMap<Double, Duration> opMap) {
		double totalMeter = opMap.lastKey().doubleValue();
		if (totalMeter == 0) {
			this.drawLink(coordinates, mapper, null);
		}
		
		double startPercentage = 0;
		double totalCoordinateLength = 0;
		for (int i = 0; i < coordinates.size() - 1; i++) {
			totalCoordinateLength += Coordinate.getDistance(coordinates.get(i), coordinates.get(i + 1));
		}
		
		for (Entry<Double, Duration> entry : opMap.entrySet()) {
			if (entry.getKey()/totalMeter == startPercentage) {
				continue;
			}
			
			List<Coordinate> segement = this.interpolate(
					coordinates, startPercentage, entry.getKey()/totalMeter, totalCoordinateLength);
			for (int i = 0; i < segement.size() - 1; i++) {
				Line line = new Line();

				line.setStartX(mapper.mapToPaneX(coordinates.get(i).getX(), this.elementPane));
				line.setStartY(mapper.mapToPaneY(coordinates.get(i).getY(), this.elementPane));
				line.setEndX(mapper.mapToPaneX(coordinates.get(i + 1).getX(), this.elementPane));
				line.setEndY(mapper.mapToPaneY(coordinates.get(i + 1).getY(), this.elementPane));

				this.setStroke(line, entry.getValue());

				this.elementPane.getChildren().add(line);
			}
			
			startPercentage = entry.getKey()/totalMeter;
		}
	}
	
	private List<Coordinate> interpolate(
			List<Coordinate> coordinates, double startPercentage, double endPercentage, double totalCoordinateLength) {		
		List<Coordinate> segement = new ArrayList<Coordinate>();
		double currentStartPercentage = 0;
		double currentEndPercentage = 0;
		
		for (int i = 0; i < coordinates.size() - 1; i++) {
			currentEndPercentage = Coordinate.getDistance(coordinates.get(i), coordinates.get(i + 1))/totalCoordinateLength;
					
			if (segement.size() == 0 && startPercentage >= currentStartPercentage && startPercentage < currentEndPercentage) {
				Coordinate coordinate = Coordinate.getCoordinateByPercentage(
					coordinates.get(i), coordinates.get(i + 1), Percentage.fromDouble(startPercentage - currentStartPercentage));
				segement.add(coordinate);
			}
			
			if (segement.size() > 0) {
				if (endPercentage <= currentEndPercentage) {
					Coordinate coordinate = Coordinate.getCoordinateByPercentage(
							coordinates.get(i), coordinates.get(i + 1), Percentage.fromDouble(endPercentage - currentStartPercentage));
					segement.add(coordinate);
					return segement;
				} else {
					segement.add(coordinates.get(i + 1));
				}
			}
			
			currentStartPercentage = currentEndPercentage;
		}
		
		return segement;
	}
	
	private void drawLink(List<Coordinate> coordinates, CoordinateMapper mapper, Duration duration) {
		for (int i = 0; i < coordinates.size() - 1; i++) {
			Line line = new Line();

			line.setStartX(mapper.mapToPaneX(coordinates.get(i).getX(), this.elementPane));
			line.setStartY(mapper.mapToPaneY(coordinates.get(i).getY(), this.elementPane));
			line.setEndX(mapper.mapToPaneX(coordinates.get(i + 1).getX(), this.elementPane));
			line.setEndY(mapper.mapToPaneY(coordinates.get(i + 1).getY(), this.elementPane));

			this.setStroke(line, duration);

			this.elementPane.getChildren().add(line);
		}
	}
	
	private void setStroke(Line line, Duration duration) {
		double totalSeconds = 18000;
		
		if (duration == null || duration.getTotalSecond() == 0) {
			line.setStroke(Color.WHITE);
			line.setStrokeWidth(this.MIN_WIDTH);
		} else {
			if (type == OCCUPANCY) {
				line.setStroke(Color.LIGHTGREEN);
			}
			
			if (type == PENDING) {
				line.setStroke(Color.RED);
			}
			
			double width = duration.getTotalSecond() / totalSeconds;
			if (width < this.MIN_WIDTH) {
				width = this.MIN_WIDTH;
			}
			line.setStrokeWidth(width);
		}
	}

	private double pressedX, pressedY;
	private InfrastructureOccupancyAndPendingLogger logger;
	
	private Collection<InfrastructureElement> elements;
	private CoordinateMapper mapper;
	
	private int type = -1;
	private boolean isActive = false;
	
	private double MIN_WIDTH = 0.07;
	
	private final int OCCUPANCY = 1;
	private final int PENDING = 2;
}
