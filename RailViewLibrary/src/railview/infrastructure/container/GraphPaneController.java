package railview.infrastructure.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import railapp.simulation.events.toinfrastructure.RequestResourceEvent;
import railapp.simulation.events.toinfrastructure.RequestResult;
import railapp.swarmintelligence.Swarm;
import railapp.swarmintelligence.SwarmLogger;
import railapp.swarmintelligence.SwarmManager;
import railapp.units.Duration;
import railapp.units.Time;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {
	
	@FXML
	private AnchorPane firstLayer;
	
	@FXML
	private AnchorPane secondLayer;
	
	@FXML
	public void initialize() {
		secondLayer.setVisible(false);
		getScatterChart();
	}
	
	public void setSwarmManager(SwarmManager si) {
		this.si = si;
	}
	
	public void getScatterChart() {
		final NumberAxis xAxis = new NumberAxis(0, 10, 1);
	    final NumberAxis yAxis = new NumberAxis(-100, 500, 100);
	    final ScatterChart<Number,Number> sc = new
	            ScatterChart<Number,Number>(xAxis,yAxis);
	    XYChart.Series series1 = new XYChart.Series();
	    
/**	    Map<Integer, List<Duration>> map = new TreeMap<Integer, List<Duration>>();
		SwarmLogger logger = si.getLogger();
		for (Swarm swarm : logger.getSwarmSet()) {
			if (swarm.getTerminationTime() == null) {
				continue;
			}
			
			Integer size =  swarm.getTrains().size();
			List<Duration> lifeCycles = map.get(size);
			if (lifeCycles == null) {
				lifeCycles = new ArrayList<Duration>();
				map.put(size, lifeCycles);
			}
			
			lifeCycles.add(swarm.getTerminationTime().getDifference(swarm.getCreationTime()));
			
		}
**/		
	for (Entry<Integer, List<Duration>> entry : getSizeAndLifecycle().entrySet()) {
			 series1.getData().add(new XYChart.Data(entry.getKey(), entry.getValue()));
		}
	    
		
//	    for(Entry<Integer,List<Duration>> entry : map.entrySet()) {
//	    	  Integer key = entry.getKey();
//	    	  List<Duration> value = entry.getValue();
	    	  
//	    	  System.out.println(key + " => " + value);
	//    	  series1.getData().add(new XYChart.Data(8.5, 323));
	    	  series1.getData().add(new XYChart.Data(10, 100));
//	    	}
	    
        sc.getData().addAll(series1);
        this.firstLayer.getChildren().add(sc);
	}
	
	// duration.getTotalMilliSecond()/1000
	private Map<Integer, List<Duration>> getSizeAndLifecycle() {
		Map<Integer, List<Duration>> map = new TreeMap<Integer, List<Duration>>();
		SwarmLogger logger = si.getLogger();
		for (Swarm swarm : logger.getSwarmSet()) {
			if (swarm.getTerminationTime() == null) {
				continue;
			}
			
			Integer size =  swarm.getTrains().size();
			List<Duration> lifeCycles = map.get(size);
			if (lifeCycles == null) {
				lifeCycles = new ArrayList<Duration>();
				map.put(size, lifeCycles);
			}
			
			lifeCycles.add(swarm.getTerminationTime().getDifference(swarm.getCreationTime()));
			
			
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
