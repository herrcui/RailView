package railview.simulation.ui.data;

/**
 * An object representing eventData with timeDistance, type, eventName and text.
 */
public class EventData {
	public EventData(TimeDistance timeDistance, int type, String eventName,
			String text) {
		super();
		this.timeDistance = timeDistance;
		this.type = type;
		this.eventName = eventName;
		this.text = text;
	}

	public TimeDistance getTimeDistance() {
		return timeDistance;
	}

	public int getType() {
		return type;
	}

	public String getEventName() {
		return this.eventName;
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
	private String eventName;
	private String text;

	public static final int SELF = 0;
	public static final int IN = 1;
	public static final int OUT = -1;
}
