package brownshome.apss;

public class Satellite {

	// Constants
	public final static double CUBESAT_DRAG_COEFFICIENT = 2.2;
	public final static double TETHER_DRAG_COEFFICIENT = 2.0;
	public final static double DEFAULT_CABLE_DIAMETER = 0.003; // metres
	public final static double CUBESAT_DIMENSION = 0.1; // metres
	public final static double DEFAULT_CABLE_DENSITY = 5000; // kg/m^3
    public final static double DEFAULT_REVS_PER_ORBIT = 9;
    public final static double DEFAULT_MASS_OF_WEIGHT = 0.050; // kg

	public final CableFunction cableVector;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	public final double centreOfMass;
	public final double bias;
	public final double cableDensity;
	public final double revsPerOrbit; // should be 0 when the cable is not spinning
    public final double massOfWeight;

	/** This is the default constructor used by the MATLAB scripts */
	public Satellite(CableFunction cableVector, double bias, double mass, double cableDiameter,
					 double cableConductivity, double cableDensity, double revsPerOrbit, double massOfWeight) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableDiameter = cableDiameter;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = cableDensity;
		this.revsPerOrbit = revsPerOrbit; // TODO change Matlab interface to work with this
        this.massOfWeight = massOfWeight;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, double bias, double mass, double cableDiameter,
					 double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableDiameter = cableDiameter;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = DEFAULT_CABLE_DENSITY;
		this.revsPerOrbit = DEFAULT_REVS_PER_ORBIT;
		this.massOfWeight = DEFAULT_MASS_OF_WEIGHT;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, double bias, double mass,
					 double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableDiameter = DEFAULT_CABLE_DIAMETER;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = DEFAULT_CABLE_DENSITY;
        this.revsPerOrbit = DEFAULT_REVS_PER_ORBIT;
        this.massOfWeight = DEFAULT_MASS_OF_WEIGHT;
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
				(mass + cableMass + massOfWeight);
		return centreOfMass;
	}

//    /**
//     * Finds the moment of inertia of a spinning tether, assuming the tether is rigid and spinning around its centre
//     * of mass.
//     * @return moment of inertia
//     */
//	public double momentOfInertiaSpinning() {
//
//        // Inertia of a cube rotating about its centre of mass
//		double I_cube_com = mass*Math.pow(CUBESAT_DIMENSION, 2/6);
//
//		// Intertia of a cube rotating about the centre of mass of the system
//		double I_cube = I_cube_com + mass*Math.pow(centreOfMass, 2);
//
//		// Inertia of the tether - assumes tether is straight
//		double x = centreOfMass - CUBESAT_DIMENSION/2; 	// distance from the point where the tether joins the
//														// CubeSat to the centre of mass of the system
//		double l_density = cableDensity * Math.PI * Math.pow(cableDiameter/2, 2);
//		double I_tether = l_density * (Math.pow((cableVector.cableLength - x), 3) + Math.pow(x, 3))/3;
//
//		// Total inertia of the system
//		return I_cube_com + I_cube + I_tether;
//    }
}
