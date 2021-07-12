package railview.simulation.graph.trainrunmonitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railapp.units.Time;
import railview.simulation.ui.components.ChartLineBlockingTime;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.LineData;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.Zoom;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.StringConverter;

public class LineMonitorPaneController {
	@FXML
	private AnchorPane lineMonitorPane;

	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double maxY = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private Time startTime = Time.getInstance(1, 23, 59, 59, 0);

	private ChartLineBlockingTime<Number, Number> lineChart;

	private HashMap<String, LineData> lineDataMap = new HashMap<String, LineData>();

	@FXML
	public void initialize() {
		lineMonitorPane.setPickOnBounds(false);

		lineChart = ChartLineBlockingTime.createBlockingTimeChartForLine();
		lineMonitorPane.getChildren().add(lineChart);
		new Zoom(lineChart, lineMonitorPane);
	}

	@FXML
	private void resetZoomBlockingTimeStairways(MouseEvent event) {
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				lineChart.setChartBound(minX, maxX, minY, maxY);
			}
		}
	}

	public LineData getLineData(String lineString) {
		return this.lineDataMap.get(lineString);
	}

	public void updateUI(String lineString, LineData inputLineData,
			HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeMap,
			HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistanceMap) {

		if (this.lineDataMap.get(lineString) == null) {
			this.lineDataMap.put(lineString, inputLineData);
		}

		LineData lineData = this.getLineData(lineString);
		this.setMaxMinXY(lineData, blockingTimeMap);

		lineChart.setChartBound(minX, maxX, minY, maxY);

		((NumberAxis) lineChart.getYAxis()).setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number t) {
				Time yTime = startTime.add(
						Duration.fromTotalSecond(-t.doubleValue())).add(Duration.fromTotalSecond(maxY-minY));
				return yTime.toStringInHMS();
			}

			@Override
			public Number fromString(String string) {
				return 1;
			}
		});

		lineChart.setLineData(lineData);
		lineChart.setBlockingTimeStairwaysMap(blockingTimeMap);
		lineChart.setTimeDistancesMap(timeDistanceMap);
	}

	// max and min coordinate for the Mapper
	private void setMaxMinXY(LineData lineData,
			HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways) {

		this.maxX = lineData.getTotalDistance().getMeter();
		this.minX = 0;

		for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : blockingTimeStairways.entrySet()) {
			Time sTime = entry.getKey().getTripSection().getStartTime();
			if (sTime.compareTo(this.startTime) < 0) {
				this.startTime = sTime;
			}

			for (BlockingTime blockingTime : entry.getValue()) {
				double endTimeSeconds = blockingTime.getRelativeEndTimeInSecond() +
						sTime.getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
				double startTimeSeconds = blockingTime.getRelativeStartTimeInSecond() +
						sTime.getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();

				if (endTimeSeconds > maxY)
					maxY = endTimeSeconds;
				if (startTimeSeconds < minY)
					minY = startTimeSeconds;
			}
		}
	}
}
