package railview.simulation.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.path.dto.LinkEdge;
import railapp.simulation.events.ScheduledEvent;
import railapp.simulation.events.totrain.AbstractEventToTrain;
import railapp.simulation.events.totrain.LeaveResourceEvent;
import railapp.simulation.events.totrain.UpdateLocationEvent;
import railapp.simulation.infrastructure.ResourceOccupancy;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.FDTrainSimulator;
import railapp.simulation.train.MBTrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Coordinate;
import railapp.units.Length;
import railapp.units.Time;
import railapp.units.UnitUtility;

public class TrainRunDataManager {
	private HashMap<AbstractTrainSimulator, List<Length>> opDistMap = new HashMap<AbstractTrainSimulator, List<Length>>();

	public List<BlockingTime> getBlockingTimeStairway(AbstractTrainSimulator train, LineData lineData) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();

		double maxBlockingInSecond = Double.MIN_VALUE;
		ResourceOccupancy maxBTOccupancy = null;

		if (train instanceof FDTrainSimulator || train instanceof MBTrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;
			boolean isFirstResource = true;

			List<ResourceOccupancy> resourceOccupancies = train.getBlockingTimeStairWay();

			Time trainStartTime = train.getTripSection().getStartTime();

			for (ResourceOccupancy resourceOccupancy : resourceOccupancies) {
				if (headDistanceInFirstResource == null) {
					headDistanceInFirstResource = (resourceOccupancy.getResource()).getPath().findFirstDistance(
							(InfrastructureObject) train.getTripSection()
									.getTripElements().get(0)
									.getOperationalPoint());
					if (headDistanceInFirstResource == null) {
						continue;
					}
				}

				if (resourceOccupancy.getReleaseTime() == null) {
					break;
				}

				double startMeter = meter;
				double endMeter = meter	+ resourceOccupancy.getResource().getPath().getLength().getMeter();
				double startTimeInSecond = resourceOccupancy.getGrantTime()
						.getDifference(trainStartTime).getTotalSeconds();
				double endTimeInSecond = resourceOccupancy.getReleaseTime()
						.getDifference(trainStartTime).getTotalSeconds();

				if (isFirstResource) { // for the first resource
					endMeter = endMeter	- headDistanceInFirstResource.getMeter();
					isFirstResource = false;
				}

				BlockingTime bt = null;
				if (lineData == null) {
					bt = new BlockingTime(startMeter, endMeter, startTimeInSecond, endTimeInSecond);
				} else {
					double startDist = this.getDistanceInLine(startMeter, train, lineData);
					double endDist = this.getDistanceInLine(endMeter, train, lineData);

					endDist = this.getDistanceInLine(endMeter, train, lineData);

					if (startDist != -1 && endDist != -1) {
						bt = new BlockingTime(startDist, endDist, startTimeInSecond, endTimeInSecond);
					}
				}

				if (bt != null) {
					blockingTimes.add(bt);
					if (bt.getDurationInSecond() > maxBlockingInSecond) {
						maxBlockingInSecond = bt.getDurationInSecond();
						maxBTOccupancy = resourceOccupancy;
					}
				}

				meter = endMeter;

				if (resourceOccupancy.getResource().getPath().getEdges().size() >= 2 &&
						resourceOccupancy.getResource().getPath().getEdges().get(1).toString().equals(
						"LinkEdge [startLength=0 m, endLength=212 m, link=SW2001: Switch.Link:1-2]")) {
					String s = "";
				}
			}

		}

