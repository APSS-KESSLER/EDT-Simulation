package brownshome.apss;

public class UnderlyingModels {
	public static final double rE = 6.731e6;
	public static final double μE = 3.986004418e14;
	
	/**
	 * Calculates the field strength at 
	 * @param position The position in R3 that the satalite is at.
	 * @return The vector of the magnetic field in R3
	 */
	public static Vec3 getMagneticFieldStrength(Vec3 position) {
		//assert false : "Not implemented";
		return new Vec3();
	}

	public static double getPlasmaDensity(Vec3 position) {
		//assert false : "Not implemented";
		return 0;
	}
	
	public static Vec3 getGravitationalAcceleration(Vec3 position) {
		return position.withLength(-μE / position.lengthSquared());
	}
}
