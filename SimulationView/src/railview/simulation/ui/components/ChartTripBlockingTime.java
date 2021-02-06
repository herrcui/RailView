package railview.simulation.ui.components;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import railview.simulation.graph.trainrunmonitor.TripMonitorPaneController;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.DraggableChart;

/**
 * A class representing a Chart with BlockingTime inside. For each blockingTime
 * there is a rectangle drawn in the chart. The BlockingTimeChart has an eventLabel, an eventTable
 * and a controller.
 * 
 * @param <X>
 * @param <Y>
 */
public class ChartTripBlockingTime<X, Y> extends DraggableChart<X, Y> {
	
	private List<BlockingTime> blockingTimes;
	private List<TimeDistance> timeDistances;
	private Map<TimeDistance, List<EventData>> eventsMap;
	private NumberAxis yAxis;
	private NumberAxis xAxis;

	private List<Polygon> polygonList = new ArrayList<Polygon>();

	private Label eventLabel;
	private TableView<TableProperty> eventTable;
	private TripMonitorPaneController controller;
	private Rectangle rectangle;

	public static ChartTripBlockingTime<Number, Number> createBlockingTimeForTripChart(
			Label eventLabel,
			TableView<TableProperty> eventTable,
			TripMonitorPaneController controller) {
		
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();

		ChartTripBlockingTime<Number, Number> chart = new ChartTripBlockingTime<Number, Number>(
				xAxis, yAxis, eventLabel, eventTable, controller);
				
		chart.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().equals(MouseButton.PRIMARY)) {
					chart.getBlockingTimeChartPlotChildren().clear();
					chart.getData().clear();
				}

				chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
					public void handle(MouseEvent event) {
						if (event.getButton().equals(MouseButton.SECONDARY)) {
							chart.getBlockingTimeChartPlotChildren().clear();
							chart.getData().clear();
						}
					}
				});
			}
		});
		
		chart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		chart.startEventHandlers();
		
		xAxis.setSide(Side.TOP);

		return chart;
	}
	
	private ChartTripBlockingTime(Axis<X> xAxis, Axis<Y> yAxis,
			Label eventLabel,
			TableView<TableProperty> eventTable,
			TripMonitorPaneController controller) {
		super(xAxis, yAxis);
		this.eventLabel = eventLabel;
		this.eventTable = eventTable;
		this.controller = controller;
		
		this.xAxis = (NumberAxis) this.getXAxis();
		this.yAxis = (NumberAxis) this.getYAxis();
		
		AnchorPane.setTopAnchor(this, 0.0);
		AnchorPane.setLeftAnchor(this, 0.0);
		AnchorPane.setRightAnchor(this, 0.0);
		AnchorPane.setBottomAnchor(this, 0.0);
	}

	public ObservableList<Node> getBlockingTimeChartPlotChildren() {
		return this.getPlotChildren();
	}

	public void setBlockingTime(List<BlockingTime> blockingTimes) {
		this.blockingTimes = blockingTimes;
	}
	
	public void setTimeDistances(List<TimeDistance> timeDistances) {
		this.timeDistances = timeDistances;
		this.setChartBound();
	}

	public void setEventsMap(Map<TimeDistance, List<EventData>> eventsMap) {
		this.eventsMap = eventsMap;
	}
	
	public void setChartBound() {
		if (this.timeDistances != null) {
			TimeDistance lastTD = this.timeDistances.get(this.timeDistances.size()-1);
			
			xAxis.setAutoRanging(false);
			xAxis.setLowerBound(0);
			xAxis.setUpperBound(lastTD.getDistance());
			yAxis.setAutoRanging(false);
			yAxis.setLowerBound(-1 * lastTD.getSecond());
			yAxis.setUpperBound(-1 * this.timeDistances.get(0).getSecond());
			
			xAxis.setTickUnit(100);
			yAxis.setTickUnit(600);
			
		}
	}


	public List<Polygon> getInformation(Label label) {
		for (Polygon polygon : polygonList) {
			polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
				}
			});
		}
		return this.polygonList;

	}

	private void writeText(double x, double y, List<EventData> eventList) {
		eventLabel.setVisible(true);
		String text = "";
		for (EventData data : eventList) {
			text += data.getEventName() + "\n";
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

	/**
	 * add the rectangles on top of the blockingTimeChart
	 */
	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();
		
		this.drawBlockingTime();
		this.drawEvents();
		this.drawTimeDistances();
	}
	
	private void drawBlockingTime() {
		if (this.blockingTimes != null) {
			for (BlockingTime blockingTime : this.blockingTimes) {
				rectangle = new Rectangle();
				this.getPlotChildren().add(rectangle);
				
				rectangle.setX(this.mapToChart(blockingTime.getRelativeStartDistance(), true));
				rectangle.setY(this.mapToChart(blockingTime.getRelativeStartTimeInSecond(), false));
				rectangle.setWidth(this.mapToChart(blockingTime.getRelativeEndDistance(), true) -
						this.mapToChart(blockingTime.getRelativeStartDistance(), true));
				rectangle.setHeight(this.mapToChart(blockingTime.getRelativeEndTimeInSecond(), false) -
						this.mapToChart(blockingTime.getRelativeStartTimeInSecond(), false));
				
				rectangle.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
			}
		}
	}
	
	private void drawTimeDistances() {
		if (this.timeDistances != null) {
			// Iterator to get the current and next distance and time value
			
			Iterator<TimeDistance> it = timeDistances.iterator();
			TimeDistance previous = null;
			if (it.hasNext()) {
				previous = it.next();
			}
			
			while (it.hasNext()) {
				TimeDistance current = it.next();
				if (current.getDistance() == -1) { // ignore the current point with -1 distance
					previous = null;
					continue;
				}
				
				if (previous == null) { // ignore previous with -1 distance, save current as previous
					previous = current;
					continue;
				}
				
				// Process previous and current here.
				Line polyLine = new Line();
				polyLine.setStartX(mapToChart(previous.getDistance(), true));
				polyLine.setEndX(mapToChart(current.getDistance(), true));
				polyLine.setStartY(mapToChart(previous.getSecond(), false));
				polyLine.setEndY(mapToChart(current.getSecond(), false));
				
				this.getPlotChildren().add(polyLine);
				
				previous = current;
			}
		}
	}
	
	private void drawEvents() {
		if (this.eventsMap != null) {
			for (Map.Entry<TimeDistance, List<EventData>> entry : this.eventsMap.entrySet()) {
				TimeDistance td = entry.getKey();
				double offset = 6.0;
				
				Polygon polygon = new Polygon();
				polygon.getPoints().addAll(new Double[] {
					this.mapToChart(td.getDistance(), true),
					this.mapToChart(td.getSecond(), false) - offset,
					this.mapToChart(td.getDistance(), true) + offset,
					this.mapToChart(td.getSecond(), false),		
					
					this.mapToChart(td.getDistance(), true),
					this.mapToChart(td.getSecond(), false) + offset,
					this.mapToChart(td.getDistance(), true) - offset,
					this.mapToChart(td.getSecond(), false),
				});
	
	
				this.addEventHandle(polygon, td, entry.getValue());
				
				this.getPlotChildren().add(polygon);
				polygonList.add(polygon);
			}
		}
	}
	
	private void addEventHandle(Polygon polygon, TimeDistance td, List<EventData> eventList) {
		polygon.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (td.getSecond() < -(yAxis.getLowerBound() / 2)
						&& td.getDistance() <= xAxis.getUpperBound() / 2) {
					eventLabel.setLayoutX(105);
					eventLabel.setLayoutY(50);
				}

				else if (td.getSecond() < -(yAxis.getLowerBound() / 2)
						&& td.getDistance() > xAxis.getUpperBound() / 2) {
					eventLabel.setLayoutX(-125);
					eventLabel.setLayoutY(50);
				}

				else if (td.getSecond() >= -(yAxis.getLowerBound() / 2)
						&& td.getDistance() <= xAxis.getUpperBound() / 2) {
					eventLabel.setLayoutX(105);
					eventLabel.setLayoutY(-70);
				}

				else {
					eventLabel.setLayoutX(-125);
					eventLabel.setLayoutY(-70);
				}

				ObservableList<TableProperty> observableEventList = FXCollections.observableArrayList();
				observableEventList.add(new TableProperty(
						"Time [s]",	String.format("%1$,.2f", td.getSecond())));
				observableEventList.add(new TableProperty(
						"Distance [m]", String.format("%1$,.2f", td.getDistance())));
				int index = 1;
				for (EventData e : eventList) {
					observableEventList.add(new TableProperty(index	+ ". Event Name", e.getEventName()));
					observableEventList.add(new TableProperty("   Description", e.getText()));
					index++;
				}

				if (eventTable == null) {
					System.out.println("eventtable is null");
				}
				eventTable.setItems(observableEventList);

				writeText((td.getDistance() + 3), (td.getSecond() - 3),	eventList);
				polygon.setFill(Color.CRIMSON);
			}

		});

		polygon.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				removeText();
				polygon.setFill(Color.BLACK);

			}
		});

		polygon.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				controller.drawEventOnSnap(td);
			}
		});
	}
	
	private double mapToChart(double origin, boolean isXAxis) {
		if (isXAxis) {
			return this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(origin));
		}
		else {
			return this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(-1.0 * origin));
		}
	}
}
