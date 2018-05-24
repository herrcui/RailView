package railview.simulation.ui.data;

/**
 * An object representing tableProperty with items and values.
 */
public class TableProperty {
	public TableProperty(String item, String value) {
		super();
		this.item = item;
		this.value = value;
	}

	public String getItem() {
		return item;
	}

	public String getValue() {
		return value;
	}

	private String item;
	private String value;
}
