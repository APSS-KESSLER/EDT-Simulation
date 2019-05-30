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

	public final double mass;
	// public final double centreOfMass;
	public final Emitter emitter;
	public final Cable startingCable;
	public final double endMass;

	/** This is the default constructor used by the MATLAB scripts */
	public Satellite(CableFunction cableVector, OrbitCharacteristics orbit, Emitter emitter, double mass, double endMass, double cableDiameter,
					 double cableConductivity, double cableDensity, double youngsModulus, double dampeningConstant) {
		this.mass = mass;
		this.emitter = emitter;
		this.endMass = endMass;
		//this.centreOfMass = centreOfMass();
		this.startingCable = new FunctionDrivenCable(cableVector, orbit, cableDiameter, cableConductivity, cableDensity, youngsModulus, dampeningConstant);
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, OrbitCharacteristics orbit, Emitter emitter, double mass, double endMass, double cableDiameter,
					 double cableConductivity) {
		this.mass = mass;
		this.endMass = endMass;
		this.emitter = emitter;
		//this.centreOfMass = centreOfMass();

		this.startingCable = new FunctionDrivenCable(cableVector, orbit, cableDiameter, cableConductivity, DEFAULT_CABLE_DENSITY, DEFAULT_YOUNGE_MODULUS, DEFAULT_DAMPING_CONSTANT);
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, OrbitCharacteristics orbit, Emitter emitter, double mass, double endMass, double cableConductivity) {
		this.startingCable = new FunctionDrivenCable(cableVector, orbit, DEFAULT_CABLE_DIAMETER, cableConductivity, DEFAULT_CABLE_DENSITY, DEFAULT_YOUNGE_MODULUS, DEFAULT_DAMPING_CONSTANT);
		this.endMass = endMass;
		this.mass = mass;
		this.emitter = emitter;
		//this.centreOfMass = centreOfMass();
	}

	/**
	 * Finds the centre of mass of the system along the axis of the length of the tether.
	 * The centre of mass is relative to the unattached end of the tether
	 * @return the centre of mass in metres
	 */
	private double centreOfMass() {

		/*double cableMass = startingCable.density * Math.PI*Math.pow(startingCable.diameter / 2, 2) * cableVector.cableLength;

		// Take the end of the tether
		double cubeSatCentreOfMass = cableVector.cableLength + CUBESAT_DIMENSION/2;
		double tetherCentreOfMass = cableVector.cableLength/2;

		double centreOfMass = (mass * cubeSatCentreOfMass + cableMass * tetherCentreOfMass)/
				(mass + cableMass);
		return centreOfMass;*/

		return 0.0;
	}
}
