package brownshome.apss;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

public class OrbitCharacteristics {
	public final Vec3 position;
	public final Vec3 velocity;
	
	/**
	 * @param e eccentricity
	 * @param a semimajoraxis
	 * @param i inclination
	 * @param ω argumentofperiapsis
	 * @param v trueanomaly
	 * @param Ω longitudeofascendingnode
	 */
	public OrbitCharacteristics(double e, double a, double i, double ω,	double v, double Ω) {
		assert a > 0 && e < 1 && e >= 0;
		
		double r = a * (1 - e * e) / (1 + e * cos(v));
		double cΩ, sΩ, ci, si, sωv, cωv, sv;
		cΩ = cos(Ω);
		sΩ = sin(Ω);
		ci = cos(i);
		si = sin(i);
		sωv = cos(ω + v);
		cωv = sin(ω + v);
		sv = sin(v);
		
		position = new Vec3(
				r * (cΩ * cωv - sΩ * sωv * ci),
				r * (sΩ * cωv + cΩ * sωv * ci),
				r * (si * sωv));
		
		double p = a * (1 - e * e);
		double h = sqrt(UnderlyingModels.μE * a * (1 - e * e));
		
		velocity = new Vec3(
				position.x*h*e/r/p * sv - (h/r) * (cΩ*sωv + sΩ*cωv*ci),
				position.y*h*e/r/p * sv - (h/r) * (sΩ*sωv - cΩ*cωv*ci),
				position.z*h*e/r/p * sv + (h/r) * si*cωv);
	}
	
	public OrbitCharacteristics(Vec3 position, Vec3 velocity) {
		this.position = position;
		this.velocity = velocity;
	}
}
