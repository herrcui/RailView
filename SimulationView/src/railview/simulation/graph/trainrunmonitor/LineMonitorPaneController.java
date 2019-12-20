package railview.simulation.graph.trainrunmonitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Duration;
import railapp.units.Time;
import railview.simulation.ui.components.ChartLineBlockingTime;
import railview.simulation.ui.data.BlockingTime;
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
	
	public void updateUI(Line line,
			Collection<Station> stations, 
			HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeMap,
			HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistanceMap) {
		
		this.setMaxMinXY(stations, blockingTimeMap);
		
		
		lineChart.setChartBound(minX, maxX, minY, maxY);
		
		((NumberAxis) lineChart.getYAxis()).setTickLabelFormatter(new StringConverter<Number>() {
			@Override
			public String toString(Number t) {
				Time testTime = startTime.add(Duration.fromTotalSecond(-t.doubleValue()));
				return testTime.toString();
			}

			@Override
			public Number fromString(String string) {
				return 1;
			}
		});

		lineChart.setStations(stations);
		lineChart.setBlockingTimeStairwaysMap(blockingTimeMap);
		lineChart.setTimeDistancesMap(timeDistanceMap);
	}
	
	// max and min coordinate for the Mapper
	private void setMaxMinXY(Collection<Station> stations, 
		HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways) {
		
		for (Station station : stations) {
			if (station.getCoordinate().getX() > maxX)
				maxX = station.getCoordinate().getX();
			if (station.getCoordinate().getX() < minX)
				minX = station.getCoordinate().getX();
		}

		for (Entry<AbstractTrainSimulator, List<BlockingTime>> entry : blockingTimeStairways.entrySet()) {
			Time sTime = entry.getKey().getTripSection().getStartTime();
			if (sTime.compareTo(this.startTime) < 0) {
				this.startTime = sTime;
			}
			
			for (BlockingTime blockingTime : entry.getValue()) {
				double time = blockingTime.getEndTimeInSecond() + 
						entry.getKey().getActiveTime().getDifference(Time.getInstance(0, 0, 0)).getTotalSeconds();
 
				if (time > maxY)
					maxY = time;
				if (time < minY)
					minY = time;
			}
		}
	}
}
