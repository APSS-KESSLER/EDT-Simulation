package brownshome.apss;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;

public class UnderlyingModels {
	/** Radius of the earth (m)*/
	public static final double rE = 6.731e6;
	/** Gm of the earth */
	public static final double μE = 3.986004418e14;
	
	/** Charge of one electron (C)*/
	public static final double e = -1.6021766208e-19;
	/** mass of one electron (kg)*/
	public static final double me = 9.10938356e-31;
	
	/** Conductivity of aluminium (S/m)*/
	public static final double  σAluminium = 3.50e7;
	
	static {
		System.loadLibrary("brownshome_apss_UnderlyingModels");
		if(!initializeWMMData()) {
			throw new OutOfMemoryError();
		}
	}
	
	/**
	 * Calculates the field strength at 
	 * @param position The position in R3 that the satellite is at.
	 * @return The vector of the magnetic field in R3
	 */
	public static Vec3 getMagneticFieldStrength(Vec3 position) {
		double longitude = atan2(position.z, position.x);
		double latitude = atan2(position.y, sqrt(position.x * position.x + position.z * position.z));
		
		return getWMMData(latitude / Math.PI * 180, longitude / Math.PI * 180, position.length(), 2017.5);
	}
	
	/**
	 * 
	 * @param latitude degrees
	 * @param longitude degrees
	 * @param height m
	 * @param time decimal year
	 * @return A Vec3 representing the strength in (North, East Down)
	 */
	private static native Vec3 getWMMData(double latitude, double longitude, double height, double time);

	private static native boolean initializeWMMData();
	
	/** e per m3 */
	public static double getPlasmaDensity(Vec3 position) {
		return 3.16228e11;
	}
	
	public static Vec3 getGravitationalAcceleration(Vec3 position) {
		return position.withLength(-μE / position.lengthSquared());
	}
}
