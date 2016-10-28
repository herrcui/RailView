package railview.simulation.ui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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
import javafx.scene.text.TextAlignment;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TimeDistance;

public class BlockingTimeChart<X, Y> extends DraggableChart<X, Y> {
	public BlockingTimeChart(Axis<X> xAxis, Axis<Y> yAxis, Label eventLabel, Label label, CheckBox selfEventCheckBox, CheckBox inEventCheckBox, CheckBox outEventCheckBox) {
		super(xAxis, yAxis);
		this.eventLabel = eventLabel;
		this.informationLabel = label;
		this.selfEventCheckBox = selfEventCheckBox;
		this.incomingEventCheckBox = inEventCheckBox;
		this.outgoingEventCheckBox = outEventCheckBox;
		
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
	

	private Path drawPath(double startx, double starty, double endx, double endy) {
		Path path = new Path();
		path.getElements().add(
				new MoveTo(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(startx)), this.getYAxis()
						.getDisplayPosition(
								this.getYAxis().toRealValue(-starty))));
		path.getElements()
				.add(new LineTo(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(endx)), this.getYAxis()
						.getDisplayPosition(this.getYAxis().toRealValue(-endy))));

		return path;
	}
	
	public List<Polygon> getInformation(Label label){
		 for(Polygon polygon : polygonList) {
			 	polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
			 			@Override
			 			public void handle(MouseEvent event) {
						}
			 		});
	        }
		 return this.polygonList;

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
		double centerPositon = this.getYAxis().getDisplayPosition(
				this.getYAxis().toRealValue(centerY));
		this.getPlotChildren().add(circle);

		circle.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent t) {
				NumberAxis xAxis = (NumberAxis) getXAxis();
				NumberAxis yAxis = (NumberAxis) getYAxis();
				String eventString = ("Event: " + event.getText());
				// writeText(xAxis.getUpperBound()/2 - xAxis.getUpperBound()/10,
				// yAxis.getLowerBound()/12*11 , eventString);

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
	
	private void selfEvent(){
		 selfEventCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
	           public void changed(ObservableValue<? extends Boolean> ov,
	             Boolean old_val, Boolean new_val) {
	        	   
	          }
	        });
	}

	private void writeText(double x, double y, List<EventData> eventList) {
		eventLabel.setVisible(true);
		String text = "";
		for (EventData data : eventList) {
			text += data.getText() + "\n";
		}
		eventLabel.setText(text);
		eventLabel.setTranslateX(this.getXAxis().getDisplayPosition(
				this.getXAxis().toRealValue(x)));
		eventLabel.setTranslateY(this.getYAxis().getDisplayPosition(
				this.getYAxis().toRealValue(-y)));
		eventLabel.setMinSize(200, 100);
		eventLabel
				.setStyle("-fx-border-color: black; -fx-background-color: white");
		eventLabel.setPadding(new Insets(0, 0, 0, 10));
		eventLabel.toFront();
	}

	private void removeText() {
		eventLabel.setVisible(false);
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
				r.setWidth(this.getXAxis()
						.getDisplayPosition(
								this.getXAxis().toRealValue(
										blockingTime.getEndMeter()))
						- this.getXAxis().getDisplayPosition(
								this.getXAxis().toRealValue(
										blockingTime.getStartMeter())));
				r.setHeight(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(
								-(blockingTime.getEndTimeInSecond())))
						- this.getYAxis()
								.getDisplayPosition(
										this.getYAxis()
												.toRealValue(
														-(blockingTime
																.getStartTimeInSecond()))));
				r.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
			}

			for (Map.Entry<TimeDistance, List<EventData>> entry : this.eventsMap
					.entrySet()) {
				TimeDistance td = entry.getKey();
				List<EventData> eventList = entry.getValue();
				yAxis = (NumberAxis) this.getYAxis();
				xAxis = (NumberAxis) this.getXAxis();

				Polygon polygon = new Polygon();
				polygon.getPoints()
						.addAll(new Double[] {
								(this.getXAxis().getDisplayPosition(this
										.getXAxis().toRealValue(td.getMeter()))),

								(this.getYAxis().getDisplayPosition(
										this.getYAxis().toRealValue(
												-td.getSecond())) - 6.0),

								(this.getXAxis().getDisplayPosition(
										this.getXAxis().toRealValue(
												td.getMeter())) + 6.0),

								(this.getYAxis().getDisplayPosition(this
										.getYAxis()
										.toRealValue(-td.getSecond()))),

								(this.getXAxis().getDisplayPosition(this
										.getXAxis().toRealValue(td.getMeter()))),

								(this.getYAxis().getDisplayPosition(
										this.getYAxis().toRealValue(
												-td.getSecond())) + 6.0),

								(this.getXAxis().getDisplayPosition(
										this.getXAxis().toRealValue(
												td.getMeter())) - 6.0),

								(this.getYAxis().getDisplayPosition(this
										.getYAxis()
										.toRealValue(-td.getSecond()))),

						});
				this.getPlotChildren().add(polygon);
				for (EventData data : eventList) {		
					if (data.getType() == 0 ){
						selfEventPolygonList.add(polygon);
					}
					else if (data.getType() == 1){
						incomingEventPolygonList.add(polygon);
					}
					else {
						outgoingEventPolygonList.add(polygon);
					}
				}
				polygonList.add(polygon);
				
				selfEventCheckBox.setSelected(false);
				selfEventCheckBox.setOnAction((e) -> {
			        if (selfEventCheckBox.isSelected()) {
			        	for(Polygon poly: selfEventPolygonList){
			        		poly.setFill(Color.ORANGE);
			        	}
			        } else {
			        	for(Polygon poly: selfEventPolygonList){
			        	 	poly.setFill(Color.BLACK);
			        	}
			        }
			        });
				
				incomingEventCheckBox.setSelected(false);
				incomingEventCheckBox.setOnAction((e) -> {
			        if (incomingEventCheckBox.isSelected()) {
			        	for(Polygon poly: incomingEventPolygonList){
			        		poly.setFill(Color.ORANGE);
			        	}
			        } else {
			        	for(Polygon poly: incomingEventPolygonList){
			        	 	poly.setFill(Color.BLACK);
			        	}
			        }
			        });
				
				outgoingEventCheckBox.setSelected(false);
				outgoingEventCheckBox.setOnAction((e) -> {
			        if (outgoingEventCheckBox.isSelected()) {
			        	for(Polygon poly: outgoingEventPolygonList){
			        		poly.setFill(Color.ORANGE);
			        	}
			        } else {
			        	for(Polygon poly: outgoingEventPolygonList){
			        	 	poly.setFill(Color.BLACK);
			        	}
			        }
			        });

				polygon.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (td.getSecond() < -(yAxis.getLowerBound() / 2)
								&& td.getMeter() <= xAxis.getUpperBound() / 2) {
							eventLabel.setLayoutX(105);
							eventLabel.setLayoutY(50);
						}

						else if (td.getSecond() < -(yAxis.getLowerBound() / 2)
								&& td.getMeter() > xAxis.getUpperBound() / 2) {
							eventLabel.setLayoutX(-125);
							eventLabel.setLayoutY(50);
						}

						else if (td.getSecond() >= -(yAxis.getLowerBound() / 2)
								&& td.getMeter() <= xAxis.getUpperBound() / 2) {
							eventLabel.setLayoutX(105);
							eventLabel.setLayoutY(-70);
						}

						else {
							eventLabel.setLayoutX(-125);
							eventLabel.setLayoutY(-70);
						}

						writeText((td.getMeter() + 3), (td.getSecond() - 3),
								eventList);
						polygon.setFill(Color.CRIMSON);
					}

				});

				polygon.setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						removeText();
						if (selfEventCheckBox.isSelected()) {			        	
							polygon.setFill(Color.ORANGE);
						}
						else{
							polygon.setFill(Color.BLACK);
						}
					}
				});
				
				polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						String text ="";
						for (EventData data : eventList) {		
							String type = "";
							if (data.getType() == 0 ){
								type = "self event";
							}
							else if (data.getType() == 1){
								type = "incoming event";
							}
							else {
								type = "outgoing event";
							}
							
							text += data.getText() + "\n" + type + "\n" + data.getEventName() + "\n" + data.getTimeDistance() + "\n \n";
						}
						informationLabel.setText(text);
					}
				});

				/**
				 * polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
				 * 
				 * @Override public void handle(MouseEvent t) {
				 *           System.out.println(getPlotChildren().toString());
				 *           Polygon arrow = null; Path path = null;
				 * 
				 *           TimeDistanceEvents timeDistanceEvents =
				 *           polygonMap.get(t.getSource()); if
				 *           (timeDistanceEvents == null) { if (td.getSecond() <
				 *           -(yAxis.getLowerBound() / 2)) { double x =
				 *           td.getMeter(); double y = yAxis.getDisplayPosition(
				 *           yAxis.toRealValue(((yAxis.getLowerBound() / 3) *
				 *           2))); arrow = drawArrow(td.getMeter(),
				 *           td.getSecond(), td.getMeter(),
				 *           -((yAxis.getLowerBound() / 3) * 2) - 100); path =
				 *           drawPath(td.getMeter(), td.getSecond(),
				 *           td.getMeter(), -((yAxis.getLowerBound() / 3) * 2) -
				 *           100);
				 * 
				 *           for (EventData event : eventList) { //
				 *           chart.writeText(x, y, events.getText()); if
				 *           (event.getType() == 0) { drawCircle(x, y, 8,
				 *           Color.ORANGE, event); y = y + 16; } if
				 *           (event.getType() == 1) { drawCircle(x, y, 8,
				 *           Color.GREEN, event); y = y + 16; } if
				 *           (event.getType() == -1) { drawCircle(x, y, 8,
				 *           Color.RED, event); y = y + 16; } } } else { double
				 *           x = td.getMeter(); double y =
				 *           yAxis.getDisplayPosition(
				 *           yAxis.toRealValue(((yAxis.getLowerBound() / 3))));
				 *           arrow = drawArrow(td.getMeter(), td.getSecond(),
				 *           td.getMeter(), -((yAxis.getLowerBound() / 3) -
				 *           100)); path = drawPath(td.getMeter(),
				 *           td.getSecond(), td.getMeter(),
				 *           -((yAxis.getLowerBound() / 3) - 100)); for
				 *           (EventData events : eventList) { //
				 *           chart.writeText(x, y, events.getText()); if
				 *           (events.getType() == 0) { drawCircle(x, y, 8,
				 *           Color.ORANGE, events); y = y - 16; } if
				 *           (events.getType() == 1) { drawCircle(x, y, 8,
				 *           Color.GREEN, events); y = y - 16; } if
				 *           (events.getType() == -1) { drawCircle(x, y, 8,
				 *           Color.RED, events); y = y - 16; } } }
				 * 
				 *           getPlotChildren().addAll(arrow, path);
				 *           polygonMap.put(polygon, new
				 *           TimeDistanceEvents(arrow, path, eventList)); } else
				 *           {
				 *           getPlotChildren().remove(timeDistanceEvents.getArrow
				 *           ());
				 *           getPlotChildren().remove(timeDistanceEvents.getPath
				 *           ()); for (EventData event :
				 *           timeDistanceEvents.events) {
				 *           getPlotChildren().remove(circleMap.get(event)); }
				 *           polygonMap.remove(polygon); } }
				 * 
				 *           });
				 * 
				 *           } }
				 **/
			}
		}
	}

	private List<BlockingTime> blockingTimes;
	private Map<TimeDistance, List<EventData>> eventsMap;
	private NumberAxis yAxis;
	private NumberAxis xAxis;

	private Map<EventData, Circle> circleMap = new HashMap<EventData, Circle>();

	private Map<Polygon, TimeDistanceEvents> polygonMap = new HashMap<Polygon, TimeDistanceEvents>();
	
	private List<Polygon> polygonList = new ArrayList<Polygon>();
	private List<Polygon> selfEventPolygonList = new ArrayList<Polygon>();
	private List<Polygon> incomingEventPolygonList = new ArrayList<Polygon>();
	private List<Polygon> outgoingEventPolygonList = new ArrayList<Polygon>();

	private CheckBox incomingEventCheckBox;
	private CheckBox selfEventCheckBox;
	private CheckBox outgoingEventCheckBox;
	private Label eventLabel;
	private Label informationLabel;
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
