package railview.simulation.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.events.ScheduledEvent;
import railapp.simulation.events.totrain.AbstractEventToTrain;
import railapp.simulation.events.totrain.UpdateLocationEvent;
import railapp.simulation.infrastructure.PartialRouteResource;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
import railapp.units.Duration;
import railapp.units.Length;
import railapp.units.Time;
import railapp.units.UnitUtility;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.StringConverter;
import javafx.util.converter.TimeStringConverter;

public class GraphPaneController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	private TabPane tabPane;

	@FXML
	private AnchorPane runningPane;

	@FXML
	private AnchorPane speedprofilePane;

	@FXML
	private AnchorPane timeDistancePane;

	@FXML
	private ListView<String> trainNumbers;

	@FXML
	public void initialize() {
		tabPane.setSide(Side.BOTTOM);

		speedProfileChart = createVelocityChart();
		timeDistanceChart = createCourseForTimeChart();

		speedprofilePane.getChildren().add(speedProfileChart);
		timeDistancePane.getChildren().add(timeDistanceChart);

		AnchorPane.setTopAnchor(timeDistanceChart, 0.0);
		AnchorPane.setLeftAnchor(timeDistanceChart, 0.0);
		AnchorPane.setRightAnchor(timeDistanceChart, 0.0);
		AnchorPane.setBottomAnchor(timeDistanceChart, 0.0);

		AnchorPane.setTopAnchor(speedProfileChart, 0.0);
		AnchorPane.setLeftAnchor(speedProfileChart, 0.0);
		AnchorPane.setRightAnchor(speedProfileChart, 0.0);
		AnchorPane.setBottomAnchor(speedProfileChart, 0.0);

		new ZoomOnlyX(speedProfileChart, speedprofilePane);
		new Zoom(timeDistanceChart, timeDistancePane);

		timeDistanceChart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		timeDistanceChart.startEventHandlers();

		speedProfileChart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() == MouseButton.PRIMARY) {
				} else {
					mouseEvent.consume();
				}
			}
		});
		speedProfileChart.startEventHandlers();

	}

	@FXML
	private void resetSpeedProfile(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				speedProfileChart.getXAxis().setAutoRanging(true);
				speedProfileChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	@FXML
	private void resetTimeDistance(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				timeDistanceChart.getXAxis().setAutoRanging(true);
				timeDistanceChart.getYAxis().setAutoRanging(true);
			}
		}
	}

	private DraggableChart<Number, Number> createVelocityChart() {
		NumberAxis xAxis = createXAxis();
		NumberAxis yAxis = createYAxis();
		DraggableChart<Number, Number> chart = new DraggableChart<>(xAxis,
				yAxis);
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		trainNumbers.getSelectionModel().selectedItemProperty()
				.addListener(new ChangeListener<String>() {
					@Override
					public void changed(
							ObservableValue<? extends String> observable,
							String oldValue, String newValue) {
						System.out.println(getTrain(newValue));
						chart.getData().clear();
						speedProfileChart.getXAxis().setAutoRanging(true);
						speedProfileChart.getYAxis().setAutoRanging(true);
						drawVelocityTable(getTrain(newValue), chart);

					}
				});
		return chart;
	}

	private BlockingTimeChart<Number, Number> createCourseForTimeChart() {

		NumberAxis xAxis = createXAxis2();
		NumberAxis yAxis = createYAxis2();
		BlockingTimeChart<Number, Number> chart = new BlockingTimeChart<Number, Number>(
				xAxis, yAxis);
		
		trainNumbers.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				AbstractTrainSimulator train = getTrain(trainNumbers.getSelectionModel().getSelectedItem().toString());
				
				if (chart.getData().isEmpty()) {
					try {						
						chart.getData().clear();
						chart.getBlockingTimeChartPlotChildren().clear();
						timeDistanceChart.getXAxis().setAutoRanging(true);
						timeDistanceChart.getYAxis().setAutoRanging(true);
						drawCourseforTimeTable(getTrain(trainNumbers
								.getSelectionModel().getSelectedItem()
								.toString()), chart);

						chart.setBlockingTime(getBlockingTimeStairway(train));
						
						chart.setEventsMap(getEvents(train, getTimeInDistance(train)));
						
						chart.setAnimated(false);
						chart.setCreateSymbols(false);
						
						Time startTime = train.getTripSection().getStartTime();

						yAxis.setTickLabelFormatter(new StringConverter<Number>() {
							@Override
							public String toString(Number t) {
								Time testTime = startTime.add(Duration.fromTotalSecond(
									-t.doubleValue()));
								return testTime.toString();
							
							}

							@Override
							public Number fromString(String string) {
								return 1;
							}
						});
				
						
						Thread.sleep(500);
						chart.getBlockingTimeChartPlotChildren().clear();
						chart.getData().clear();
						drawCourseforTimeTable(getTrain(trainNumbers
								.getSelectionModel().getSelectedItem()
								.toString()), chart);
						// TODO arrows for events

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				else {
					chart.getData().clear();
					chart.getBlockingTimeChartPlotChildren().clear();
					timeDistanceChart.getXAxis().setAutoRanging(true);
					timeDistanceChart.getYAxis().setAutoRanging(true);
					drawCourseforTimeTable(getTrain(trainNumbers
							.getSelectionModel().getSelectedItem().toString()),
							chart);
					chart.setBlockingTime(getBlockingTimeStairway( getTrain(trainNumbers
							.getSelectionModel().getSelectedItem().toString())));
					chart.setAnimated(false);
					chart.setCreateSymbols(false);
					Time startTime = getTrain(
							trainNumbers.getSelectionModel().getSelectedItem()
									.toString()).getTripSection()
							.getStartTime();

					// TODO arrows for events
					// chart.drawArrow(400, 400, 200, 1000);
					// chart.drawArrow2(400, 200, 400, 1000);
					/**
					 * for(Map.Entry<TimeDistance,List<Event>> entry :
					 * getEvents().entrySet()){ TimeDistance td =
					 * entry.getKey(); List<Event> eventList = entry.getValue();
					 * chart.drawArrow2(td.getMeter(), td.getSecond(),
					 * td.getMeter(), td.getSecond()+1000); double x =
					 * td.getMeter(); double y = td.getSecond()+1150; for(Event
					 * events: eventList){ //chart.writeText(x, y,
					 * events.getText()); if(events.getType() == 0){
					 * chart.drawCircle(x, y, 100, Color.YELLOW); }
					 * if(events.getType() == 1){ chart.drawCircle(x, y, 100,
					 * Color.GREEN); } if(events.getType() == -1){
					 * chart.drawCircle(x, y, 100, Color.RED); } y = y + 200; }
					 * }
					 **/

					yAxis.setTickLabelFormatter(new StringConverter<Number>() {

						@Override
						public String toString(Number t) {
							Time testTime = startTime.add(Duration.fromTotalSecond(
								-t.doubleValue()));
							return testTime.toString();
						
						}

						@Override
						public Number fromString(String string) {
							return 1;
						}
					});
				}

				chart.setOnMouseDragged(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {

						if (event.getButton().equals(MouseButton.PRIMARY)) {

							chart.getBlockingTimeChartPlotChildren().clear();
							chart.getData().clear();

							drawCourseforTimeTable(getTrain(trainNumbers
									.getSelectionModel().getSelectedItem()
									.toString()), chart);
						}

						chart.setOnMouseReleased(new EventHandler<MouseEvent>() {
							public void handle(MouseEvent event) {
								if (event.getButton().equals(
										MouseButton.SECONDARY)) {
									chart.getBlockingTimeChartPlotChildren()
											.clear();
									chart.getData().clear();
									drawCourseforTimeTable(
											getTrain(trainNumbers
													.getSelectionModel()
													.getSelectedItem()
													.toString()), chart);
								}
							}
						});

					}
				});
			}

		});
		xAxis.setSide(Side.TOP);

		return chart;

	}

	private NumberAxis createXAxis() {
		NumberAxis xAxis = new NumberAxis();
		return xAxis;
	}

	private NumberAxis createYAxis() {
		NumberAxis yAxis = new NumberAxis();

		return yAxis;
	}

	private NumberAxis createXAxis2() {
		NumberAxis xAxis = new NumberAxis();
		return xAxis;
	}

	private NumberAxis createYAxis2() {
		NumberAxis yAxis = new NumberAxis();
		return yAxis;
	}

	public void setTrainList(List<AbstractTrainSimulator> trainList) {
		this.updateTrainMap(trainList);
	}

	public void updateTrainMap(List<AbstractTrainSimulator> trainList) {
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(),
					trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE
					&& !numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}
		trainNumbers.setItems(numbers);

	}

	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}

	public LineChart<Number, Number> drawVelocityTable(
			AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("course for velocity");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for (Map.Entry<Double, Double> entry : getCourseForVelocity(train)
					.entrySet()) {
				CourseForVelocitySeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
			}
			chart.getData().add(CourseForVelocitySeries);
			XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();

			speedLimitSeries.setName("speedlimit");
			double y = -1;
			speedLimitSeries.getData().add(new Data<Number, Number>(0, y));
			for (Map.Entry<Double, Double> entry : getSpeedLimit(train)
					.entrySet()) {
				if (y >= 0) {
					speedLimitSeries.getData().add(
							new Data<Number, Number>(entry.getKey(), y));
				}
				speedLimitSeries.getData().add(
						new Data<Number, Number>(entry.getKey(), entry
								.getValue()));
				y = entry.getValue();
			}
			chart.getData().add(speedLimitSeries);
			speedLimitSeries
					.nodeProperty()
					.get()
					.setStyle(
							"-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");

			chart.setCreateSymbols(false);
		}
		return chart;
	}

	public BlockingTimeChart<Number, Number> drawCourseforTimeTable(
			AbstractTrainSimulator train,
			BlockingTimeChart<Number, Number> chart) {
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			List<TimeDistance> timeDistances = this.getTimeInDistance(train);
			
			for (TimeDistance point : timeDistances) {
				courseForTimeSeries.getData().add(
						new Data<Number, Number>(point.getMeter(), (point
								.getSecond() * -1)));
			}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}

		return chart;
	}

	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForVelocity(
			AbstractTrainSimulator train) {
		Map<Double, Double> velocityMap = new LinkedHashMap<Double, Double>();
		double meter = 0; // x
		double velocityInKmH = 0; // y
		for (DiscretePoint point : train.getWholeCoursePoints()) {
			velocityInKmH = point.getVelocity().getKilometerPerHour();
			meter += point.getDistance().getMeter();
			velocityMap.put(meter, velocityInKmH);
		}
		return velocityMap;
	}

	// Map: Meter, TimeInSecond
	private List<TimeDistance> getTimeInDistance(AbstractTrainSimulator train) {
		List<TimeDistance> pointList = new ArrayList<TimeDistance>();
		double meter = 0; // x
		double timeInSecond = 0; // y

		for (DiscretePoint point : train.getWholeCoursePoints()) {
			timeInSecond += point.getDuration().getTotalSecond();
			meter += point.getDistance().getMeter();
			pointList.add(new TimeDistance(meter, timeInSecond));
		}
		return pointList;
	}

	private LinkedHashMap<Double, Double> getSpeedLimit(
			AbstractTrainSimulator train) {
		// Velocity
		LinkedHashMap<Double, Double> speedLimitMap = new LinkedHashMap<Double, Double>();

		LinkPath path = train.getFullPath();

		double maxTrainKmH = train.getTrainDefinition().getMaxVelocity()
				.getKilometerPerHour();
		double maxKmH = Math
				.min(maxTrainKmH, path.getLinkEdges().get(0).getLink()
						.getGeometry().getMaxVelocity().getKilometerPerHour());
		double lastMeter = 0;
		double meter = 0;

		for (LinkEdge edge : path.getLinkEdges()) {
			if (edge.getLength().getMeter() == 0) {
				continue;
			}

			double linkKmH = edge.getLink().getGeometry().getMaxVelocity()
					.getKilometerPerHour();

			if (Math.abs(Math.min(maxTrainKmH, linkKmH) - maxKmH) >= UnitUtility.ERROR) {
				speedLimitMap.put(lastMeter, maxKmH);
				speedLimitMap.put(meter, maxKmH);

				maxKmH = Math.min(maxTrainKmH, linkKmH);
				lastMeter = meter;
			}

			meter += edge.getLength().getMeter();
		}

		speedLimitMap.put(lastMeter, maxKmH);
		speedLimitMap.put(meter, maxKmH);

		return speedLimitMap;
	}

	private List<BlockingTime> getBlockingTimeStairway(
			AbstractTrainSimulator train) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();
		if (train instanceof TrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;

			List<PartialRouteResource> resources = ((TrainSimulator) train)
					.getBlockingTimeStairWay();
			Time trainStartTime = train.getTripSection().getStartTime();

			for (PartialRouteResource resource : resources) {
				if (headDistanceInFirstResource == null) {
					headDistanceInFirstResource = resource.getPath()
							.findFirstDistance(
									(InfrastructureObject) train
											.getTripSection().getTripElements()
											.get(0).getOperationalPoint());
					if (headDistanceInFirstResource == null) {
						continue;
					}
				}

				if (resource.getReleaseTime() == null) {
					break;
				}

				double startMeter = meter;
				double endMeter = meter
						+ resource.getPartialRoute().getPath().getLength()
								.getMeter();
				double startTimeInSecond = resource.getGrantTime()
						.getDifference(trainStartTime).getTotalSecond();
				double endTimeInSecond = resource.getReleaseTime()
						.getDifference(trainStartTime).getTotalSecond();

				if (meter == 0) { // for the first resource
					endMeter = endMeter
							- headDistanceInFirstResource.getMeter();
				}

				blockingTimes.add(new BlockingTime(startMeter, endMeter,
						startTimeInSecond, endTimeInSecond));

				meter = endMeter;
			}

		}

		return blockingTimes;
	}

	private Map<TimeDistance, List<Event>> getEvents(AbstractTrainSimulator train, List<TimeDistance> timeDistances) {
		Map<TimeDistance, List<Event>> eventsMap = new HashMap<TimeDistance, List<Event>>();

		for (ScheduledEvent scheduledEvent : train.getEvents()) {
			if (scheduledEvent instanceof UpdateLocationEvent) {
				continue;
			}
			
			double second = scheduledEvent.getScheduleTime().getDifference(
					train.getTripSection().getStartTime()).getTotalSecond();
			double currentSecond = 0;
			double currentMeter = 0;
			
			for (TimeDistance point : timeDistances) {
				if (point.getSecond() + currentSecond >= second) {
					double factor = (point.getSecond() + currentSecond - second)/point.getSecond();
					currentMeter += factor * point.getMeter();
					break;
				} else {
					currentSecond += point.getSecond();
					currentMeter += point.getMeter();
				}
			}
			
			TimeDistance entry = new TimeDistance(second, currentMeter);
			int type = Event.IN;
			if (scheduledEvent.getSource().equals(train)) {
				type = Event.SELF;
			}
			
			String text = scheduledEvent instanceof AbstractEventToTrain ?
					((AbstractEventToTrain) scheduledEvent).getEventString() : scheduledEvent.toString();
			
			List<Event> events = eventsMap.get(entry);
			if (events == null) {
				events = new ArrayList<Event>();
				eventsMap.put(entry, events);
			}
			events.add(new Event(entry, type, text));
		}
		
		return eventsMap;
	}

	DraggableChart<Number, Number> timeDistanceChart;
	DraggableChart<Number, Number> speedProfileChart;

	ObservableList<String> numbers = FXCollections.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap = new ConcurrentHashMap<String, AbstractTrainSimulator>();

	class TimeDistance {
		TimeDistance(double meter, double second) {
			this.meter = meter;
			this.second = second;
		}

		public double getMeter() {
			return this.meter;
		}

		public double getSecond() {
			return this.second;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			long temp;
			temp = Double.doubleToLongBits(meter);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(second);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TimeDistance other = (TimeDistance) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (Double.doubleToLongBits(meter) != Double
					.doubleToLongBits(other.meter))
				return false;
			if (Double.doubleToLongBits(second) != Double
					.doubleToLongBits(other.second))
				return false;
			return true;
		}

		private double meter;
		private double second;

		private GraphPaneController getOuterType() {
			return GraphPaneController.this;
		}
	}

	class BlockingTime {
		BlockingTime(double startMeter, double endMeter,
				double startTimeInSecond, double endTimeInSecond) {
			super();
			this.startMeter = startMeter;
			this.endMeter = endMeter;
			this.startTimeInSecond = startTimeInSecond;
			this.endTimeInSecond = endTimeInSecond;
		}

		public double getStartMeter() {
			return startMeter;
		}

		public double getEndMeter() {
			return endMeter;
		}

		public double getStartTimeInSecond() {
			return startTimeInSecond;
		}

		public double getEndTimeInSecond() {
			return endTimeInSecond;
		}

		private double startMeter;
		private double endMeter;
		private double startTimeInSecond;
		private double endTimeInSecond;
	}

	class Event {
		Event(TimeDistance timeDistance, int type, String text) {
			super();
			this.timeDistance = timeDistance;
			this.type = type;
			this.text = text;
		}

		Event(double meter, double time, int type, String text) {
			super();
			this.timeDistance = new TimeDistance(meter, time);
			this.type = type;
			this.text = text;
		}

		public TimeDistance getTimeDistance() {
			return timeDistance;
		}

		public int getType() {
			return type;
		}

		public String getText() {
			return text;
		}

		TimeDistance timeDistance;
		private int type;
		private String text;

		static final int SELF = 0;
		static final int IN = 1;
		static final int OUT = -1;
	}

	class BlockingTimeChart<X, Y> extends DraggableChart<X, Y> {

		public BlockingTimeChart(Axis<X> xAxis, Axis<Y> yAxis) {
			super(xAxis, yAxis);
		}
		
		public ObservableList<Node> getBlockingTimeChartPlotChildren() {
			return this.getPlotChildren();
		}

		void setBlockingTime(List<BlockingTime> blockingTimes) {
			this.blockingTimes = blockingTimes;
		}
		
		void setEventsMap(Map<TimeDistance, List<Event>> eventsMap) {
			this.eventsMap = eventsMap;
		}

		private void drawArrow(double startx, double starty, double endx,
				double endy) {
			path = new Path();
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

			this.getPlotChildren().addAll(path, arrow);
		}

		private void drawCircle(double centerX, double centerY, double radius,
				Paint value, Event events) {
			circle = new Circle();
			circle.setCenterX(this.getXAxis().getDisplayPosition(
					this.getXAxis().toRealValue(centerX)));
			circle.setCenterY(this.getYAxis().getDisplayPosition(
					this.getYAxis().toRealValue(-centerY)));
			circle.setRadius(this.getYAxis().getDisplayPosition(
					this.getYAxis().toRealValue(-radius)));
			circle.setFill(value);
			this.getPlotChildren().add(circle);
			
			circle.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					events.getText();
					writeText(centerX + 300, -centerY, events.getText());

				}
			});
			circle.setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					removeText();

				}
			});
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

		
				for (Map.Entry<TimeDistance, List<Event>> entry : this.eventsMap.entrySet()) {
					TimeDistance td = entry.getKey();
					List<Event> eventList = entry.getValue();
					yAxis = (NumberAxis) this.getYAxis();

					Circle circle = new Circle();
					circle.setCenterX(this.getXAxis().getDisplayPosition(
							this.getXAxis().toRealValue(td.getMeter())));
					circle.setCenterY(this.getYAxis().getDisplayPosition(
							this.getYAxis().toRealValue(-td.getSecond())));
					circle.setRadius(this.getYAxis().getDisplayPosition(
							this.getYAxis().toRealValue(-40)));
					circle.setFill(Color.BLACK);
					this.getPlotChildren().add(circle);

					circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent t) {
							if (td.getSecond() < -(yAxis.getLowerBound() / 2)) {
								double x = td.getMeter();
								double y = -((yAxis.getLowerBound() / 3) * 2);
								drawArrow(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) * 2) - 65);
								for (Event events : eventList) {
									// chart.writeText(x, y, events.getText());
									if (events.getType() == 0) {
										drawCircle(x, y, 40, Color.ORANGE,
												events);
									}
									if (events.getType() == 1) {
										drawCircle(x, y, 40, Color.GREEN,
												events);
									}
									if (events.getType() == -1) {
										drawCircle(x, y, 40, Color.RED, events);
									}
									y = y + 80;
								}
							} else {
								double x = td.getMeter();
								double y = -((yAxis.getLowerBound() / 3));
								drawArrow(td.getMeter(), td.getSecond(),
										td.getMeter(),
										-((yAxis.getLowerBound() / 3) - 65));
								for (Event events : eventList) {
									// chart.writeText(x, y, events.getText());
									if (events.getType() == 0) {
										drawCircle(x, y, 40, Color.ORANGE,
												events);
									}
									if (events.getType() == 1) {
										drawCircle(x, y, 40, Color.GREEN,
												events);
									}
									if (events.getType() == -1) {
										drawCircle(x, y, 40, Color.RED, events);
									}
									y = y - 80;
								}
							}
						}
					});

				}

			}
		}

		private List<BlockingTime> blockingTimes;
		private Map<TimeDistance, List<Event>> eventsMap;
		private NumberAxis yAxis;
		Circle circle;
		Text txt;
		Rectangle r;
		Path path;
		Label label;
	}
}
