package railview.simulation.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class GraphPaneController {
	
	@FXML
	private AnchorPane firstLayer;
	
	@FXML
	private AnchorPane secondLayer;
	
	
	@FXML
	public void initialize() {
		secondLayer.setVisible(false);
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
	
}
