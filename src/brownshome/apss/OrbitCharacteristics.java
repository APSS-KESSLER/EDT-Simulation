package brownshome.apss;

public class OrbitCharacteristics {
	public final Vec3 position;
	public final Vec3 velocity;
	
	public OrbitCharacteristics(double periapsis, double apsis, double inclination, double argumentOfPeriapsis,
				double trueAnomaly,	double longitudeOfAscendingNode) {
		position = null;
		velocity = null;
	}
	
	public OrbitCharacteristics(Vec3 position, Vec3 velocity) {
		this.position = position;
		this.velocity = velocity;
	}
}
