package brownshome.apss;

public class Satellite {

	// Constants
	public final static double CUBESAT_DRAG_COEFFICIENT = 2.2;
	public final static double TETHER_DRAG_COEFFICIENT = 2.0;
	public final static double DEFAULT_CABLE_DIAMETER = 0.003; // metres
	public final static double CUBESAT_DIMENSION = 0.1; // metres
	public final static double DEFAULT_CABLE_DENSITY = 5000; // kg/m^3
	public final static double DEFAULT_YOUNGE_MODULUS = 128e9; // Pa
	public final static double DEFAULT_DAMPING_CONSTANT = 0.3; // SEAN

	public final CableFunction cableVector;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	public final double centreOfMass;
	public final double bias;
	public final double cableDensity;

	/** This is the force applied by the material if it was extended to double it's length.
	 * e.g. F = k * (m - M) / M
	 **/
	public final double youngsModulus;

	/**
	 * This is how much the tether resists motion due to internal friction.
	 * F_damping = -g(dx/dt) where g is the damping constant
	 * Has units of kg/s or N/(m/s)
	 */
	public final double dampeningConstant;

	/** This is the default constructor used by the MATLAB scripts */
	public Satellite(CableFunction cableVector, double bias, double mass, double cableDiameter,
					 double cableConductivity, double cableDensity, double youngsModulus, double dampeningConstant) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableDiameter = cableDiameter;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = cableDensity;

		this.youngsModulus = youngsModulus;
		this.dampeningConstant = dampeningConstant;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, double bias, double mass, double cableDiameter, double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableDiameter = cableDiameter;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();

		this.cableDensity = DEFAULT_CABLE_DENSITY;
		this.youngsModulus = DEFAULT_YOUNGE_MODULUS;
		this.dampeningConstant = DEFAULT_DAMPING_CONSTANT;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, double bias, double mass, double cableConductivity) {
		this(cableVector, bias, mass, DEFAULT_CABLE_DIAMETER, cableConductivity);
	}

	/**
	 * Finds the centre of mass of the system along the axis of the length of the tether.
	 * The centre of mass is relative to the unattached end of the tether
	 * @return the centre of mass in metres
	 */
	private double centreOfMass() {

		double cableMass = cableDensity * Math.PI*Math.pow(cableDiameter/2, 2) * cableVector.cableLength;

		// Take the end of the tether
		double cubeSatCentreOfMass = cableVector.cableLength + CUBESAT_DIMENSION/2;
		double tetherCentreOfMass = cableVector.cableLength/2;

		double centreOfMass = (mass * cubeSatCentreOfMass + cableMass * tetherCentreOfMass)/
				(mass + cableMass);
		return centreOfMass;
	}
}