		return blockingTimes;
	}

	// Map: Meter, TimeInSecond
	public List<TimeDistance> getTimeInDistance(AbstractTrainSimulator train, LineData lineData) {
		List<TimeDistance> pointList = new ArrayList<TimeDistance>();
		double meter = 0; // x
		double timeInSecond = 0; // y

		for (DiscretePoint point : train.getWholeCoursePoints()) {
			timeInSecond += point.getDuration().getTotalSeconds();
			meter += point.getDistance().getMeter();
			if (lineData == null) {
				pointList.add(new TimeDistance(meter, timeInSecond));
			} else {
				double distance = this.getDistanceInLine(meter, train, lineData);
				pointList.add(new TimeDistance(distance, timeInSecond));
			}
		}
		return pointList;
	}

	public HashMap<AbstractTrainSimulator, List<BlockingTime>> getBlockingTimeStairwaysInLine(
			LineData lineData, Collection<AbstractTrainSimulator> trains) {
		HashMap<AbstractTrainSimulator, List<BlockingTime>> result = new HashMap<AbstractTrainSimulator, List<BlockingTime>>();

		for (AbstractTrainSimulator train : trains) {
			result.put(train, this.getBlockingTimeStairway(train, lineData));
		}
		return result;
	}

	public HashMap<AbstractTrainSimulator, List<TimeDistance>> getTimeDistancesInLine(
			LineData lineData, Collection<AbstractTrainSimulator> trains) {
		HashMap<AbstractTrainSimulator, List<TimeDistance>> result = new HashMap<AbstractTrainSimulator, List<TimeDistance>>();
		for (AbstractTrainSimulator train : trains) {
			result.put(train, this.getTimeInDistance(train, lineData));
		}
		return result;
	}

	private List<Length> getOpDistances(AbstractTrainSimulator train) {
		List<Length> opDistances = this.opDistMap.get(train);

		if (opDistances == null) {
			Length refDist = Length.fromMeter(0);
			opDistances = new ArrayList<Length>();
			for (TripElement elem : train.getTripSection().getTripElements()) {
				Length opDistance = train.getFullPath().findFirstDistance(
						(InfrastructureObject) elem.getOperationalPoint(),
						refDist);

				opDistances.add(opDistance);
				refDist = opDistance;
			}

			this.opDistMap.put(train, opDistances);
		}

		return opDistances;
	}

	private double getDistanceInLine(double meter, AbstractTrainSimulator train, LineData lineData) {
		int index = 0;
		List<Length> opDistances = this.getOpDistances(train);

		Collection<Station> stations = lineData.getStations();

		for (Length dist : opDistances) {
			if (index >= opDistances.size() - 1) {
				break;
			}

			if (meter - dist.getMeter() >= UnitUtility.ERROR * -1 &&
				opDistances.get(index + 1).getMeter() - meter >= UnitUtility.ERROR * -1) {

				Station startStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index)
						.getOperationalPoint()).getElement().getStation();
				Station endStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index + 1)
						.getOperationalPoint()).getElement().getStation();

				if (stations.contains(startStation) && stations.contains(endStation)) {
					return lineData.findStartStationDistance(startStation, endStation).getMeter()
							+ (meter - dist.getMeter())
							* (lineData.findEndStationDistance(startStation, endStation).getMeter() -
									lineData.findStartStationDistance(startStation, endStation).getMeter())
							/ (opDistances.get(index + 1).getMeter() - opDistances.get(index).getMeter());
				} else {
					return -1;
				}
			}
			index++;
		}

		return -1;
	}

	public Map<TimeDistance, List<EventData>> getEvents(AbstractTrainSimulator train) {
		List<TimeDistance> timeDistances = this.getTimeInDistance(train, null);
		Map<TimeDistance, List<EventData>> eventsMap = new HashMap<TimeDistance, List<EventData>>();

		for (ScheduledEvent scheduledEvent : train.getEvents()) {
			if (scheduledEvent instanceof UpdateLocationEvent) {
				continue;
			}

			if (scheduledEvent instanceof LeaveResourceEvent && train instanceof MBTrainSimulator) {
				continue;
			}

			double second = scheduledEvent.getScheduleTime()
					.getDifference(train.getTripSection().getStartTime())
					.getTotalSeconds();
			double lastSecond = 0;
			double lastMeter = 0;

			// second and meter in timeDistances are accumulated value
			for (TimeDistance point : timeDistances) {
				if (point.getSecond() >= second) {
					if (point.getSecond() - lastSecond != 0) {
						double factor = (second - lastSecond)
								/ (point.getSecond() - lastSecond);
						lastMeter += factor * (point.getDistance() - lastMeter);
					}
					break;
				} else {
					lastSecond = point.getSecond();
					lastMeter = point.getDistance();
				}
			}

			TimeDistance entry = new TimeDistance(lastMeter, second);
			int type = EventData.IN;
			if (scheduledEvent.getSource().equals(train)) {
				type = EventData.SELF;
			}

			String text = scheduledEvent instanceof AbstractEventToTrain ? ((AbstractEventToTrain) scheduledEvent)
					.getEventString() : scheduledEvent.toString();

			List<EventData> events = eventsMap.get(entry);
			if (events == null) {
				events = new ArrayList<EventData>();
				eventsMap.put(entry, events);
			}

			EventData event = new EventData(entry, type, scheduledEvent
					.getClass().getSimpleName(), text);
			events.add(event);
		}

		return eventsMap;
	}

	public List<Coordinate> getTrainPathCoordinates(
			AbstractTrainSimulator train) {
		List<Coordinate> coordniates = new ArrayList<Coordinate>();
		for (LinkEdge edge : train.getFullPath().getEdges()) {
			coordniates.addAll(edge.getCoordinates());
		}

		return coordniates;
	}
}
