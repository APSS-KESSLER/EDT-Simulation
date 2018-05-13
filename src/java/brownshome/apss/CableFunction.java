package brownshome.apss;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import brownshome.apss.OrbitalSimulation.State;

public abstract class CableFunction implements Function<OrbitalSimulation.State, Vec3> {
	private String name;

	public static final List<CableFunction> CABLE_FUNCTIONS;

	static {
		CABLE_FUNCTIONS = Collections.unmodifiableList(Arrays.asList(
				new CableFunction("Towards gravity - 50m") {
					@Override
					public Vec3 apply(State state) {
						return state.gravity.withLength(50);
					}
				},
				
				acrossVelocity(25),
				acrossVelocity(50),
				acrossVelocity(100),
				acrossVelocity(200),
				acrossVelocity(500),
				acrossVelocity(1000)));
	}
	
	private CableFunction(String name) {
		this.name = name;
	}
	
	static CableFunction acrossVelocity(double distance) {
		return new CableFunction("Across velocity - " + distance + "m") {
			@Override
			public Vec3 apply(State state) {
				return state.position.cross(state.velocity).withLength(distance);
			}
		};
	}
	
	@Override
	public String toString() {
		return name;
	}
}
