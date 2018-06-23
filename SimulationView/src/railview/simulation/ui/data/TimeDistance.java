package railview.simulation.ui.data;

/**
 * An object representing timeDistance with meters and seconds.
 */
public class TimeDistance {
	public TimeDistance(double distance, double second) {
		this.distance = distance;
		this.second = second;
	}

	public double getDistance() {
		return this.distance;
	}

	public double getSecond() {
		return this.second;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(second);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		TimeDistance other = (TimeDistance) obj;
		if (Double.doubleToLongBits(distance) != Double
				.doubleToLongBits(other.distance))
			return false;
		if (Double.doubleToLongBits(second) != Double
				.doubleToLongBits(other.second))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimeDistance [meter=" + distance + ", second=" + second + "]";
	}

	private double distance;
	private double second;
}
