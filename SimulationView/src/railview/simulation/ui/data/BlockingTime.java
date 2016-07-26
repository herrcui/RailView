package railview.simulation.ui.data;

public class BlockingTime {
	public BlockingTime(double startMeter, double endMeter,
			double startTimeInSecond, double endTimeInSecond) {
		super();
		this.startMeter = startMeter;
		this.endMeter = endMeter;
		this.startTimeInSecond = startTimeInSecond;
		this.endTimeInSecond = endTimeInSecond;
	}

	public double getStartMeter() {
		return startMeter;
	}

	public double getEndMeter() {
		return endMeter;
	}

	public double getStartTimeInSecond() {
		return startTimeInSecond;
	}

	public double getEndTimeInSecond() {
		return endTimeInSecond;
	}

	private double startMeter;
	private double endMeter;
	private double startTimeInSecond;
	private double endTimeInSecond;
}
