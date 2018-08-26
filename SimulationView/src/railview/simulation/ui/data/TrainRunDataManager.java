package railview.simulation.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.simulation.infrastructure.PartialRouteResource;
import railapp.simulation.infrastructure.ResourceOccupancy;
import railapp.simulation.runingdynamics.sections.DiscretePoint;
import railapp.simulation.train.AbstractTrainSimulator;
import railapp.simulation.train.TrainSimulator;
import railapp.timetable.dto.TripElement;
import railapp.units.Length;
import railapp.units.Time;
import railapp.units.UnitUtility;

public class TrainRunDataManager {
	private HashMap<AbstractTrainSimulator, List<Length>> opDistMap = new HashMap<AbstractTrainSimulator, List<Length>>();
	
	public List<BlockingTime> getBlockingTimeStairway(AbstractTrainSimulator train, Line line) {
		List<BlockingTime> blockingTimes = new ArrayList<BlockingTime>();
		if (train instanceof TrainSimulator) {
			double meter = 0;
			Length headDistanceInFirstResource = null;

			List<ResourceOccupancy> resourceOccupancies = ((TrainSimulator) train)
					.getBlockingTimeStairWay();

			Time trainStartTime = train.getTripSection().getStartTime();

			for (ResourceOccupancy resourceOccupancy : resourceOccupancies) {
				if (headDistanceInFirstResource == null) {
					headDistanceInFirstResource = ((PartialRouteResource) resourceOccupancy
							.getResource()).getPath().findFirstDistance(
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
				double endMeter = meter
						+ ((PartialRouteResource) resourceOccupancy
								.getResource()).getPartialRoute().getPath()
								.getLength().getMeter();
				double startTimeInSecond = resourceOccupancy.getGrantTime()
						.getDifference(trainStartTime).getTotalSeconds();
				double endTimeInSecond = resourceOccupancy.getReleaseTime()
						.getDifference(trainStartTime).getTotalSeconds();

				if (blockingTimes.size() == 0) { // for the first resource
					endMeter = endMeter
							- headDistanceInFirstResource.getMeter();
				}

				if (line == null) {
					blockingTimes.add(new BlockingTime(startMeter, endMeter,
							startTimeInSecond, endTimeInSecond));
				} else {
					List<Length> opDistances = this.getOpDistances(train);

					double startDist = this.getDistanceInLine(opDistances,
							startMeter, train);
					double endDist = this.getDistanceInLine(opDistances,
							endMeter, train);
					blockingTimes.add(new BlockingTime(startDist, endDist,
							startTimeInSecond, endTimeInSecond));
				}

				meter = endMeter;
			}

		}

		return blockingTimes;
	}
	
	// Map: Meter, TimeInSecond
	public List<TimeDistance> getTimeInDistance(AbstractTrainSimulator train, Line line) {
		List<TimeDistance> pointList = new ArrayList<TimeDistance>();
		double meter = 0; // x
		double timeInSecond = 0; // y

		for (DiscretePoint point : train.getWholeCoursePoints()) {
			timeInSecond += point.getDuration().getTotalSeconds();
			meter += point.getDistance().getMeter();
			if (line == null) {
				pointList.add(new TimeDistance(meter, timeInSecond));
			} else {
				List<Length> opDistances = this.getOpDistances(train);
				double distance = this.getDistanceInLine(opDistances, meter, train);
				pointList.add(new TimeDistance(distance, timeInSecond));
			}
		}
		return pointList;
	}
	
	public HashMap<AbstractTrainSimulator, List<BlockingTime>> getAllBlockingTimeStairwaysInLine(
			Line line, Collection<AbstractTrainSimulator> trains) {
		HashMap<AbstractTrainSimulator, List<BlockingTime>> result = new HashMap<AbstractTrainSimulator, List<BlockingTime>>();

		for (AbstractTrainSimulator train : trains) {
			result.put(train, this.getBlockingTimeStairway(train, line));
		}
		return result;
	}

	public HashMap<AbstractTrainSimulator, List<TimeDistance>> getAllTimeDistancesInLine(
			Line line, Collection<AbstractTrainSimulator> trains) {
		HashMap<AbstractTrainSimulator, List<TimeDistance>> result = new HashMap<AbstractTrainSimulator, List<TimeDistance>>();
		for (AbstractTrainSimulator train : trains) {
			result.put(train, this.getTimeInDistance(train, line));
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
	
	private double getDistanceInLine(List<Length> opDistances, double meter,
			AbstractTrainSimulator train) {
		int index = 0;
		if (meter >= opDistances.get(opDistances.size() - 1).getMeter()) {
			return ((InfrastructureObject) train.getTripSection()
					.getTripElements().get(opDistances.size() - 1)
					.getOperationalPoint()).getElement().getStation()
					.getCoordinate().getX();
		}

		for (Length dist : opDistances) {
			if (index >= opDistances.size() - 1) {
				break;
			}

			if (opDistances == null || opDistances.get(index + 1) == null) {
				continue;
			}

			if (meter - dist.getMeter() >= UnitUtility.ERROR * -1
					&& opDistances.get(index + 1).getMeter() - meter >= UnitUtility.ERROR
							* -1) {

				Station startStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index)
						.getOperationalPoint()).getElement().getStation();
				Station endStation = ((InfrastructureObject) train
						.getTripSection().getTripElements().get(index + 1)
						.getOperationalPoint()).getElement().getStation();

				return startStation.getCoordinate().getX()
						+ (meter - dist.getMeter())
						* (endStation.getCoordinate().getX() - startStation
								.getCoordinate().getX())
						/ (opDistances.get(index + 1).getMeter() - opDistances
								.get(index).getMeter());
			}
			index++;
		}

		return -1;
	}
}
