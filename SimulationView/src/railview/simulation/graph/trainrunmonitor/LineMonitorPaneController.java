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
import railview.simulation.ui.data.CoordinateMapper;
import railview.simulation.ui.data.TimeDistance;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class LineMonitorPaneController {
	@FXML
	private AnchorPane linePane, lineBlockingTimesAnchorPane;
	
	private double minX = Double.MAX_VALUE;
	private double maxX = Double.MIN_VALUE;
	private double maxY = Double.MIN_VALUE;
	private double minY = Double.MAX_VALUE;
	private BlockingTimeForLineChart<Number, Number> lineChart;
	
	@FXML
	public void initialize() {
		lineBlockingTimesAnchorPane.setPickOnBounds(false);
	}
	
	@FXML
	private void resetZoomBlockingTimeStairways(MouseEvent event) {
		// TODO after add linechart
		if (event.getButton().equals(MouseButton.SECONDARY)) {
			if (event.getClickCount() == 2) {
				lineChart.getXAxis().setAutoRanging(true);
				lineChart.getYAxis().setAutoRanging(true);
			}
		}
	}
	
	public void updateUI(Line line,
			Collection<Station> stations, 
			HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeMap,
			HashMap<AbstractTrainSimulator, List<TimeDistance>> timeDistanceMap) {
		
		this.setMaxMinXY(stations, blockingTimeMap);
		
		linePane.getChildren().clear();
		CoordinateMapper mapper = new CoordinateMapper(maxX, minX, maxY, minY);
		javafx.scene.shape.Line stationLine = new javafx.scene.shape.Line();
		stationLine.setStartX(mapper.mapToPaneX(minX, linePane));
		stationLine.setEndX(mapper.mapToPaneX(maxX, linePane));
		stationLine.setStartY(linePane.getHeight() / 2);
		stationLine.setEndY(linePane.getHeight() / 2);
		linePane.getChildren().add(stationLine);
		
		lineBlockingTimesAnchorPane.getChildren().clear();
		lineChart = BlockingTimeForLineChart.createBlockingTimeChartForLine();
		lineChart.setX(minX, maxX);

		// TODO can the collection directly be used?	
		lineChart.setStations(stations);
		lineChart.drawStations(stations, lineChart);

		// to draw the rectangles directly into the chart
		lineChart.setBlockingTimeStairwaysMap(blockingTimeMap);

		// to draw the timeDistances directly into the chart
		lineChart.setTimeDistancesMap(timeDistanceMap);
		
		AnchorPane.setTopAnchor(lineChart, 0.0);
		AnchorPane.setLeftAnchor(lineChart, 0.0);
		AnchorPane.setRightAnchor(lineChart, 0.0);
		AnchorPane.setBottomAnchor(lineChart, 0.0);
		
		lineChart.drawTimeDistances(lineChart);
		//lineChart.drawBlockingTimeStairway();
		
		//new Zoom(lineChart, lineBlockingTimesAnchorPane);
		
		lineChart.setMouseFilter(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if (mouseEvent.getButton() != MouseButton.PRIMARY) {
					mouseEvent.consume();
				}
			}
		});
		lineChart.startEventHandlers();
		
		lineBlockingTimesAnchorPane.getChildren().add(lineChart);
	}
	
	private void setMaxMinXY(Collection<Station> stations, 
		HashMap<AbstractTrainSimulator, List<BlockingTime>> blockingTimeStairways) {
		// max and min x-coordinate for the Mapper
		for (Station station : stations) {
			if (station.getCoordinate().getX() > maxX)
				maxX = station.getCoordinate().getX();
			if (station.getCoordinate().getX() < minX)
				minX = station.getCoordinate().getX();
		}

		// max and min y-coordinate
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
