package brownshome.apss;

/** This class represents the cable. This may be functionally driven, or driven by the python simulation. */
public abstract class Cable {
	public final double length;
	public final double diameter;
	public final double conductivity;
	public final double density;


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

	protected Cable(double length, double diameter, double conductivity, double density, double youngsModulus, double dampeningConstant) {
		this.length = length;
		this.diameter = diameter;
		this.conductivity = conductivity;
		this.density = density;
		this.youngsModulus = youngsModulus;
		this.dampeningConstant = dampeningConstant;
	}

	public abstract Vec3 getDirection(double m);
	public abstract Vec3 getPosition(double m);
	public abstract Vec3 getVelocity(double m);
}
