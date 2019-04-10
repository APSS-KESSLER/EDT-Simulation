package brownshome.apss;

import static java.lang.Math.atan2;
import static java.lang.Math.sqrt;
import java.io.*;

public class UnderlyingModels {
	/** Radius of the earth (m)*/
	public static final double rE = 6.371e6;
	/** Gm of the earth */
	public static final double μE = 3.986004418e14;
	
	/** Charge of one electron (C)*/
	public static final double e = -1.6021766208e-19;
	/** mass of one electron (kg)*/
	public static final double me = 9.10938356e-31;
	
	/** Conductivity of aluminium (S/m)*/
	public static final double  σAluminium = 3.50e7;

	private static final double plasmaDensity = 3.16228e11;
	
	static {
		System.loadLibrary("library");
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
		double longitude = atan2(position.x, -position.z);
		double latitude = atan2(position.y, sqrt(position.x * position.x + position.z * position.z));
		
		Vec3 NED = getWMMData(latitude / Math.PI * 180, longitude / Math.PI * 180, position.length(), 2017.5).scale(1e-9);
		
		//y is up, x is right, z is forward. 0-longitude is (0, 0, -rE)
		return translateNED(new Vec3(NED.y, NED.x, NED.z), latitude, longitude);
	}
	
	private static Vec3 translateNED(Vec3 ned, double latitude, double longitude) {
		//rotate up by longitude
		//rotate by latitude
		
		ned = ned.rotateX(latitude);
		return ned.rotateY(longitude);
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
		return plasmaDensity;
	}

	public static double getAtmosphericDensity(Vec3 position) {
	    return 1.93e-11; // TODO better value
    }


	public static Vec3 getGravitationalAcceleration(Vec3 position) {
        double longitude = atan2(position.x, -position.z);
        double latitude = atan2(position.y, sqrt(position.x * position.x + position.z * position.z));

		// Altitude of satellite considering constant earth radius
		double altitude = position.length() - rE;

		String coordinates = "\"" + longitude + " " + latitude + " " + altitude + "\"";

		// File path of the gravity model exe
		String modelPath = "src\\library\\GeographicLib-1.49\\bin\\Gravity.exe ";

        // Run the model using the most recent (2008) egm data
		String[] command = {modelPath, "--input-string", coordinates, "-n", "egm2008"};
		String accelerationString = UnderlyingModels.runCommand(command);

		double[] acceleration = new double[3];
		String[] accStrings = accelerationString.split(" ");

		for(int i = 0; i < accStrings.length; i++) {
            acceleration[i] = Double.parseDouble(accStrings[i]);
        }

        Vec3 NED = ENUtoNED(new Vec3(acceleration[0], acceleration[1], acceleration[2]));
        Vec3 translatedNED = translateNED(NED, latitude, longitude);

        System.out.println("NED! " + translatedNED);

        //TODO: remove centrifugal acceleration.
        //TODO: compute the correct altitude
		return translatedNED;
	}

	// Converts East, North, Up (default from gravity model) coordinates to North, East, Down.
	public static Vec3 ENUtoNED(Vec3 ENU) {
	    return new Vec3(ENU.y, ENU.x, -ENU.z);
    }

	public static String runCommand(String[] command) {

		try {
		    Process p = Runtime.getRuntime().exec(command);

			BufferedReader stdInput = new BufferedReader(new
					InputStreamReader(p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(p.getErrorStream()));

			String s = "", result = "";

			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				result = result + s;
			}

			// read any errors from the attempted command
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

			return result;
		}
		catch (IOException e) {
			System.out.println("exception happened - here's what I know: ");
			e.printStackTrace();
			System.exit(-1);
		}
		return "";
	}
}
