package railview.simulation.ui.components;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import railapp.infrastructure.dto.Station;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Time;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.TimeDistance;

public class BlockingTimeForLineChart<X, Y> extends DraggableChart<X, Y> {

	private HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairwaysMap;
	private HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistancesMap;
	private List<Station> stationList;
	private Rectangle rectangle;
	private double maxY;

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

		if (this.timeDistancesMap != null && this.timeDistancesMap.size() > 0) {
			this.drawTimeDistance();
		}

		if (this.blockingTimeStairwaysMap != null && this.blockingTimeStairwaysMap.size() > 0) {
			this.drawBlockingTimeStairway();
		}
	}
	
	private void drawTimeDistance() {
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
				// Process previous and current here.
				javafx.scene.shape.Line polyLine = new javafx.scene.shape.Line();
				polyLine.setStartX(this.getXAxis()
						.getDisplayPosition(
								this.getXAxis().toRealValue(
										previous.getDistance())));
				polyLine.setEndX(this.getXAxis().getDisplayPosition(
						this.getXAxis().toRealValue(current.getDistance())));
				polyLine.setStartY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(maxY - (activeTime + previous.getSecond()))));
				polyLine.setEndY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(maxY - (activeTime + current.getSecond()))));
				this.getPlotChildren().add(polyLine);
				previous = current;
			}
		}
	}
	
	private void drawBlockingTimeStairway() {
		for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : this.blockingTimeStairwaysMap.entrySet()) {
			double activeTime = entry.getKey().getActiveTime()
					.getDifference(Time.getInstance(0, 0, 0))
					.getTotalSeconds();
			for (BlockingTime blockingTime : entry.getValue()) {
				double startX = blockingTime.getStartDistance();
				double endX = blockingTime.getEndDistance();
				double startY = blockingTime.getStartTimeInSecond();
				double endY = blockingTime.getEndTimeInSecond();

				rectangle = new Rectangle();

				rectangle.setY(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(
								(maxY - (activeTime + startY)))));

				rectangle.setHeight(this.getYAxis().getDisplayPosition(
						this.getYAxis().toRealValue(
								(this.maxY - (activeTime + endY))))
						- this.getYAxis().getDisplayPosition(
								this.getYAxis().toRealValue(
										(maxY - (activeTime + startY)))));

				if (endX > startX) {
					rectangle.setX(this.getXAxis().getDisplayPosition(
							this.getXAxis().toRealValue(startX)));
					rectangle.setWidth(this.getXAxis().getDisplayPosition(
							this.getXAxis().toRealValue(endX))
							- this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(startX)));
				} else {
					rectangle.setX(this.getXAxis().getDisplayPosition(
							this.getXAxis().toRealValue(endX)));
					rectangle.setWidth(this.getXAxis().getDisplayPosition(
							this.getXAxis().toRealValue(startX))
							- this.getXAxis().getDisplayPosition(
									this.getXAxis().toRealValue(endX)));
				}

				rectangle.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.5));
				this.getPlotChildren().add(rectangle);
			}
		}
	}
}
