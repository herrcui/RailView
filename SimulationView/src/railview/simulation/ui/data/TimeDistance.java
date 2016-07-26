package railview.simulation.ui.data;

public class TimeDistance {
	public TimeDistance(double meter, double second) {
		this.meter = meter;
		this.second = second;
	}

	public double getMeter() {
		return this.meter;
	}

	public double getSecond() {
		return this.second;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(meter);
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
		if (Double.doubleToLongBits(meter) != Double
				.doubleToLongBits(other.meter))
			return false;
		if (Double.doubleToLongBits(second) != Double
				.doubleToLongBits(other.second))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TimeDistance [meter=" + meter + ", second=" + second + "]";
	}

	private double meter;
	private double second;
}
