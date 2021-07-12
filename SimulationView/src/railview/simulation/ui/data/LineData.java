package railview.simulation.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Length;

public class LineData {
	public LineData(Collection<Station> stations, ArrayList<AbstractTrainSimulator> trains) {
		for (AbstractTrainSimulator train : trains) {
			boolean isValid = true;
			for (TripElement element : train.getTripSection().getTripElements()) {
				Station station = element.getStation();
				if (!stations.contains(station)) {
					isValid = false;
					break;
				}
			}

			if (isValid) {
				for (int i = 0; i < train.getTripSection().getTripElements().size(); i++) {
				    TripElement element = train.getTripSection().getTripElements().get(i);
					Length distance = train.getFullPath().findFirstDistance(
							((InfrastructureObject) element.getOperationalPoint()));

					this.stationList.add(element.getStation());
					this.distanceList.add(distance);
					if (i > 0) {
						this.pairStartIndex.put(new StationPair(this.stationList.get(i-1), this.stationList.get(i)),
								i - 1);
					}
				}
				break;
			}
		}
	}

	public Length getTotalDistance() {
		// get last distance of the series of stations
		return distanceList.get(distanceList.size() - 1);
	}

	public Length findStartStationDistance(Station startStation, Station endStation) {
		Integer startIndex = this.pairStartIndex.get(new StationPair(startStation, endStation));
		if (startIndex != null) {
			return this.distanceList.get(startIndex);
		}

		startIndex = this.pairStartIndex.get(new StationPair(endStation, startStation));
		if (startIndex != null) {
			return this.distanceList.get(startIndex + 1);
		}

		return null;
	}

	public Length findEndStationDistance(Station startStation, Station endStation) {
		Integer startIndex = this.pairStartIndex.get(new StationPair(startStation, endStation));
		if (startIndex != null) {
			return this.distanceList.get(startIndex + 1);
		}

		startIndex = this.pairStartIndex.get(new StationPair(endStation, startStation));
		if (startIndex != null) {
			return this.distanceList.get(startIndex);
		}

		return null;
	}

	public List<Station> getStations() {
		return this.stationList;
	}

	public List<Length> getDistances() {
		return this.distanceList;
	}

	// private LinkedHashMap<Station, List<Length>> stationDistances = new LinkedHashMap<Station, List<Length>>();

	private List<Station> stationList = new ArrayList<Station>();
	private List<Length> distanceList = new ArrayList<Length>();
	private HashMap<StationPair, Integer> pairStartIndex = new HashMap<StationPair, Integer>();

	class StationPair {
		private Station station1;
		private Station station2;

		StationPair(Station station1, Station station2) {
			this.station1 = station1;
			this.station2 = station2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((station1 == null) ? 0 : station1.hashCode());
			result = prime * result + ((station2 == null) ? 0 : station2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StationPair other = (StationPair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (station1 == null) {
				if (other.station1 != null)
					return false;
			} else if (!station1.equals(other.station1))
				return false;
			if (station2 == null) {
				if (other.station2 != null)
					return false;
			} else if (!station2.equals(other.station2))
				return false;
			return true;
		}

		private LineData getOuterType() {
			return LineData.this;
		}
	}
}
