package railview.simulation.ui.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import railapp.infrastructure.dto.Station;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Length;
import railapp.units.Time;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.LineData;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.DraggableChart;

public class ChartLineBlockingTime<X, Y> extends DraggableChart<X, Y> {

	private HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairwaysMap;
	private HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistancesMap;
	private LineData lineData;
	private Rectangle rectangle;

	private double maxY;

	public static ChartLineBlockingTime<Number, Number> createBlockingTimeChartForLine() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		xAxis.setSide(Side.TOP);

		ChartLineBlockingTime<Number, Number> chart =
			new ChartLineBlockingTime<Number, Number>(xAxis, yAxis);

		chart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() != MouseButton.PRIMARY) {
					mouseEvent.consume();
				}
			}
		});
		chart.startEventHandlers();

		return chart;
	}

	private ChartLineBlockingTime(Axis<X> xAxis, Axis<Y> yAxis) {
		super(xAxis, yAxis);

		AnchorPane.setTopAnchor(this, 0.0);
		AnchorPane.setLeftAnchor(this, 0.0);
		AnchorPane.setRightAnchor(this, 0.0);
		AnchorPane.setBottomAnchor(this, 0.0);
	}

	public void setChartBound(double minX, double maxX, double minY, double maxY) {
		NumberAxis xAxis = (NumberAxis) this.getXAxis();
		NumberAxis yAxis = (NumberAxis) this.getYAxis();

		this.maxY = maxY;

		xAxis.setAutoRanging(false);
		xAxis.setLowerBound(minX);
		xAxis.setUpperBound(maxX);
		yAxis.setAutoRanging(false);
		yAxis.setLowerBound(0);
		yAxis.setUpperBound(maxY-minY);

		xAxis.setTickUnit(100);
		yAxis.setTickUnit(600);
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

	public void setLineData(LineData lineData) {
		this.lineData = lineData;
	}

	@Override
	protected void layoutPlotChildren() {
		super.layoutPlotChildren();
		this.refresh();
	}

	public void refresh() {
		this.getPlotChildren().clear();

		if (this.blockingTimeStairwaysMap != null && this.blockingTimeStairwaysMap.size() > 0) {
			this.drawBlockingTimeStairway();
		}

		if (this.timeDistancesMap != null && this.timeDistancesMap.size() > 0) {
			this.drawTimeDistance();
		}

		if (this.lineData != null) {
			this.drawStations();
		}
	}

	private void drawTimeDistance() {
		//lineChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
		//lineChart.setLegendVisible(false);
		//lineChart.setCreateSymbols(false);

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
				Line polyLine = new Line();

				polyLine.setStartX(mapToChart(previous.getDistance(), true));
				polyLine.setEndX(mapToChart(current.getDistance(), true));
				polyLine.setStartY(mapToChart(activeTime + previous.getSecond(), false));
				polyLine.setEndY(mapToChart(activeTime + current.getSecond(), false));

				this.getPlotChildren().add(polyLine);

				previous = current;
			}
		}
	}

	public void drawBlockingTimeStairway() {
		for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : this.blockingTimeStairwaysMap.entrySet()) {
			double activeTime = entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();

			for (BlockingTime blockingTime : entry.getValue()) {
				double startX = blockingTime.getRelativeStartDistance();
				double endX = blockingTime.getRelativeEndDistance();
				double startY = blockingTime.getRelativeStartTimeInSecond();
				double endY = blockingTime.getRelativeEndTimeInSecond();

				rectangle = new Rectangle();

				rectangle.setY(mapToChart(activeTime + startY, false));
				rectangle.setHeight(mapToChart(endY, false) - mapToChart(startY, false));


				if (endX > startX) {
					rectangle.setX(mapToChart(startX, true));
					rectangle.setWidth(mapToChart(endX, true) - mapToChart(startX, true));
				} else {
					rectangle.setX(mapToChart(endX, true));
					rectangle.setWidth(mapToChart(startX, true) - mapToChart(endX, true));
				}

				rectangle.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
				this.getPlotChildren().add(rectangle);
			}
		}
	}

	private void drawStations() {
		List<Station> stationList = this.lineData.getStations();
		List<Length> distanceList = this.lineData.getDistances();

		for (int i = 0; i < stationList.size(); i++) {
			Station station = stationList.get(i);
			Length distance = distanceList.get(i);

			double chartX = mapToChart(distance.getMeter(), true);
			Circle circle = new Circle();
			circle.setCenterX(chartX);
			circle.setCenterY(0);
			circle.setRadius(5);
			circle.setFill(Color.BLUE.deriveColor(0, 1, 1, 1.0));
			this.getPlotChildren().add(circle);

			Text dataText = new Text(station.getName());
			dataText.setLayoutX(chartX);
			dataText.setLayoutY(30);
			dataText.setRotate(90);
			this.getPlotChildren().add(dataText);
		}
	}

	private double mapToChart(double origin, boolean isXAxis) {
		if (isXAxis) {
			return this.getXAxis().getDisplayPosition(this.getXAxis().toRealValue(origin));
		}
		else {
			return this.getYAxis().getDisplayPosition(this.getYAxis().toRealValue(maxY - origin));
		}
	}
}
