package brownshome.apss;

import java.util.*;
import java.util.function.*;

import brownshome.apss.OrbitalSimulation.State;

public class Satellite {
	public final CableFunction cableVector;
	public final double mass;
	
	public Satellite(CableFunction cableVector, double mass) {
		this.cableVector = cableVector;
		this.mass = mass;
	}
}
