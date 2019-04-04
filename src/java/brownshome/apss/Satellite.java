package brownshome.apss;

public class Satellite {

	// Constants
	public final static double CUBESAT_DRAG_COEFFICIENT = 2.2;
	public final static double TETHER_DRAG_COEFFICIENT = 2.0;
	public final static double DEFAULT_CABLE_WIDTH = 0.05; // metres
	public final static double DEFAULT_CABLE_THICKNESS = 0.0005; // metres
	public final static double CUBESAT_DIMENSION = 0.1; // metres
	public final static double DEFAULT_CABLE_DENSITY = 5000; // kg/m^3

	public final CableFunction cableVector;
	public final double mass;

	public final double cableWidth;
	public final double cableThickness;

	public final double cableConductivity;
	public final double centreOfMass;
	public final double cableDensity;
	public final Emitter emitter;

	/** This is the default constructor used by the MATLAB scripts */
	public Satellite(CableFunction cableVector, Emitter emitter, double mass, double cableWidth, double cableThickness,
					 double cableConductivity, double cableDensity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableWidth = cableWidth;
		this.cableThickness = cableThickness;
		this.emitter = emitter;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = cableDensity;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, Emitter emitter, double mass, double cableWidth, double cableThickness,
					 double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableWidth = cableWidth;
		this.cableThickness = cableThickness;
		this.emitter = emitter;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = DEFAULT_CABLE_DENSITY;
	}

	/** This is a constructor used by the Java simulation program */
	public Satellite(CableFunction cableVector, Emitter emitter, double mass,
					 double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableWidth = DEFAULT_CABLE_WIDTH;
		this.cableThickness = DEFAULT_CABLE_THICKNESS;
		this.emitter = emitter;
		this.cableConductivity = cableConductivity;
		this.centreOfMass = centreOfMass();
		this.cableDensity = DEFAULT_CABLE_DENSITY;
	}

	/**
	 * Finds the centre of mass of the system along the axis of the length of the tether.
	 * The centre of mass is relative to the unattached end of the tether
	 * @return the centre of mass in metres
	 */
	private double centreOfMass() {

		double cableMass = cableDensity * cableWidth * cableThickness * cableVector.cableLength;

		// Take the end of the tether
		double cubeSatCentreOfMass = cableVector.cableLength + CUBESAT_DIMENSION/2;
		double tetherCentreOfMass = cableVector.cableLength/2;

		double centreOfMass = (mass * cubeSatCentreOfMass + cableMass * tetherCentreOfMass)/
				(mass + cableMass);
		return centreOfMass;
	}
}
