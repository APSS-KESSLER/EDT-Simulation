package brownshome.apss;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;

import javafx.application.Application;

/**
 * Simulates the orbit of the satellite given an input function for the characteristics of the cable
 * @author James
 */
public class OrbitalSimulation {
	private static final double NANOS_PER_SECOND = 1e9;
	
	public final Satellite satelite;
	
	private long timeStep;
	private long currentTime;
	
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
		public final Vec3 cableVector;
		public Vec3 lorentzForce;
		public Vec3 lorentzTorque;
		public Vec3 dragForce;
		public Vec3 dragTorque;
		public Vec3 netTorque;
		public final Vec3 gravityGradientTorque;
		public final double plasmaDensity;
        public final double atmosphericDensity;
		public double current;
		
		public State(Vec3 position, Vec3 velocity) {
			this.position = position;
			this.velocity = velocity;
			magneticField = UnderlyingModels.getMagneticFieldStrength(position);
			plasmaDensity = UnderlyingModels.getPlasmaDensity(position);
            atmosphericDensity = UnderlyingModels.getAtmosphericDensity(position);
			gravity = UnderlyingModels.getGravitationalAcceleration(position);
			cableVector = OrbitalSimulation.this.satelite.cableVector.apply(this);
			lorentzForce = new Vec3();
			lorentzTorque = new Vec3();
            lorentzCalculation(); // calculate Lorentz force and torque
			dragForce = new Vec3();
			dragTorque = new Vec3();
			dragCalculation();
			netTorque = dragTorque.add(lorentzTorque);
			gravityGradientTorque = gravityGradientTorque();
			acceleration = gravity.scaleAdd(lorentzForce.add(dragForce), 1.0 / OrbitalSimulation.this.satelite.mass);
		}

		public State(OrbitCharacteristics orbit) {
			this(orbit.position, orbit.velocity);
		}

		private void lorentzCalculation() {
			double cableLength = cableVector.length();
            //double cableLength = OrbitalSimulation.this.satelite.cableVector.cableLength;
			//System.out.println(cableLength);
			Vec3 cableUnitVector = cableVector.scale(1 / cableLength);
			
			//Em = (B x v) . dl
			double voltageGradient = velocity.cross(magneticField).dot(cableUnitVector);
			
			if(voltageGradient < 0)
				return;
			
			//Eq (3) 
			double dIdlConstant = -UnderlyingModels.e * plasmaDensity * OrbitalSimulation.this.satelite.cableDiameter * 
					Math.sqrt(-2 * UnderlyingModels.e / UnderlyingModels.me);
			
			//Eq (4)
			double resistivity = 1.0 / (Math.PI * OrbitalSimulation.this.satelite.cableDiameter
					* OrbitalSimulation.this.satelite.cableDiameter / 4 * OrbitalSimulation.this.satelite.cableConductivity);
			
			//Keep iterating with different starting voltages to find the voltage where Vc + RI + Ve = Vemf * l, this should be findable with a binary search.
			
			class Result {
				double currentLength = 0;
				double currentLengthRadius = 0;
				double endCurrent;
				double endVoltage;
			
				Result(double startingVoltage, int iterations) {
					double current = 0;
					double voltage = startingVoltage;
					double dl = cableLength / iterations;
					
					//Euler integration of the system
					for (int i = 0; i < iterations; i++) {
                        double radius = i * dl;
						double deltaV = voltage - radius * voltageGradient;
						
						voltage += current * resistivity * dl;
						
						if (deltaV > 0) {
							current += Math.sqrt(deltaV) * dIdlConstant * dl;
						}

						double changeInCurrentLength = current * dl;

						currentLength += changeInCurrentLength;
						currentLengthRadius += changeInCurrentLength * (satelite.centreOfMass - radius);
					}
					
					endCurrent = current;
					endVoltage = voltage;
				}
			}
			
			double targetEndVoltage = voltageGradient * cableLength;
			double low = -1000;
			double high = targetEndVoltage + 1000;
			double endVoltage;
			
			Result r;
			int i = 0;
			
			r = new Result(low, 10);
			endVoltage = emitterVoltageDrop(r.endCurrent) + r.endVoltage;
			if(endVoltage > targetEndVoltage) {
				current = 0;
				return;
			}
			
			do {
				i++;
				int iterations = 10;
				if(high - low < 10) {
					iterations = 100;
				}
				
				double mid = low / 2 + high / 2;
				r = new Result(mid, iterations);
				endVoltage = emitterVoltageDrop(r.endCurrent) + r.endVoltage;
				if(endVoltage < targetEndVoltage) {
					low = mid;
				} else {
					high = mid;
				}
			} while(Math.abs(low - high) / targetEndVoltage > 1e-4 && i < 1000);
			
			if(i == 1000) {
				System.out.println("Failed to converge");
			} else {
			    //System.out.println("Converged");
            }
			
			current = r.endCurrent;
			lorentzForce = cableUnitVector.cross(magneticField).scale(r.currentLength);
			lorentzTorque = cableUnitVector.cross(cableUnitVector.cross(magneticField)).scale(r.currentLengthRadius);
		}



