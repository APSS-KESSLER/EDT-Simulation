package brownshome.apss;

/**
 * Represents a immultable Vector with three components
 * @author James
 *
 */
public final class Vec3 {
	public final double x, y, z;
	
	/**
	 * Initializes this vector to (0, 0, 0)
	 */
	public Vec3() {
		this(0, 0, 0);
	}
	
	public Vec3(double[] ds) {
		this(ds[0], ds[1], ds[2]);
	}
	
	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/**
	 * Duplicates a vector
	 * @param v The vector to duplicate
	 */
	public Vec3(Vec3 v) {
		this(v.x, v.y, v.z);
	}

	public Vec3 scaleAdd(Vec3 v, double s) {
		return new Vec3(x + v.x * s, y + v.y * s, z + v.z * s);
	}

	public Vec3 add(Vec3 v) {
		return new Vec3(x + v.x, y + v.y, z + v.z);
	}

	public Vec3 scale(double d) {
		return new Vec3(x * d, y * d, z * d);
	}

	public Vec3 withLength(double targetLength) {
		double s = targetLength / length();
		return scale(s);
	}

	/**
	 * Finds the angle between two vectors
	 * @param other other vector
	 * @return angle in radians
	 */
	public double angleBetween(Vec3 other) {
		return Math.acos(this.dot(other)/(this.length()*other.length()));
	}

	public Vec3 withLengthSafe(double targetLength) {
		double length = length();
		if(length < Double.MIN_NORMAL) {
			return new Vec3(0, 0, targetLength);
		} else {
			return scale(targetLength / length);
		}
	}
	
	public double length() {
		return Math.sqrt(lengthSquared());
	}

	public double lengthSquared() {
		return x * x + y * y + z * z;
	}

	public Vec3 cross(Vec3 v) {
		return new Vec3(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}
	
	@Override
	public String toString() {
		return String.format("[%.3g, %.3g, %.3g]", x, y, z);
	}

	public double dot(Vec3 v) {
		return x * v.x + y * v.y + z * v.z;
	}

	public Vec3 rotateY(double angle) {
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		
		return new Vec3(x * c - z * s, y, x * s + z * c);
	}
	
	public Vec3 rotateX(double angle) {
		double s = Math.sin(angle);
		double c = Math.cos(angle);
		
		return new Vec3(x, y * c - z * s, z * c + y * s);
	}

	/**
	 * Rotates the vector around another vector by a given angle
	 * @param toRotateAbout vector to rotate around
	 * @param angle angle to rotate around
	 * @return rotated vector
	 */
	public Vec3 rotateAbout(Vec3 toRotateAbout, double angle){

		// Retrieve unit vector to rotate about
		Vec3 u = toRotateAbout.withLength(1);

		// Define rotation matrix
		double R11 = Math.cos(angle) + Math.pow(u.x, 2) * (1 - Math.cos(angle));
		double R12 = u.x * u.y * (1 - Math.cos(angle)) - u.z * Math.sin(angle);
		double R13 = u.x * u.z * (1 - Math.cos(angle)) + u.y * Math.sin(angle);

		double R21 = u.y * u.x * (1 - Math.cos(angle)) + u.z * Math.sin(angle);
		double R22 = Math.cos(angle) + Math.pow(u.y, 2) * (1 - Math.cos(angle));
		double R23 = u.y * u.z * (1 - Math.cos(angle)) - u.x * Math.sin(angle);

		double R31 = u.z * u.x * (1 - Math.cos(angle)) - u.y * Math.sin(angle);
		double R32 = u.z * u.y * (1 - Math.cos(angle)) + u.x * Math.sin(angle);
		double R33 = Math.cos(angle) + Math.pow(u.z, 2) * (1 - Math.cos(angle));

		// Apply rotation matrix
		double x = R11*this.x + R12*this.y + R13*this.z;
		double y = R21*this.x + R22*this.y + R23*this.z;
		double z = R31*this.x + R32*this.y + R33*this.z;

		return new Vec3(x, y, z);
	}

	/**
	 * Projects a vector onto another vector
	 * @param toProjectOnto vector to project this onto
	 * @return project of this onto other vector
	 */
	public Vec3 projectOnto(Vec3 toProjectOnto) {
		return toProjectOnto.scale(this.dot(toProjectOnto)/toProjectOnto.lengthSquared());
	}
}
