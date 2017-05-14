package brownshome.apss;

import java.util.function.Function;

public abstract class CableFunction implements Function<OrbitalSimulation.State, Vec3> {
	private String name;
	
	public CableFunction(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
