package railview.simulation.ui.data;

/**
 * An object representing a blockingTime with startMeter, endMeter, startTime
 * and endTime.
 *
 * The time is referenced by the start time of the original timetable.
 */
public class BlockingTime {
	// startTimeInSecond: grantTime.getDifference(trainStartTime).getTotalSeconds();
	// endTimeInSecond: releaseTime.getDifference(trainStartTime).getTotalSeconds();
	public BlockingTime(double startDistance, double endDistance,
			double startTimeInSecond, double endTimeInSecond) {
		super();

		this.relativeStartDistance = startDistance;
		this.relativeEndDistance = endDistance;
		this.relativeStartTimeInSecond = startTimeInSecond;
		this.relativeEndTimeInSecond = endTimeInSecond;
	}

	public double getRelativeStartDistance() {
		return relativeStartDistance;
	}

	public double getRelativeEndDistance() {
		return relativeEndDistance;
	}

	public double getRelativeStartTimeInSecond() {
		return relativeStartTimeInSecond;
	}

	public double getRelativeEndTimeInSecond() {
		return relativeEndTimeInSecond;
	}

	private double relativeStartDistance;
	private double relativeEndDistance;
	private double relativeStartTimeInSecond;
	private double relativeEndTimeInSecond;
}
