package brownshome.apss;

public class OrbitCharacteristics {
	public final Vec3 position;
	public final Vec3 velocity;
	
	public OrbitCharacteristics(double periapsis, double apsis, double inclination, double argumentOfPeriapsis,
				double trueAnomaly,	double longitudeOfAscendingNode) {
		//TODO
		position = new Vec3(0, 7.331e6, 0);
		velocity = new Vec3(Math.sqrt(UnderlyingModels.μE / 7.331e5), 0, 0);
	}
	
	public OrbitCharacteristics(Vec3 position, Vec3 velocity) {
		this.position = position;
		this.velocity = velocity;
	}
}
