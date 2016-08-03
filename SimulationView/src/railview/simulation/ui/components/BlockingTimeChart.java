package railview.simulation.ui.components;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import railview.simulation.ui.DraggableChart;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TimeDistance;

public class BlockingTimeChart<X, Y> extends DraggableChart<X, Y> {
	public BlockingTimeChart(Axis<X> xAxis, Axis<Y> yAxis) {
		super(xAxis, yAxis);
	}
	
	public ObservableList<Node> getBlockingTimeChartPlotChildren() {
		return this.getPlotChildren();
	}

	public void setBlockingTime(List<BlockingTime> blockingTimes) {
		this.blockingTimes = blockingTimes;
	}
	
	public void setEventsMap(Map<TimeDistance, List<EventData>> eventsMap) {
		this.eventsMap = eventsMap;
	}

	private Path drawPath(double startx, double starty, double endx,
			double endy) {
		Path path = new Path();
		path.getElements().add(
				new MoveTo(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(startx)), this
						.getYAxis().getDisplayPosition(
								this.getYAxis().toRealValue(-starty))));
		path.getElements().add(
				new LineTo(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(endx)), this.getYAxis()
						.getDisplayPosition(
								this.getYAxis().toRealValue(-endy))));
		
		return path;
	}
	
	private Polygon drawArrow(double startx, double starty, double endx,
			double endy) {
		Polygon arrow = new Polygon();
		arrow.getPoints().addAll(
				new Double[] { 0.0, 5.0, -5.0, -5.0, 5.0, -5.0 });

		double angle = Math.atan2(endy - starty, endx - startx) * 180 / 3.14;

		arrow.setRotate((angle - 90));

		arrow.setTranslateX(this.getXAxis().getDisplayPosition(
				this.getXAxis().toRealValue(startx)));
		arrow.setTranslateY(this.getYAxis().getDisplayPosition(
				this.getYAxis().toRealValue(-starty)));

		arrow.setTranslateX(this.getXAxis().getDisplayPosition(
				this.getXAxis().toRealValue(endx)));
		arrow.setTranslateY(this.getYAxis().getDisplayPosition(
				this.getYAxis().toRealValue(-endy)));
		
		return arrow;
	}

	private void drawCircle(double centerX, double centerY, double radius,
			Paint value, EventData event) {
		Circle circle = new Circle();
		circle.setCenterX(this.getXAxis().getDisplayPosition(
				this.getXAxis().toRealValue(centerX)));
		circle.setCenterY(centerY);
		circle.setRadius(radius);
		circle.setFill(value);
		double centerPositon = this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(centerY));
		this.getPlotChildren().add(circle);
		
		circle.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				NumberAxis xAxis = (NumberAxis) getXAxis();
				NumberAxis yAxis = (NumberAxis) getYAxis();
				String eventString = ("Event: " + event.getText());
				writeText(xAxis.getUpperBound()/2 - xAxis.getUpperBound()/8, yAxis.getLowerBound()/10*9 , eventString);

			}
		});
		circle.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				removeText();

			}
		});
		
		this.circleMap.put(event, circle);
	}

	private void writeText(double x, double y, String text) {
		txt = new Text();
		txt.setText(text);
		txt.setLayoutX(this.getXAxis().getDisplayPosition(
				this.getXAxis().toRealValue(x)));
		txt.setLayoutY(this.getYAxis().getDisplayPosition(
				this.getYAxis().toRealValue(y)));
		this.getPlotChildren().add(txt);
	}

	private void removeText() {
		this.getPlotChildren().remove(this.txt);
	}

	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();
		if (this.blockingTimes != null) {
			for (BlockingTime blockingTime : this.blockingTimes) {
				r = new Rectangle();
				this.getPlotChildren().add(r);
				r.setX(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(
								blockingTime.getStartMeter())));
				r.setY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(
								-(blockingTime.getStartTimeInSecond()))));
				r.setWidth(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(
								blockingTime.getEndMeter()))
						- this.getXAxis().getDisplayPosition(
								this.getXAxis().toRealValue(
										blockingTime.getStartMeter())));
				r.setHeight(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(
								-(blockingTime.getEndTimeInSecond())))
						- this.getYAxis().getDisplayPosition(
								this.getYAxis().toRealValue(
										-(blockingTime
												.getStartTimeInSecond()))));
				r.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
			}

	
			for (Map.Entry<TimeDistance, List<EventData>> entry : this.eventsMap.entrySet()) {
				TimeDistance td = entry.getKey();
				List<EventData> eventList = entry.getValue();
				yAxis = (NumberAxis) this.getYAxis();

				Polygon polygon = new Polygon();
			        polygon.getPoints().addAll(new Double[]{
			        		(this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(td.getMeter()))),
											
							(this.getYAxis().getDisplayPosition(
									this.getYAxis().toRealValue(-td.getSecond()))-
									6.0),
			           
							(this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(td.getMeter()))+6.0),
									
							(this.getYAxis().getDisplayPosition(
									this.getYAxis().toRealValue(-td.getSecond()))),
									
							(this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(td.getMeter()))),
									
							(this.getYAxis().getDisplayPosition(
									this.getYAxis().toRealValue(-td.getSecond()))+
									6.0),
									
							(this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(td.getMeter()))-6.0),
											
							(this.getYAxis().getDisplayPosition(
									this.getYAxis().toRealValue(-td.getSecond()))),

			        });
			        this.getPlotChildren().add(polygon);

			        
			        
				polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent t) {
						System.out.println(getPlotChildren().toString());
						Polygon arrow = null;
						Path path = null;
						
						TimeDistanceEvents timeDistanceEvents = polygonMap.get(t.getSource());
						if (timeDistanceEvents == null) {
							if (td.getSecond() < -(yAxis.getLowerBound() / 2)) {
								double x = td.getMeter();
								double y = yAxis.getDisplayPosition(
										yAxis.toRealValue(((yAxis.getLowerBound() / 3) * 2)));
								arrow = drawArrow(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) * 2) - 100);
								path = drawPath(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) * 2) - 100);
								
								for (EventData event : eventList) {
									// chart.writeText(x, y, events.getText());
									if (event.getType() == 0) {
										drawCircle(x, y, 8, Color.ORANGE,
												event);
										y = y + 16;
									}
									if (event.getType() == 1) {
										drawCircle(x, y, 8, Color.GREEN,
												event);
										y = y + 16;
									}
									if (event.getType() == -1) {
										drawCircle(x, y, 8, Color.RED, event);
										y = y + 16;
									}
								}
							} else {
								double x = td.getMeter();
								double y = yAxis.getDisplayPosition(
										yAxis.toRealValue(((yAxis.getLowerBound() / 3))));
								arrow = drawArrow(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) - 100));
								path = drawPath(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) - 100));
								for (EventData events : eventList) {
									// chart.writeText(x, y, events.getText());
									if (events.getType() == 0) {
										drawCircle(x, y, 8, Color.ORANGE,
												events);
										y = y - 16;
									}
									if (events.getType() == 1) {
										drawCircle(x, y, 8, Color.GREEN,
												events);
										y = y - 16;
									}
									if (events.getType() == -1) {
										drawCircle(x, y, 8, Color.RED, events);
										y = y - 16;
									}
								}
							}
							
							getPlotChildren().addAll(arrow, path);
							polygonMap.put(polygon, new TimeDistanceEvents(arrow, path, eventList));
						} else {
							getPlotChildren().remove(timeDistanceEvents.getArrow());
							getPlotChildren().remove(timeDistanceEvents.getPath());
							for (EventData event : timeDistanceEvents.events) {
								getPlotChildren().remove(circleMap.get(event));
							}
							polygonMap.remove(polygon);
						}
					}
					
				});

			}

		}
	}

	private List<BlockingTime> blockingTimes;
	private Map<TimeDistance, List<EventData>> eventsMap;
	private NumberAxis yAxis;
	
	private Map<EventData, Circle> circleMap = new HashMap<EventData, Circle>();
	
	private Map<Polygon, TimeDistanceEvents> polygonMap = new HashMap<Polygon, TimeDistanceEvents>();

	private Text txt;
	private Rectangle r;
	
	class TimeDistanceEvents {		
		public TimeDistanceEvents(Polygon arrow, Path path,
				List<EventData> events) {
			super();
			this.arrow = arrow;
			this.path = path;
			this.events = events;
		}

		public Polygon getArrow() {
			return arrow;
		}
		
		public Path getPath() {
			return path;
		}
		
		public List<EventData> getEvents() {
			return events;
		}
		
		private Polygon arrow;
		private Path path;
		private List<EventData> events;
	}
}
