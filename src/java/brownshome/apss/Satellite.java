package brownshome.apss;

public class Satellite {
	public final CableFunction cableVector;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	
	public Satellite(CableFunction cableVector, double mass, double cableDiameter, double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableConductivity = cableConductivity;
		this.cableDiameter = cableDiameter;
	}
}
