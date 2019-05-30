package brownshome.apss;

import java.time.Duration;
import java.util.Arrays;

import javafx.application.Application;

/**
 * Simulates the orbit of the satellite given an input function for the characteristics of the cable
 * @author James
 */
public class OrbitalSimulation {
	private static final double NANOS_PER_SECOND = 1e9;
	private static final int SEGMENTS_OF_TETHER = 50;

	public final Satellite satellite;
	public final ExternalInterlink tetherSimulation = new ExternalInterlink(Arrays.asList("python", "TetherSimulation.py"));

	public final double[] masses;
	public final double[] lengths;

	private long timeStep;
	private long currentTime;
	
	public class State {
		public final PythonSimulatedCable cable;
		public final Vec3[] forces;

		public final Vec3[] lorentzForce;
		public final Vec3[] dragForce;
		public final Vec3[] gravity;

		public final Vec3 magneticField;
		public final double plasmaDensity;
        public final double atmosphericDensity;

        public double current;
		public final long time;
		public Emitter.EmitterResult emitterResult;

		public Vec3 averagePosition;
		public Vec3 averageVelocity;
		public Vec3 totalLorentzForce;

		public State(Vec3[] position, Vec3[] velocity, Cable oldCable, long time) {
			this.time = time;
			this.cable = new PythonSimulatedCable(position, velocity, oldCable.length, oldCable.diameter, oldCable.conductivity, oldCable.density, oldCable.youngsModulus, oldCable.dampeningConstant);

			averagePosition = new Vec3();
			averageVelocity = new Vec3();

			for(int i = 0; i < SEGMENTS_OF_TETHER; i++) {
				averagePosition = averagePosition.add(position[i]);
				averageVelocity = averageVelocity.add(velocity[i]);
			}

			averageVelocity = averageVelocity.scale(1.0 / SEGMENTS_OF_TETHER);
			averagePosition = averagePosition.scale(1.0 / SEGMENTS_OF_TETHER);

			magneticField = UnderlyingModels.getMagneticFieldStrength(averagePosition);
			plasmaDensity = UnderlyingModels.getPlasmaDensity(averagePosition);
			atmosphericDensity = UnderlyingModels.getAtmosphericDensity(averagePosition);

			forces = new Vec3[position.length];
			gravity = new Vec3[position.length];
			dragForce = new Vec3[position.length];

			for(int i = 0; i < gravity.length; i++) {
				gravity[i] = UnderlyingModels.getGravitationalAcceleration(position[i]);
			}

			LorentzForceCalculation lorentzForceCalculation = new LorentzForceCalculation(this, satellite.emitter, cable);
			lorentzForceCalculation.solve();
			emitterResult = lorentzForceCalculation.getEmitterResult();
			lorentzForce = lorentzForceCalculation.getForcePerUnitLength();

			totalLorentzForce = new Vec3();

			// Scale forces by length and mass and sum to get total forces.
			for(int i = 0; i < lorentzForce.length; i++) {
				lorentzForce[i] = lorentzForce[i].scale(lengths[i]);
				totalLorentzForce.add(lorentzForce[i]);
				forces[i] = gravity[i].scale(masses[i]).add(lorentzForce[i]);
			}
		}

		public State(OrbitCharacteristics orbit, long time) {
			this(extractPositions(satellite.startingCable), extractVelocities(satellite.startingCable), satellite.startingCable, time);
		}

        /**
         * Finds the drag force and torque acting on the tether due to the drag force.
         */
		/*private void dragCalculation() {
			double rCubeSat = -(Satellite.CUBESAT_DIMENSION / 2 + satellite.cableVector.cableLength - satellite.centreOfMass);
			double rTether1 = -(satellite.cableVector.cableLength - satellite.centreOfMass) / 2;
			double rTether2 = satellite.centreOfMass / 2;

			double velocityScalar = cableVector.dot(velocity) / Math.pow(velocity.length(), 2);
			Vec3 scaledVelocity = velocity.scale(velocityScalar);
			double effectiveAreaRatio = cableVector.add(scaledVelocity).length();

			double fCubeSat = -0.5 * atmosphericDensity * Satellite.CUBESAT_DRAG_COEFFICIENT * velocity.lengthSquared()
					* Math.pow(Satellite.CUBESAT_DIMENSION, 2);

			double fTether = -0.5 * atmosphericDensity * Satellite.TETHER_DRAG_COEFFICIENT *
					Math.pow(velocity.length(), 2) * effectiveAreaRatio * satellite.cableDiameter;
			double fTether1 = ((satellite.cableVector.cableLength - satellite.centreOfMass) /
					satellite.cableVector.cableLength) * fTether;
			double fTether2 = (satellite.centreOfMass / satellite.cableVector.cableLength) * fTether;

			dragForce = velocity.withLength(fCubeSat + fTether);
			dragTorque = cableVector.withLength(1).cross(velocity.withLength(1))
					.scale(rCubeSat * fCubeSat + rTether1 * fTether1 + rTether2 * fTether2);
		}*/
	}

	private static Vec3[] extractPositions(Cable startingCable) {
		Vec3[] positions = new Vec3[SEGMENTS_OF_TETHER];

		double dl = startingCable.length / (SEGMENTS_OF_TETHER - 1);
		for(int i = 0; i < SEGMENTS_OF_TETHER; i++) {
			positions[i] = startingCable.getPosition(dl * i);
		}

		return positions;
	}

	private static Vec3[] extractVelocities(Cable startingCable) {
		Vec3[] velocites = new Vec3[SEGMENTS_OF_TETHER];

		double dl = startingCable.length / (SEGMENTS_OF_TETHER - 1);
		for(int i = 0; i < SEGMENTS_OF_TETHER; i++) {
			velocites[i] = startingCable.getVelocity(dl * i);
		}

		return velocites;
	}

	private State state;
	
	/**
	 * Takes a function that converts a position to a cable vector
	 */
	public OrbitalSimulation(OrbitCharacteristics startingOrbit, Satellite satellite, long timeStep) {
		this.satellite = satellite;
		this.timeStep = timeStep;
		currentTime = 0;
		
		state = new State(startingOrbit, timeStep);

		masses = new double[SEGMENTS_OF_TETHER];
		lengths = new double[SEGMENTS_OF_TETHER];

		Arrays.fill(lengths, satellite.startingCable.length / (SEGMENTS_OF_TETHER - 1));
		lengths[0] *= 0.5;
		lengths[SEGMENTS_OF_TETHER - 1] *= 0.5;

		for(int i = 0; i < lengths.length; i++) {
			masses[i] = lengths[i] * satellite.startingCable.density;
		}

		masses[0] += satellite.endMass;
		masses[SEGMENTS_OF_TETHER - 1] += satellite.mass;
	}
	
	public long getCurrentTime() {
		return currentTime;
	}
	
	public long getTimeStep() {
		return timeStep;
	}
	
	public void setTimeStep(long timeStep) {
		this.timeStep = timeStep;
	}
	
	/**
	 * Steps one timestep
	 */
	public void step() {
		stepImpl(timeStep);
	}
	
	public void step(Duration amount) {
		step(amount.toNanos());
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
		// TODO use python script
	}
	
	public static void main(String[] args) {
		Application.launch(Display.class, args);
	}

	public State getState() {
		return state;
	}
}
