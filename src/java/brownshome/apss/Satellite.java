package brownshome.apss;

public class Satellite {
	public final CableFunction cableVector;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	public final double bias;
	
	public Satellite(CableFunction cableVector, double bias, double mass, double cableDiameter, double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.bias = bias;
		this.cableConductivity = cableConductivity;
		this.cableDiameter = cableDiameter;
	}
}
