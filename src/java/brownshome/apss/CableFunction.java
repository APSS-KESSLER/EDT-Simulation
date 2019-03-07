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
				acrossVelocity(1000)));
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
				Vec3 base = state.position.cross(state.velocity);

				// This needs to rotate around state.position - use a matrix
				// The angle (time % period) / period * 2PI radians if spin is constant speed
				// Find angular displacement as angular displacement plus instantaneous angular speed times time step
				// Find angular speed as angular speed plus torque divided by inertia
				// We need to calculate moment of inertia
				//return base.rotateY(time % period);
				return new Vec3();
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
