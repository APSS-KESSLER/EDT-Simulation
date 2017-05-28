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
}
