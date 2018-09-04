package brownshome.apss;

import java.util.*;
import java.util.function.*;

import brownshome.apss.OrbitalSimulation.State;

public class Satellite {
	public final CableFunction cableVector;
	public final double cubeSatDimension;
	public final double mass;
	public final double cableDiameter;
	public final double cableConductivity;
	
	public Satellite(CableFunction cableVector, double cubeSatDimension, double mass, double cableDiameter,
					 double cableConductivity) {
		this.cableVector = cableVector;
		this.mass = mass;
		this.cableConductivity = cableConductivity;
		this.cableDiameter = cableDiameter;
		this.cubeSatDimension = cubeSatDimension;
	}
}
