package brownshome.apss;

public class LorentzForceCalculation {
	private static final int GUESS_ITERATIONS = 100;

	private final OrbitalSimulation.State state;
	private final Emitter emitter;
	private final Cable cable;

	private final double resistivity;
	private final double dIdlConstant;

	/* WORKING VARIABLES */
	private double satVoltage, satCurrent;

	/* RESULTS */
	private Emitter.EmitterResult emitterResult;
	private Vec3[] forcePerUnitLength;

	public LorentzForceCalculation(OrbitalSimulation.State state, Emitter emitter, Cable cable) {
		this.state = state;
		this.emitter = emitter;
		this.cable = cable;

		// Eq (3)
		// This is a scalar that is used to convert the voltage between the plasma and the tether to a dIdl value.
		dIdlConstant = -UnderlyingModels.e * state.plasmaDensity * cable.diameter * Math.sqrt(-2 * UnderlyingModels.e / UnderlyingModels.me);

		// Eq (4)
		// This is the resistance of the cable in ohm / m
		resistivity = 1.0 / (Math.PI * cable.diameter * cable.diameter / 4 * cable.conductivity);
	}

	public void solve() {
		// Try different starting voltages until one them works, in a binary search

		guessVoltage(0);

		double requiredVoltageDrop;

		double low, high;

		requiredVoltageDrop = emitter.calculateRequiredVoltageDrop(satCurrent);

		if(requiredVoltageDrop < -satVoltage) {
			low = 0;
			high = 10;

			do {
				high *= 2;
				guessVoltage(high);
				requiredVoltageDrop = emitter.calculateRequiredVoltageDrop(satCurrent);
			} while(requiredVoltageDrop < -satVoltage);
		} else {
			low = -10;
			high = 0;

			do {
				low *= 2;
				guessVoltage(low);
				requiredVoltageDrop = emitter.calculateRequiredVoltageDrop(satCurrent);
			} while(requiredVoltageDrop < -satVoltage);
		}

		for(int i = 0; i < 250; i++) {
			double mid = (low + high) * 0.5;
			guessVoltage(mid);

			requiredVoltageDrop = emitter.calculateRequiredVoltageDrop(satCurrent);

			if(requiredVoltageDrop < -satVoltage) {
				low = mid;
			} else {
				high = mid;
			}
		}

		emitterResult = emitter.calculateFinalResult(satCurrent, -satVoltage);
	}

	public Emitter.EmitterResult getEmitterResult() {
		return emitterResult;
	}

	public Vec3[] getForcePerUnitLength() {
		return forcePerUnitLength;
	}

	/**
	 * Sets the sat end voltage, and current
	 */
	private void guessVoltage(double startingVoltage) {
		// Current is towards the free end
		double current = 0;
		double voltage = startingVoltage;
		double dl = cable.length / (GUESS_ITERATIONS - 1);

		forcePerUnitLength = new Vec3[GUESS_ITERATIONS];

		// Euler integration of the system.
		//
		for (int i = 0; i < GUESS_ITERATIONS; i++) {
			double distanceFromFreeEnd = i * dl;

			Vec3 direction = cable.getDirection(distanceFromFreeEnd);

			// V/m voltageGradient is the change in the equilibrium voltage as we move towards the sat end of the tether.
			double voltageGradient = cable.getVelocity(distanceFromFreeEnd).cross(state.magneticField).dot(direction);

			// Voltage compared to the plasma
			voltage += voltageGradient * dl + current * resistivity * dl;

			if (voltage > 0) {
				current += Math.sqrt(voltage) * dIdlConstant * dl;
			}

			forcePerUnitLength[i] = direction.cross(state.magneticField).scale(current / dl);
		}


		satVoltage = voltage;
		satCurrent = current;
	}
}
