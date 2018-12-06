package brownshome.apss;

public class Satellite {
	public final CableFunction cableVector;
	public final double cubeSatDimension;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	public final double cubeSatDragCoefficient;
	public final double tetherDragCoefficient;
	public final double centreOfMass;
	public final double bias;
	
	public Satellite(CableFunction cableVector, double bias, double cubeSatDimension, double mass, double cableDiameter,
					 double cableConductivity,
					 double cubeSatDragCoefficient, double tetherDragCoefficient) {
		this.cableVector = cableVector;
		this.cubeSatDimension = cubeSatDimension;
		this.mass = mass;
		this.cableDiameter = cableDiameter;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.cubeSatDragCoefficient = cubeSatDragCoefficient;
		this.tetherDragCoefficient = tetherDragCoefficient;
		this.centreOfMass = centreOfMass();
	}

	/**
	 * Finds the centre of mass of the system along the axis of the length of the tether.
	 * The centre of mass is relative to the unattached end of the tether
	 * @return the centre of mass in metres
	 */
	private double centreOfMass() {

		// Need to calculate density; 5000 kg/m^3 is from FDP
		double cableMass = 5000 * Math.PI*Math.pow(cableDiameter/2, 2) * cableVector.cableLength;

		// Take the end of the tether
		double cubeSatCentreOfMass = cableVector.cableLength + cubeSatDimension/2;
		double tetherCentreOfMass = cableVector.cableLength/2;

		double centreOfMass = (mass * cubeSatCentreOfMass + cableMass * tetherCentreOfMass)/
				(mass + cableMass);
		return centreOfMass;
	}
}
