package railview.simulation.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import railapp.infrastructure.dto.Line;
import railapp.infrastructure.dto.Station;
import railapp.infrastructure.object.dto.InfrastructureObject;
import railapp.infrastructure.service.IInfrastructureServiceUtility;
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
	private IInfrastructureServiceUtility infraServiceUtility;
	
	public void setInfraServiceUtility(IInfrastructureServiceUtility infraServiceUtility) {
		this.infraServiceUtility = infraServiceUtility;
	}
	
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
				double endMeter = meter	+ ((PartialRouteResource) resourceOccupancy
						.getResource()).getPartialRoute().getPath().getLength().getMeter();
				double startTimeInSecond = resourceOccupancy.getGrantTime()
						.getDifference(trainStartTime).getTotalSeconds();
				double endTimeInSecond = resourceOccupancy.getReleaseTime()
						.getDifference(trainStartTime).getTotalSeconds();

				if (blockingTimes.size() == 0) { // for the first resource
					endMeter = endMeter	- headDistanceInFirstResource.getMeter();
				}

				if (line == null) {
					blockingTimes.add(new BlockingTime(startMeter, endMeter,
							startTimeInSecond, endTimeInSecond));
				} else {
					double startDist = this.getDistanceInLine(startMeter, train, line);
					double endDist = this.getDistanceInLine(endMeter, train, line);
					if (startDist != -1 && endDist != -1) {
						blockingTimes.add(new BlockingTime(startDist, endDist,
								startTimeInSecond, endTimeInSecond));
					}
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
				double distance = this.getDistanceInLine(meter, train, line);
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
	
	private double getDistanceInLine(double meter, AbstractTrainSimulator train, Line line) {
		int index = 0;
		List<Length> opDistances = this.getOpDistances(train);
		
		if (meter >= opDistances.get(opDistances.size() - 1).getMeter()) {
			return ((InfrastructureObject) train.getTripSection()
					.getTripElements().get(opDistances.size() - 1)
					.getOperationalPoint()).getElement().getStation()
					.getCoordinate().getX();
		}
		
		Collection<Station> stations = new HashSet<Station>();
		stations.addAll(this.infraServiceUtility.getLineService().findStationsByLine(line));

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
					return startStation.getCoordinate().getX()
							+ (meter - dist.getMeter())
							* (endStation.getCoordinate().getX() - startStation
									.getCoordinate().getX())
							/ (opDistances.get(index + 1).getMeter() - opDistances
									.get(index).getMeter());
				} else {
					return -1;
				}
			}
			index++;
		}

		return -1;
	}
}
