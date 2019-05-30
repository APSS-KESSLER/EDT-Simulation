package brownshome.apss;

public class PythonSimulatedCable extends Cable {
	private final Vec3[] position, velocity;

	public PythonSimulatedCable(Vec3[] position, Vec3[] velocity, double length, double diameter, double conductivity, double cableDensity, double youngsModulus, double dampeningConstant) {
		super(length, diameter, conductivity, cableDensity, youngsModulus, dampeningConstant);

		this.position = position;
		this.velocity = velocity;
	}

	@Override
	public Vec3 getDirection(double m) {
		return null;
	}

	@Override
	public Vec3 getPosition(double m) {
		return null;
	}

	@Override
	public Vec3 getVelocity(double m) {
		return null;
	}
}