        private Vec3 gravityGradientTorque() {
            return new Vec3(); // needs to be calculated
		}
		
		private double emitterVoltageDrop(double endCurrent) {
			//https://ieeexplore-ieee-org.ezproxy.auckland.ac.nz/stamp/stamp.jsp?tp=&arnumber=4480910
			
			return 35; //Crappy estimate, but gets the job done
		}

        /**
         * Finds the drag force and torque acting on the tether due to the drag force.
         */
		private void dragCalculation() {
		    double rCubeSat = -(satelite.cubeSatDimension/2 + satelite.cableVector.cableLength - satelite.centreOfMass);
            double rTether1 = -(satelite.cableVector.cableLength - satelite.centreOfMass)/2;
            double rTether2 = satelite.centreOfMass/2;

		    double velocityScalar = cableVector.dot(velocity)/Math.pow(velocity.length(),2);
		    Vec3 scaledVelocity = velocity.scale(velocityScalar);
		    double effectiveAreaRatio = cableVector.add(scaledVelocity).length();

            double fCubeSat = -0.5 * atmosphericDensity * satelite.cubeSatDragCoefficient * velocity.lengthSquared()
                    * Math.pow(satelite.cubeSatDimension, 2);

            double fTether = -0.5 * atmosphericDensity * satelite.tetherDragCoefficient *
                    Math.pow(velocity.length(),2)*effectiveAreaRatio*satelite.cableDiameter;
            double fTether1 = ((satelite.cableVector.cableLength-satelite.centreOfMass)/
                    satelite.cableVector.cableLength)*fTether;
            double fTether2 = (satelite.centreOfMass/satelite.cableVector.cableLength)*fTether;

            dragForce = velocity.withLength(fCubeSat + fTether);
            dragTorque = cableVector.withLength(1).cross(velocity.withLength(1))
                    .scale(rCubeSat*fCubeSat + rTether1*fTether1 + rTether2*fTether2);
            System.out.println(dragTorque.length());
		}

		public State scaleAdd(Derivative dSdt, long nanos) {
			return new State(position.scaleAdd(dSdt.dp, nanos / NANOS_PER_SECOND), velocity.scaleAdd(dSdt.dv,
                    nanos / NANOS_PER_SECOND));
		}
	}
	
	private State state;
	
	/**
	 * Takes a function that converts a position to a cable vector
	 */
	public OrbitalSimulation(OrbitCharacteristics startingOrbit, Satellite satellite, long timeStep) {
		this.satelite = satellite;
		this.timeStep = timeStep;
		currentTime = 0;
		
		state = new State(startingOrbit);
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
		//Uses RK4 integration to simulate step
		Derivative a, b, c, d;
		
		a = new Derivative(state);
		b = new Derivative(state.scaleAdd(a, nanos / 2));
		c = new Derivative(state.scaleAdd(b, nanos / 2));
		d = new Derivative(state.scaleAdd(c, nanos));
		
		Derivative dSdt = new Derivative(a, b, c, d);
		state = state.scaleAdd(dSdt, nanos);
		currentTime += nanos;
	}
	
	public static void main(String[] args) {
		Application.launch(Display.class, args);
	}

	public State getState() {
		return state;
	}
}
