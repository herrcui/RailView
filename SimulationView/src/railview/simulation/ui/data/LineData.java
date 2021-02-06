package railview.simulation.ui.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Length;

public class LineData {
	public LineData(AbstractTrainSimulator refTrain) {
		for (TripElement element : refTrain.getTripSection().getTripElements()) {
			Length distance = refTrain.getFullPath().findFirstDistance(
					((InfrastructureObject) element.getOperationalPoint()));
			this.stationDistances.put(element.getStation(), distance);
		}
	}

	public Length getTotalDistance() {
		// get last distance of the series of stations
		return this.stationDistances.get(this.getStations().get(stationDistances.size() - 1));
	}

	public Length findStationDistance(Station station) {
		return this.stationDistances.get(station);
	}

	public List<Station> getStations() {
		return new ArrayList<Station>(stationDistances.keySet());
	}

	private LinkedHashMap<Station, Length> stationDistances = new LinkedHashMap<Station, Length>();
}
