package railview.simulation.ui;

import java.io.IOException;
import java.net.URL;
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
import railapp.infrastructure.service.IInfrastructureServiceUtility;
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
import railview.infrastructure.container.NetworkPaneController;
import railview.simulation.ui.components.BlockingTimeChart;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.EventData;
import railview.simulation.ui.data.TimeDistance;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

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
	private AnchorPane energyPane;
	
	@FXML
	private AnchorPane snapshotRoot;

	@FXML
	private ListView<String> trainNumbersBlockingTime;

	@FXML
	private ListView<String> trainNumbersRunningDynamics;

	@FXML
	public void initialize() {
		tabPane.setSide(Side.BOTTOM);

		speedProfileChart = createSpeedprofileChart();
		timeDistanceChart = createTimeDistanceChart();

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

		try {
			FXMLLoader snapshotPaneLoader = new FXMLLoader();
			URL location = SnapshotPaneController.class
					.getResource("SnapshotPane.fxml");
			snapshotPaneLoader.setLocation(location);
			snapshotPane = (StackPane) snapshotPaneLoader.load();
			this.snapshotPaneController = snapshotPaneLoader.getController();
			
			this.snapshotRoot.getChildren().add(snapshotPane);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setInfrastructureServiceUtility(
			IInfrastructureServiceUtility infraServiceUtility) {
		this.snapshotPaneController.setInfrastructureServiceUtility(infraServiceUtility);
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

	private DraggableChart<Number, Number> createSpeedprofileChart() {
		NumberAxis xAxis = createXAxis();
		NumberAxis yAxis = createYAxis();
		DraggableChart<Number, Number> chart = new DraggableChart<>(xAxis,
				yAxis);
		chart.setAnimated(false);
		chart.setCreateSymbols(false);
		trainNumbersRunningDynamics.getSelectionModel().selectedItemProperty()
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

	private BlockingTimeChart<Number, Number> createTimeDistanceChart() {

		NumberAxis xAxis = createXAxis2();
		NumberAxis yAxis = createYAxis2();
		BlockingTimeChart<Number, Number> chart = new BlockingTimeChart<Number, Number>(
				xAxis, yAxis);

		trainNumbersBlockingTime
				.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						AbstractTrainSimulator train = getTrain(trainNumbersBlockingTime
								.getSelectionModel().getSelectedItem()
								.toString());

						if (chart.getData().isEmpty()) {
							try {
								chart.getData().clear();
								chart.getBlockingTimeChartPlotChildren()
										.clear();
								timeDistanceChart.getXAxis().setAutoRanging(
										true);
								timeDistanceChart.getYAxis().setAutoRanging(
										true);
								drawCourseforTimeTable(
										getTrain(trainNumbersBlockingTime
												.getSelectionModel()
												.getSelectedItem().toString()),
										chart);

								chart.setBlockingTime(getBlockingTimeStairway(train));

								chart.setEventsMap(getEvents(train,
										getTimeInDistance(train)));

								chart.setAnimated(false);
								chart.setCreateSymbols(false);

								Time startTime = train.getTripSection()
										.getStartTime();

								yAxis.setTickLabelFormatter(new StringConverter<Number>() {
									@Override
									public String toString(Number t) {
										Time testTime = startTime.add(Duration
												.fromTotalSecond(-t
														.doubleValue()));
										return testTime.toString();

									}

									@Override
									public Number fromString(String string) {
										return 1;
									}
								});

								Thread.sleep(500);
								chart.getBlockingTimeChartPlotChildren()
										.clear();
								chart.getData().clear();
								drawCourseforTimeTable(
										getTrain(trainNumbersBlockingTime
												.getSelectionModel()
												.getSelectedItem().toString()),
										chart);
								// TODO arrows for events

							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							chart.getData().clear();
							chart.getBlockingTimeChartPlotChildren().clear();
							timeDistanceChart.getXAxis().setAutoRanging(true);
							timeDistanceChart.getYAxis().setAutoRanging(true);
							drawCourseforTimeTable(
									getTrain(trainNumbersBlockingTime
											.getSelectionModel()
											.getSelectedItem().toString()),
									chart);
							chart.setBlockingTime(getBlockingTimeStairway(getTrain(trainNumbersBlockingTime
									.getSelectionModel().getSelectedItem()
									.toString())));

							chart.setEventsMap(getEvents(train,
									getTimeInDistance(train)));

							chart.setAnimated(false);
							chart.setCreateSymbols(false);
							Time startTime = getTrain(
									trainNumbersBlockingTime
											.getSelectionModel()
											.getSelectedItem().toString())
									.getTripSection().getStartTime();

							yAxis.setTickLabelFormatter(new StringConverter<Number>() {

								@Override
								public String toString(Number t) {
									Time testTime = startTime.add(Duration
											.fromTotalSecond(-t.doubleValue()));
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

								if (event.getButton().equals(
										MouseButton.PRIMARY)) {

									chart.getBlockingTimeChartPlotChildren()
											.clear();
									chart.getData().clear();

									drawCourseforTimeTable(
											getTrain(trainNumbersBlockingTime
													.getSelectionModel()
													.getSelectedItem()
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
													getTrain(trainNumbersBlockingTime
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

		trainNumbersRunningDynamics.setItems(numbers);
		trainNumbersBlockingTime.setItems(numbers);

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

	private Map<TimeDistance, List<EventData>> getEvents(
			AbstractTrainSimulator train, List<TimeDistance> timeDistances) {
		Map<TimeDistance, List<EventData>> eventsMap = new HashMap<TimeDistance, List<EventData>>();

		for (ScheduledEvent scheduledEvent : train.getEvents()) {
			if (scheduledEvent instanceof UpdateLocationEvent) {
				continue;
			}

			double second = scheduledEvent.getScheduleTime()
					.getDifference(train.getTripSection().getStartTime())
					.getTotalSecond();
			double lastSecond = 0;
			double lastMeter = 0;

			// second and meter in timeDistances are accumulated value
			for (TimeDistance point : timeDistances) {
				if (point.getSecond() >= second) {
					if (point.getSecond() - lastSecond != 0) {
						double factor = (second - lastSecond)
								/ (point.getSecond() - lastSecond);
						lastMeter += factor * (point.getMeter() - lastMeter);
					}
					break;
				} else {
					lastSecond = point.getSecond();
					lastMeter = point.getMeter();
				}
			}

			TimeDistance entry = new TimeDistance(lastMeter, second);
			int type = EventData.IN;
			if (scheduledEvent.getSource().equals(train)) {
				type = EventData.SELF;
			}

			String text = scheduledEvent instanceof AbstractEventToTrain ? ((AbstractEventToTrain) scheduledEvent)
					.getEventString() : scheduledEvent.toString();

			List<EventData> events = eventsMap.get(entry);
			if (events == null) {
				events = new ArrayList<EventData>();
				eventsMap.put(entry, events);
			}

			EventData event = new EventData(entry, type, text);
			events.add(event);
		}

		return eventsMap;
	}

	private DraggableChart<Number, Number> timeDistanceChart;
	private DraggableChart<Number, Number> speedProfileChart;

	private StackPane snapshotPane;
	private SnapshotPaneController snapshotPaneController;

	private ObservableList<String> numbers = FXCollections
			.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap = new ConcurrentHashMap<String, AbstractTrainSimulator>();
}
