package railview.simulation.graph.trainrunmonitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.units.Time;
import railview.simulation.ui.components.BlockingTimeForLineChart;
import railview.simulation.ui.data.BlockingTime;
import railview.simulation.ui.data.TimeDistance;
import railview.simulation.ui.utilities.Zoom;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class LineMonitorPaneController {
	@FXML
	private AnchorPane lineMonitorPane;
	
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double maxY = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	
	private BlockingTimeForLineChart<Number, Number> lineChart;
	
	@FXML
	public void initialize() {
		lineMonitorPane.setPickOnBounds(false);
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
		
		lineMonitorPane.getChildren().clear();
		lineChart = BlockingTimeForLineChart.createBlockingTimeChartForLine(minX, maxX, minY, maxY);

		lineChart.setStations(stations);
		lineChart.setBlockingTimeStairwaysMap(blockingTimeMap);
		lineChart.setTimeDistancesMap(timeDistanceMap);
		
		new Zoom(lineChart, lineMonitorPane);
		
		lineMonitorPane.getChildren().add(lineChart);
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
