package brownshome.apss;

import java.time.temporal.*;
import java.util.function.UnaryOperator;

import javafx.application.Application;

/**
 * Simulates the orbit of the satalite given an input function for the characteristics of the cable
 * @author James
 */
public class OrbitalSimulation {
	private static final double NANOS_PER_SECOND = 1e9;
	
	private final Satellite satelite;
	private final long timeStep;
	
	long currentTime;
	
	private class Derivative {
		public final Vec3 dp;
		public final Vec3 dv;
		
		public Derivative(State state) {
			dp = state.velocity;
			dv = state.acceleration;
		}

		public Derivative(Derivative a, Derivative b, Derivative c, Derivative d) {
			dp = a.dp.scaleAdd(b.dp, 2.0).scaleAdd(c.dp, 2.0).add(d.dp).scale(1.0 / 6.0);
			dv = a.dv.scaleAdd(b.dv, 2.0).scaleAdd(c.dv, 2.0).add(d.dv).scale(1.0 / 6.0);
		}
	}
	
	public class State {
		public final Vec3 position;
		public final Vec3 velocity;
		public final Vec3 magneticField;
		public final Vec3 acceleration;
		public final Vec3 gravity;
		public final double plasmaDensity;
		
		public State(Vec3 position, Vec3 velocity) {
			this.position = position;
			this.velocity = velocity;
			magneticField = UnderlyingModels.getMagneticFieldStrength(position);
			plasmaDensity = UnderlyingModels.getPlasmaDensity(position);
			gravity = UnderlyingModels.getGravitationalAcceleration(position);
			acceleration = gravity.scaleAdd(lorentzForce(), 1.0 / OrbitalSimulation.this.satelite.mass);
		}

		private Vec3 lorentzForce() {
			return new Vec3();
		}

		private Vec3 gravitationalAcceleration() {
			assert false : "not implemented";
			return null;
		}

		public State scaleAdd(Derivative dSdt, long nanos) {
			return new State(position.scaleAdd(dSdt.dp, nanos / NANOS_PER_SECOND), velocity.scaleAdd(dSdt.dv, nanos / NANOS_PER_SECOND));
		}
	}
	
	private State state;
	
	/**
	 * Takes a function that converts a position to a cable vector
	 * @param cableVector The fucntion.
	 */
	public OrbitalSimulation(OrbitCharacteristics startingOrbit, Satellite satellite, long timeStep) {
		this.satelite = satellite;
		this.timeStep = timeStep;
		currentTime = 0;
	}
	
	/**
	 * Steps one timestep
	 */
	public void step() {
		stepImpl(timeStep);
	}
	
	/**
	 * Step to a specified time from the start of the simulation
	 */
	public void stepTo(TemporalAmount amount) {
		stepTo(amount.get(ChronoUnit.NANOS));
	}
	
	public void stepTo(long nanos) {
		step(nanos - currentTime);
	}
	
	public void step(TemporalAmount amount) {
		step(amount.get(ChronoUnit.NANOS));
	}
	
	public void step(long nanos) {
		while(nanos > timeStep) {
			nanos -= timeStep;
			stepImpl(timeStep);
		}
		
		stepImpl(nanos); //last step
	}
	
	/* Does a single step */
	private void stepImpl(long nanos) {
		//Uses RK4 integration to simulate step
		Derivative a, b, c, d;
		
		a = new Derivative(state);
		b = new Derivative(state.scaleAdd(a, nanos / 2));
		c = new Derivative(state.scaleAdd(b, nanos / 2));
		d = new Derivative(state.scaleAdd(c, nanos));
		
		Derivative dSdt = new Derivative(a, b, c, d);
		state = state.scaleAdd(dSdt, nanos);
	}
	
	public static void main(String[] args) {
		Application.launch(Display.class, args);
	}
}
