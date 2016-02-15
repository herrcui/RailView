package railview.swarmintelligence.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import railapp.swarmintelligence.Swarm;
import railapp.swarmintelligence.SwarmLogger;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Duration;
import railapp.units.Time;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {
	
	@FXML
	private AnchorPane firstLayer;
	
	@FXML
	private AnchorPane secondLayer;
	
	private ScatterChart<Number,Number> scatterChart;
	
	private Time lastUpdateTime = null;
	
	private Duration timeInterval = Duration.fromTotalSecond(60);
	
	@FXML
	public void initialize() {
		secondLayer.setVisible(false);
	}
	
	public void setSwarmManager(SwarmManager si) {
		this.si = si;
		final NumberAxis xAxis = new NumberAxis(0, 10, 1);
	    final NumberAxis yAxis = new NumberAxis(0, 1800, 60);
	    
	    scatterChart = new ScatterChart<Number,Number>(xAxis,yAxis);
	    ObservableList<Series<Number,Number>> dataList = FXCollections.observableArrayList();
	    Series<Number,Number> series = new Series<Number,Number>();
	    dataList.add(series);
	    series.setName("Lifecycle");
	    
	    scatterChart.setData(dataList);
	    scatterChart.setLayoutX(350);
	    firstLayer.getChildren().add(scatterChart);
	}
	
	public void setScatterChart(Time updateTime) {	
		if (this.lastUpdateTime != null &&
				updateTime.getDifference(this.lastUpdateTime).getTotalMilliSecond() < this.timeInterval.getTotalMilliSecond()) {
			return;
		}
		
		ObservableList<Data<Number, Number>> seriesData = scatterChart.getData().get(0).getData();
		seriesData.clear();
		
	    for(Entry<Integer,List<Duration>> entry : getSizeAndLifecycle(updateTime).entrySet()) {
	    	int x = entry.getKey();

	    	for(Duration duration : entry.getValue()) {
	    		double y= duration.getTotalMilliSecond()/1000;
	    		seriesData.add(new Data<Number, Number>(x, y));
	    	}
	    }
	    
	    this.lastUpdateTime = updateTime;
	}

	// duration.getTotalMilliSecond()/1000
	private Map<Integer, List<Duration>> getSizeAndLifecycle(Time updateTime) {
		Map<Integer, List<Duration>> map = new TreeMap<Integer, List<Duration>>();
		SwarmLogger logger = si.getLogger();
		for (Swarm swarm : logger.getSwarmSet()) {
			
			Integer size =  swarm.getTrains().size();
			List<Duration> lifeCycles = map.get(size);
			if (lifeCycles == null) {
				lifeCycles = new ArrayList<Duration>();
				map.put(size, lifeCycles);
			}
			
			if (swarm.getTerminationTime() == null) {
				lifeCycles.add(updateTime.getDifference(swarm.getCreationTime()));
			} else {
				lifeCycles.add(swarm.getTerminationTime().getDifference(swarm.getCreationTime()));
			}
			
		}
		
		return map;		
	}
	
	// for the historgamm, go throught the sorted duration, and get the size:
	//
	// (double) entry.getKey().getTotalMilliSecond()/1000
	// entry.getValue().size()
	/*
	private TreeMap<Duration, List<RequestResourceEvent>> getHistogrammData() {
		TreeMap<Duration, List<RequestResourceEvent>> result = new TreeMap<Duration, List<RequestResourceEvent>>();
		double currentLimit = interval.getTotalMilliSecond();
		List<RequestResourceEvent> list = new ArrayList<RequestResourceEvent>();
		for (Entry<Duration, List<RequestResourceEvent>> entry : durationMap.entrySet()) {
			if (entry.getKey().getTotalMilliSecond() <= currentLimit) {
				list.addAll(entry.getValue());
			} else {
				// increase the currentLimit, until currentLimit is bigger than the current duration
				while (entry.getKey().getTotalMilliSecond() > currentLimit) {
					result.put(Duration.fromTotalMilliSecond(currentLimit), list);
					currentLimit += interval.getTotalMilliSecond();
					list = new ArrayList<RequestResourceEvent>();
				}
								
				list = entry.getValue();
			}
		}
		result.put(Duration.fromTotalMilliSecond(currentLimit), list);
		return result;
	}
	
	private List<RequestResourceEvent> getAllRequests(
			TreeMap<Duration, List<RequestResourceEvent>> histogramm, Duration duration) {
		Entry<Duration, List<RequestResourceEvent>> floorEntry = histogramm.floorEntry(duration);
		if (floorEntry != null) {
			return floorEntry.getValue();
		} else {
			return null;
		}
	}
	
	private LinkedHashMap<Time, RequestResult> getResultMap(RequestResourceEvent event) {
		return event.getResultMap();
	}
	
	
	private TreeMap<Duration, List<RequestResourceEvent>> durationMap;
	
	private Duration interval = Duration.fromSecond(120);
	*/
	
	private SwarmManager si;
}
