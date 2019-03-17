package brownshome.apss;

/**
 * This class represents the electron emitter and the controlling circuitry.
 *
 * It handles the calculation of the voltage drop
 **/
public class Emitter {
	private static final double POWER_LIMIT = 1.5, CURRENT_LIMIT = 12e-3, BIAS_MAX = 200;

	private static final double THERMAL_CATHODE_DROP = 200;


	/** These values will be NaN if they are undefined. */
	private final double powerLimit, currentLimit, technologyVoltageDrop, setBias;

	/** Constructs an emitter with an adjustable bias */
	public Emitter(double powerLimit, double currentLimit, double technologyVoltageDrop) {
		assert Double.isFinite(technologyVoltageDrop)
				&& (Double.isFinite(powerLimit) || Double.isFinite(currentLimit));

		this.powerLimit = powerLimit;
		this.currentLimit = currentLimit;
		this.technologyVoltageDrop = technologyVoltageDrop;
		this.setBias = Double.NaN;
	}

	/** Constructs an emitter with a set bias voltage. These values may be set to NaN if they are to be unlimited. */
	public Emitter(double powerLimit, double currentLimit, double setBias, double technologyVoltageDrop) {
		assert Double.isFinite(setBias) && Double.isFinite(technologyVoltageDrop);

		this.powerLimit = powerLimit;
		this.currentLimit = currentLimit;
		this.setBias = setBias;
		this.technologyVoltageDrop = technologyVoltageDrop;
	}

	public static Emitter createThermionicCathode() {
		return new Emitter(POWER_LIMIT, CURRENT_LIMIT, THERMAL_CATHODE_DROP);
	}

	public static Emitter createThermionicCathode(double bias, boolean withLimits) {
		return new Emitter(withLimits ? POWER_LIMIT : Double.NaN, withLimits ? CURRENT_LIMIT : Double.NaN, bias, THERMAL_CATHODE_DROP);
	}

	/** Power limit does not include the constant power draw. */
	public static Emitter createThermionicCathode(double powerLimit, double currentLimit) {
		return new Emitter(powerLimit, currentLimit, THERMAL_CATHODE_DROP);
	}

	public static Emitter createThermionicCathode(double powerLimit, double currentLimit, double bias) {
		return new Emitter(powerLimit, currentLimit, bias, THERMAL_CATHODE_DROP);
	}

	/** Calculates an emitter result from a given current and voltage drop */
	public EmitterResult calculateFinalResult(double current, double voltageDrop) {
		double bias = technologyVoltageDrop - voltageDrop;

		// No negative biases
		if(bias < 0) bias = 0;

		double power = bias * current;

		return new EmitterResult(power, bias, voltageDrop);
	}

	public static final class EmitterResult {
		public final double powerUsage, chosenBias, voltageDrop;

		public EmitterResult() {
			powerUsage = 0;
			chosenBias = 0;
			voltageDrop = 0;
		}

		private EmitterResult(double powerUsage, double chosenBias, double voltageDrop) {
			this.powerUsage = powerUsage;
			this.chosenBias = chosenBias;
			this.voltageDrop = voltageDrop;
		}
	}

	/**
	 * Calculates the voltage drop required to emit a given current.
	 *
	 * @param current the electron current to emit from the emitter. This must be positive, or zero.
	 * @return The the voltage below ground that the emitter is required to be.
	 **/
	public double calculateRequiredVoltageDrop(double current) {
		// Check the current limit
		if(Double.isFinite(currentLimit) && current > currentLimit) {
			return Double.POSITIVE_INFINITY;
		}

		if(current == 0) {
			return Double.NEGATIVE_INFINITY;
		}

		// Provide as much bias as we can for a given power limit and respecting setBias
		double bias;

		if(Double.isFinite(setBias)) {
			bias = setBias;
		} else if(Double.isFinite(powerLimit)) {
			bias = powerLimit / current;
		} else {
			// No power limit, and we are below the current limit. We require no voltage.
			bias = Double.POSITIVE_INFINITY;
		}

		bias = Math.min(bias, BIAS_MAX);

		return technologyVoltageDrop - bias;
	}
}
