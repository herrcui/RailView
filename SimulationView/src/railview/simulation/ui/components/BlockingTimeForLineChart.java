package railview.simulation.ui.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Time;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.DraggableChart;

public class BlockingTimeForLineChart<X, Y> extends DraggableChart<X, Y> {

	private HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairwaysMap;
	private HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistancesMap;
	private List<Station> stationList;
	private Rectangle rectangle;
	
	private static double minX, maxX;
	private static double minY, maxY;

	public static BlockingTimeForLineChart<Number, Number> createBlockingTimeChartForLine() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();

		minX = minY = 0;
		maxX = maxY = 100;
		
		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(minX);
		xAxis.setUpperBound(maxX);
		yAxis.setAutoRanging(true);

		BlockingTimeForLineChart<Number, Number> chart = new BlockingTimeForLineChart<Number, Number>(
				xAxis, yAxis);

		xAxis.setSide(Side.TOP);
		chart.setMaxY(maxY);

		return chart;
	}
	
	public BlockingTimeForLineChart(Axis<X> xAxis, Axis<Y> yAxis) {
		super(xAxis, yAxis);
	}

	public ObservableList<Node> getBlockingTimeStairwayChartPlotChildren() {
		return this.getPlotChildren();
	}

	public void setBlockingTimeStairwaysMap(
			HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairwaysMap) {
		this.blockingTimeStairwaysMap = blockingTimeStairwaysMap;
	}

	public void setTimeDistancesMap(
			HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistancesMap) {
		this.timeDistancesMap = timeDistancesMap;
	}

	public void setStationList(List<Station> stationList) {
		this.stationList = stationList;
	}

	
	public void setMaxY(double maxY) {
		this.maxY = maxY;
	}
	
	@SuppressWarnings("unchecked")
	public void setX(double minX, double maxX) {
		this.getXAxis().setAutoRanging(false);
		((ValueAxis<Number>) this.getXAxis()).setLowerBound(minX);
		((ValueAxis<Number>) this.getXAxis()).setUpperBound(maxX);
	}

	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();

		for (Station station : this.stationList) {
			Circle circle = new Circle();
			circle.setCenterX(this.getXAxis()
					.getDisplayPosition(
							this.getXAxis().toRealValue(
									station.getCoordinate().getX())));
			circle.setCenterY(0);
			circle.setRadius(5);
			this.getPlotChildren().add(circle);
/**
			Label stationLabel = new Label();
			stationLabel.setText(station.getName());
			stationLabel.setVisible(true);
			stationLabel
					.setTranslateX(mapper.mapToPaneX(station.getCoordinate()
							.getX() - stationLabel.getWidth(), linePane));
			stationLabel.setTranslateY(linePane.getHeight() / 2
					- linePane.getHeight() / 15);
**/
		}

		if (this.blockingTimeStairwaysMap != null && this.blockingTimeStairwaysMap.size() > 0) {
			this.drawBlockingTimeStairway();
		}
		
		if (this.timeDistancesMap != null && this.timeDistancesMap.size() > 0) {
			this.drawTimeDistanceOld();
		}	
	}	
	
	public void drawTimeDistances(BlockingTimeForLineChart<Number, Number> lineChart) {

		lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
		lineChart.setLegendVisible(false);
		lineChart.setCreateSymbols(false);

		if (this.timeDistancesMap.size() > 0) {
			for (Entry<AbstractTrainSimulator, List<TimeDistance>> entry : this.timeDistancesMap.entrySet()) {
				XYChart.Series<Number, Number> timeDistancesSeries = new Series<Number, Number>();

				double activeTime = entry.getKey().getActiveTime()
						.getDifference(Time.getInstance(0, 0, 0))
						.getTotalSeconds();
				for (TimeDistance timeDistance : entry.getValue()) {
					if (timeDistance.getDistance() == -1) {
						if (timeDistancesSeries.getData().size() > 0) {
							// store series and prepare new series
							lineChart.getData().add(timeDistancesSeries);
							timeDistancesSeries.nodeProperty().get().setStyle("-fx-stroke: blue");
							timeDistancesSeries = new Series<Number, Number>();
						}
					} else {
						timeDistancesSeries.getData().add(new Data<Number, Number>(
							timeDistance.getDistance(),
							(maxY - (activeTime + timeDistance.getSecond()))));
						
					}
				}

				if (timeDistancesSeries.getData().size() > 0) {
					lineChart.getData().add(timeDistancesSeries);
					timeDistancesSeries.nodeProperty().get().setStyle("-fx-stroke: blue");
				}
			}

		}
	}
	
	private void drawTimeDistanceOld() {
		for (Entry<AbstractTrainSimulator, List<TimeDistance>> entry : this.timeDistancesMap
				.entrySet()) {
			// Iterator to get the current and next distance and time value
			double activeTime = entry.getKey().getActiveTime()
					.getDifference(Time.getInstance(0, 0, 0))
					.getTotalSeconds();
			Iterator<TimeDistance> it = entry.getValue().iterator();
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
				javafx.scene.shape.Line polyLine = new javafx.scene.shape.Line();
				polyLine.setStartX(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(previous.getDistance())));
				polyLine.setEndX(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(current.getDistance())));
				polyLine.setStartY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(maxY + 100 - (activeTime + previous.getSecond()))));
				polyLine.setEndY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(maxY + 100 - (activeTime + current.getSecond()))));
				this.getPlotChildren().add(polyLine);
				
				previous = current;
			}
		}
	}
	
	public void drawBlockingTimeStairway() {
		for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : this.blockingTimeStairwaysMap.entrySet()) {
			double activeTime = entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
			
			for (BlockingTime blockingTime : entry.getValue()) {
				double startX = blockingTime.getStartDistance();
				double endX = blockingTime.getEndDistance();
				double startY = blockingTime.getStartTimeInSecond();
				double endY = blockingTime.getEndTimeInSecond();

				rectangle = new Rectangle();

				rectangle.setY(this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(
						(maxY - (activeTime + startY)))));

				rectangle.setHeight(this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(
						(maxY - (activeTime + endY)))) - 
					this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(
						(maxY - (activeTime + startY)))));

				if (endX > startX) {
					rectangle.setX(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(startX)));
					rectangle.setWidth(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(endX)) -
						this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(startX)));
				} else {
					rectangle.setX(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(endX)));
					rectangle.setWidth(this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(startX)) -
						this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(endX)));
				}

				rectangle.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
				this.getPlotChildren().add(rectangle);
			}
		}
	}
	
	public BlockingTimeForLineChart<Number, Number> drawStations(
			List<Station> stationList,
			BlockingTimeForLineChart<Number, Number> chart) {
		XYChart.Series<Number, Number> stationSeries = new Series<Number, Number>();

		for (Station station : stationList) {
			stationSeries.getData().add(new Data<Number, Number>(station.getCoordinate().getX(), 0));

		}
		chart.getData().add(stationSeries);
	    return chart;
	}
}
