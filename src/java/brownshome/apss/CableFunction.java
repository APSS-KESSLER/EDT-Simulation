package brownshome.apss;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import brownshome.apss.OrbitalSimulation.State;

public abstract class CableFunction implements Function<OrbitalSimulation.State, Vec3> {
	private String name;

	public static final List<CableFunction> CABLE_FUNCTIONS;
	public final double cableLength;

	static {
		CABLE_FUNCTIONS = Collections.unmodifiableList(Arrays.asList(
				towardsGravity(25),
				towardsGravity(50),
				towardsGravity(100),
				towardsGravity(200),
                towardsGravity(500),
                towardsGravity(1000),

				acrossVelocity(25),
				acrossVelocity(50),
				acrossVelocity(100),
				acrossVelocity(200),
				acrossVelocity(500),
				acrossVelocity(1000),

				acrossVelocitySpin(500),
                acrossVelocitySpin(1000)));
	}
	
	private CableFunction(String name, double cableLength) {
		this.name = name;
		this.cableLength = cableLength;
	}
	
	public static CableFunction towardsGravity(double distance) {
		return new CableFunction("Towards gravity - " + distance + "m", distance) {
			@Override
			public Vec3 getCableDirection(State state) {
				return state.gravity;
			}
		};
	}

	public static CableFunction acrossVelocity(double distance) {
		return new CableFunction("Across velocity - " + distance + "m", distance) {
			@Override
			public Vec3 getCableDirection(State state) {
				return state.position.cross(state.velocity);
			}
		};
	}

	public static CableFunction acrossVelocitySpin(double distance) {
		return new CableFunction("Across velocity Spun - " + distance + "m", distance) {
			@Override
			public Vec3 getCableDirection(State state) {

				Vec3 towardsEarth = state.gravity;

				// Base (to define as 0 radians) is component of velocity vector perpendicular to gravity
				Vec3 velocityOntoGravityProjection = state.velocity.projectOnto(towardsEarth);
				Vec3 base = state.velocity.add(velocityOntoGravityProjection.scale(-1));

                // TODO set revs per orbit in Satellite through Matlab

				// Retrieve number of times satellite rotates in orbit
				double revsPerOrbit = state.getRevsPerOrbit();

				// Find current angle between base satellite position and line pointing North
				Vec3 north = new Vec3(0, 1, 0);
				double orbitalAngle = towardsEarth.angleBetween(north);

				if (state.position.x < 0) {
					orbitalAngle = Math.PI/2 - orbitalAngle;
				} else {
					orbitalAngle = orbitalAngle + Math.PI/2;
				}

				// Find fraction of orbit satellite has moved through
				double currentFraction = orbitalAngle / (2*Math.PI);

				// Find total angle satellite will rotate through in an orbit
				double totalAngle = 2 * Math.PI * revsPerOrbit;

				// Find angle to rotate satellite by
				double rotateAngle = currentFraction * totalAngle % (2 * Math.PI);

                // Rotate around base position to find new cable direction
				return base.rotateAbout(towardsEarth, rotateAngle);
			}
		};
	}

	@Override
	public String toString() {
		return name;
	}

	public abstract Vec3 getCableDirection(State state);

    @Override
    public Vec3 apply(State state) {
        return getCableDirection(state).withLength(cableLength);
    }
}
