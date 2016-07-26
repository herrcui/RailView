package railview.simulation.ui.data;

public class EventData {
	public EventData(TimeDistance timeDistance, int type, String text) {
		super();
		this.timeDistance = timeDistance;
		this.type = type;
		this.text = text;
	}

	public EventData(double meter, double time, int type, String text) {
		super();
		this.timeDistance = new TimeDistance(meter, time);
		this.type = type;
		this.text = text;
	}

	public TimeDistance getTimeDistance() {
		return timeDistance;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return "EventData [timeDistance=" + timeDistance + ", type=" + type
				+ ", text=" + text + "]";
	}

	TimeDistance timeDistance;
	private int type;
	private String text;

	public static final int SELF = 0;
	public static final int IN = 1;
	public static final int OUT = -1;
}
