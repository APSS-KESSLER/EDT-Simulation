package brownshome.apss;

public class FunctionDrivenCable extends Cable {
	private final OrbitCharacteristics orbit;
	private final Vec3 vector;

	public FunctionDrivenCable(CableFunction vector, OrbitCharacteristics orbit, double diameter, double conductivity, double density, double youngeModulus, double dampingConstant) {
		super(vector.cableLength, diameter, conductivity, density, youngeModulus, dampingConstant);

		this.vector = vector.getCableDirection(orbit);
		this.orbit = orbit;
	}

	@Override
	public Vec3 getDirection(double m) {
		return vector.scale(-1);
	}

	@Override
	public Vec3 getPosition(double m) {
		return orbit.position.add(vector.scale(length - m));
	}

	@Override
	public Vec3 getVelocity(double m) {
		return orbit.velocity; // TODO add rotation to point nadir.
	}
}
