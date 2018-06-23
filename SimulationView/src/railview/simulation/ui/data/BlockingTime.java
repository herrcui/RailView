package railview.simulation.ui.data;

/**
 * An object representing a blockingTime with startMeter, endMeter, startTime
 * and endTime.
 */
public class BlockingTime {
	public BlockingTime(double startDistance, double endDistance,
			double startTimeInSecond, double endTimeInSecond) {
		super();
		this.startDistance = startDistance;
		this.endDistance = endDistance;
		this.startTimeInSecond = startTimeInSecond;
		this.endTimeInSecond = endTimeInSecond;
	}

	public double getStartDistance() {
		return startDistance;
	}

	public double getEndDistance() {
		return endDistance;
	}

	public double getStartTimeInSecond() {
		return startTimeInSecond;
	}

	public double getEndTimeInSecond() {
		return endTimeInSecond;
	}

	private double startDistance;
	private double endDistance;
	private double startTimeInSecond;
	private double endTimeInSecond;
}
