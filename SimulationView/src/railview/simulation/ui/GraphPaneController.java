package railview.simulation.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.infrastructure.path.dto.LinkPath;
import railapp.rollingstock.dto.SimpleTrain;
import railapp.simulation.infrastructure.PartialRouteResource;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
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
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


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

        timeDistanceChart = createCourseForTimeChart();
        speedProfileChart = createVelocityChart();

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
	    DraggableChart<Number, Number> chart = new DraggableChart<>(xAxis, yAxis);
	    chart.setAnimated(false);
	    chart.setCreateSymbols(false);
	    trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
    			System.out.println(getTrain(newValue));
       		    drawVelocityTable(getTrain(newValue), chart);

            }
        });
	    return chart ;
	}

	private BlockingTimeChart<Number, Number> createCourseForTimeChart() {
		NumberAxis xAxis = createXAxis2();
	    NumberAxis yAxis = createYAxis2();
	    BlockingTimeChart<Number,Number> chart = new BlockingTimeChart<Number,Number>(xAxis,yAxis);
	    chart.getBlockingTimeChartPlotChildren();

	    trainNumbers.setOnMouseClicked(new EventHandler<MouseEvent>() {
	        @Override
	        public void handle(MouseEvent event) {
	            System.out.println("clicked on" + getTrain(trainNumbers.getSelectionModel().getSelectedItem().toString()));
	        }
	    });

	    trainNumbers.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
            	chart.removeRectangle();
                chart.getData().clear();
    			drawCourseforTimeTable(getTrain(newValue), chart);
    			chart.setBlockingTime(getBlockingTimeStairway(getTrain(newValue)));
    //			chart.addRectangle(1000,1000,1000,1000);
    		    chart.setAnimated(false);
    		    chart.setCreateSymbols(false);
    			yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
    			        @Override
    			        public String toString(Number value) {
    			        	while (value.doubleValue() != 0) {
    			            return String.format("%7.0f", -value.doubleValue());
    			        }
    			        	return String.format("%7.0f", value.doubleValue());
    			        }
    			    });

    			chart.setOnMouseDragged(new EventHandler<MouseEvent>() {
    		          @Override
    		          public void handle(MouseEvent event) {
    		        	 chart.getBlockingTimeChartPlotChildren().clear();
    		        	 chart.getData().clear();

    		    		 drawCourseforTimeTable(getTrain(newValue), chart);

    		    		 /*
    		    		 for (BlockingTime blockingTime : chart.blockingTimes) {
    		        	 chart.addRectangle(blockingTime.getStartMeter(),
    		        			 blockingTime.getStartTimeInSecond(),
    		        			 blockingTime.getEndMeter()-blockingTime.getStartMeter(),
    		        			 blockingTime.getEndTimeInSecond()-blockingTime.getStartTimeInSecond());
    		          }
    		          */
    		          }
    		          });


            }
        });





	    xAxis.setSide(Side.TOP);
	    return chart ;
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
		CopyOnWriteArrayList<AbstractTrainSimulator> tempList = new
			CopyOnWriteArrayList<AbstractTrainSimulator>();
		tempList.addAll(trainList);
		for (AbstractTrainSimulator trainSimulator : tempList) {
			this.trainMap.put(trainSimulator.getTrain().getNumber(), trainSimulator);
			String trainNumber = trainSimulator.getTrain().getNumber();
			if (trainSimulator.getTrain().getStatus() != SimpleTrain.INACTIVE &&
					!numbers.contains(trainNumber)) {
				numbers.add(trainNumber);
			}
		}
		trainNumbers.setItems(numbers);

	}


	public AbstractTrainSimulator getTrain(String trainNumber) {
		return this.trainMap.get(trainNumber);
	}


	public LineChart<Number, Number> drawVelocityTable(AbstractTrainSimulator train, LineChart<Number, Number> chart) {
		chart.getData().clear();
		XYChart.Series<Number, Number> CourseForVelocitySeries = new Series<Number, Number>();
		CourseForVelocitySeries.setName("course for velocity");
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
		for(Map.Entry<Double,Double> entry : getCourseForVelocity(train).entrySet()) {
			CourseForVelocitySeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
		}
		chart.getData().add(CourseForVelocitySeries);
		XYChart.Series<Number, Number> speedLimitSeries = new Series<Number, Number>();

		speedLimitSeries.setName("speedlimit");
		double y = -1;
		speedLimitSeries.getData().add(new Data<Number, Number>(0, y));
	    for(Map.Entry<Double,Double> entry: getSpeedLimit(train).entrySet()) {
	        if (y >= 0) {
	            speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), y));
	        }
	        speedLimitSeries.getData().add(new Data<Number, Number>(entry.getKey(), entry.getValue()));
	        y = entry.getValue();
	    }
	    chart.getData().add(speedLimitSeries);
	    speedLimitSeries.nodeProperty().get().setStyle("-fx-stroke: #000000; -fx-stroke-dash-array: 0.1 5.0;");

		chart.setCreateSymbols(false);
		}
		return chart;
	}

	public BlockingTimeChart<Number, Number> drawCourseforTimeTable(AbstractTrainSimulator train, BlockingTimeChart<Number, Number> chart) {
		chart.getData().clear();
		XYChart.Series<Number, Number> courseForTimeSeries = new Series<Number, Number>();
		courseForTimeSeries.setName("course for time");
		double y = -1;
		courseForTimeSeries.getData().add(new Data<Number, Number>(0, y));
		if (train.getTrain().getStatus() != SimpleTrain.INACTIVE) {
			for(TimeDistance point : getTimeInDistance(train)) {
				courseForTimeSeries.getData().add(new Data<Number, Number>(point.getMeter(), (point.getSecond() * -1)));
		}
			chart.getData().add(courseForTimeSeries);
			chart.setCreateSymbols(false);
		}

		List<BlockingTime> blockingTimes = this.getBlockingTimeStairway(train);
		for (BlockingTime blockingTime : blockingTimes) {
			blockingTime.getEndMeter();
		}

		return chart;
	}

	// Map: Meter, VelocityInKmH
	private Map<Double, Double> getCourseForVelocity(AbstractTrainSimulator train) {
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

	private LinkedHashMap<Double, Double> getSpeedLimit(AbstractTrainSimulator train) {
		// Velocity
		LinkedHashMap<Double, Double> speedLimitMap = new LinkedHashMap<Double, Double>();

		LinkPath path = train.getFullPath();

		double maxTrainKmH = train.getTrainDefinition().getMaxVelocity().getKilometerPerHour();
		double maxKmH = Math.min(maxTrainKmH,
			path.getLinkEdges().get(0).getLink().getGeometry().getMaxVelocity().getKilometerPerHour());
		double lastMeter = 0;
		double meter = 0;

		for (LinkEdge edge : path.getLinkEdges()) {
			if (edge.getLength().getMeter() == 0) {
				continue;
			}

			double linkKmH = edge.getLink().getGeometry().getMaxVelocity().getKilometerPerHour();

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

	private List<BlockingTime> getBlockingTimeStairway(AbstractTrainSimulator train) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();
		if (train instanceof TrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;

			List<PartialRouteResource> resources = ((TrainSimulator) train).getBlockingTimeStairWay();
			Time trainStartTime = train.getTripSection().getStartTime();

			for (PartialRouteResource resource : resources) {
				if (headDistanceInFirstResource == null) {
					headDistanceInFirstResource = resource.getPath().findFirstDistance(
						(InfrastructureObject) train.getTripSection().getTripElements().get(0).getOperationalPoint());
					if (headDistanceInFirstResource == null) {
						continue;
					}
				}

				if (resource.getReleaseTime() == null) {
					break;
				}

				double startMeter = meter;
				double endMeter = meter + resource.getPartialRoute().getPath().getLength().getMeter();
				double startTimeInSecond = resource.getGrantTime().getDifference(trainStartTime).getTotalSecond();
				double endTimeInSecond = resource.getReleaseTime().getDifference(trainStartTime).getTotalSecond();

				if (meter == 0) { // for the first resource
					endMeter = endMeter - headDistanceInFirstResource.getMeter();
				}

				blockingTimes.add(new BlockingTime(startMeter, endMeter, startTimeInSecond, endTimeInSecond));

				meter = endMeter;
			}

		}

		return blockingTimes;
	}


	DraggableChart<Number, Number> timeDistanceChart;
	DraggableChart<Number, Number> speedProfileChart;

	ObservableList<String> numbers = FXCollections.observableArrayList();
	private ConcurrentHashMap<String, AbstractTrainSimulator> trainMap =
			new ConcurrentHashMap<String, AbstractTrainSimulator>();

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

		private double meter;
		private double second;
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

	class BlockingTimeChart<X,Y> extends DraggableChart<X,Y> {


		public BlockingTimeChart(Axis<X> xAxis, Axis<Y> yAxis) {
			super(xAxis, yAxis);


			// TODO Auto-generated constructor stub
		}

		void setBlockingTime(List<BlockingTime> blockingTimes) {
			this.blockingTimes = blockingTimes;
		}

		 public void addRectangle(double x, double y, double width, double height) {
		        r = new Rectangle();

		        double dx = ((ValueAxis<?>) this.getXAxis()).getLowerBound();
		        double dy = ((ValueAxis<?>) this.getYAxis()).getLowerBound();

		        r.setX(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(x)));
                r.setY(this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(-y)));
                r.setWidth(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(width)));
                r.setHeight(this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(-height)));
                r.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.5));
         		r.setMouseTransparent(true);
                this.getPlotChildren().add(r);

		    }

		 public void removeRectangle () {
			 this.getPlotChildren().clear();
		 }

		public ObservableList<Node> getBlockingTimeChartPlotChildren() {
			return this.getPlotChildren();
		}


		 @Override
        protected void layoutPlotChildren() {
            super.layoutPlotChildren();
            if (this.blockingTimes != null) {
            	for (BlockingTime blockingTime : this.blockingTimes) {
            		 r = new Rectangle();

                     r.setX(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(blockingTime.getStartMeter())));
                     r.setY(this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(-(blockingTime.getStartTimeInSecond()))));
                     r.setWidth(this.getXAxis().getDisplayPosition(
                    		 		this.getXAxis().toRealValue(blockingTime.getEndMeter())) -
                    		 this.getXAxis().getDisplayPosition(
                    				this.getXAxis().toRealValue(blockingTime.getStartMeter())));
                     r.setHeight(this.getYAxis().getDisplayPosition(
                    		 		this.getYAxis().toRealValue(-(blockingTime.getEndTimeInSecond())))-
                    		 	this.getYAxis().getDisplayPosition(
                    		 		this.getYAxis().toRealValue(-(blockingTime.getStartTimeInSecond()))));

                     r.setFill(Color.GREEN.deriveColor(0, 1, 1, 0.5));
             		 r.setMouseTransparent(true);
                     this.getPlotChildren().add(r);
            	}

            }
        }

		private List<BlockingTime> blockingTimes;
		Rectangle r;

	}

}
