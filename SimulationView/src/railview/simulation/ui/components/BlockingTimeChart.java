package railview.simulation.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import railview.simulation.ui.TrainRunMonitorPaneController;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TableProperty;
import railview.simulation.ui.data.TimeDistance;

/**
 * A class representing a Chart with BlockingTime inside. For each blockingTime
 * there is a rectangle drawn in the chart. The BlockingTimeChart has an eventLabel, an eventTable
 * and a controller.
 * 
 * @param <X>
 * @param <Y>
 */
public class BlockingTimeChart<X, Y> extends DraggableChart<X, Y> {

	public BlockingTimeChart(Axis<X> xAxis, Axis<Y> yAxis, Label eventLabel,
			TableView<TableProperty> eventTable,
			TrainRunMonitorPaneController controller) {
		super(xAxis, yAxis);
		this.eventLabel = eventLabel;
		this.eventTable = eventTable;
		this.controller = controller;
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

				polygonList.add(polygon);

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

						ObservableList<TableProperty> observableEventList = FXCollections
								.observableArrayList();
						observableEventList.add(new TableProperty("Time [s]",
								String.format("%1$,.2f", td.getSecond())));
						observableEventList.add(new TableProperty(
								"Distance [m]", String.format("%1$,.2f",
										td.getMeter())));
						int index = 1;
						for (EventData e : eventList) {
							observableEventList.add(new TableProperty(index
									+ ". Event Name", e.getEventName()));
							observableEventList.add(new TableProperty(
									"   Description", e.getText()));
							index++;
						}

						if (eventTable == null) {
							System.out.println("eventtable is null");
						}
						eventTable.setItems(observableEventList);

						writeText((td.getMeter() + 3), (td.getSecond() - 3),
								eventList);
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
		}
	}

	private List<BlockingTime> blockingTimes;
	private Map<TimeDistance, List<EventData>> eventsMap;
	private NumberAxis yAxis;
	private NumberAxis xAxis;

	private List<Polygon> polygonList = new ArrayList<Polygon>();

	private Label eventLabel;
	private TableView<TableProperty> eventTable;
	private TrainRunMonitorPaneController controller;
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
