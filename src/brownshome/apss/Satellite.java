package brownshome.apss;

import java.util.*;
import java.util.function.*;

import brownshome.apss.OrbitalSimulation.State;

public class Satellite {
	public static final List<CableFunction> CABLE_FUNCTIONS;
	
	static {
		CABLE_FUNCTIONS = Collections.unmodifiableList(Arrays.asList(
				new CableFunction("Towards gravity - 5m") {
					@Override
					public Vec3 apply(State state) {
						return state.gravity.withLength(5.0);
					}
				},
				
				new CableFunction("Across field - 5m") {
					@Override
					public Vec3 apply(State state) {
						return state.magneticField.cross(state.velocity).withLength(5.0);
					};
				}
		));
	}
	
	public final CableFunction cableVector;
	public final double mass;
	
	public Satellite(CableFunction cableVector, double mass) {
		this.cableVector = cableVector;
		this.mass = mass;
	}
}
