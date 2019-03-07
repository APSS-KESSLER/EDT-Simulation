package brownshome.apss;

public class LorentzForceCalculator {
	private final OrbitalSimulation simulation;

	public LorentzForceCalculator(OrbitalSimulation parentSimulation) {
		this.simulation = parentSimulation;
	}

	private void lorentzCalculation() {
		double cableLength = cableVector.length();
		//double cableLength = OrbitalSimulation.this.satellite.cableVector.cableLength;
		//System.out.println(cableLength);
		Vec3 cableUnitVector = cableVector.scale(1 / cableLength);

		//Em = (B x v) . dl
		double voltageGradient = velocity.cross(magneticField).dot(cableUnitVector);

		if(voltageGradient < 0)
			return;

		//Eq (3)
		double dIdlConstant = -UnderlyingModels.e * plasmaDensity * OrbitalSimulation.this.satellite.cableDiameter *
				Math.sqrt(-2 * UnderlyingModels.e / UnderlyingModels.me);

		//Eq (4)
		double resistivity = 1.0 / (Math.PI * OrbitalSimulation.this.satellite.cableDiameter
				* OrbitalSimulation.this.satellite.cableDiameter / 4 * OrbitalSimulation.this.satellite.cableConductivity);

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
					currentLengthRadius += changeInCurrentLength * (satellite.centreOfMass - radius);
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
		} while(Math.abs(high - low) > 1e-7 && i < 1000);

		if(i == 1000) {
			System.out.println("Failed to converge");
		} else {
			//System.out.println("Converged");
		}

		current = r.endCurrent;
		lorentzForce = cableUnitVector.cross(magneticField).scale(r.currentLength);
		lorentzTorque = cableUnitVector.cross(cableUnitVector.cross(magneticField)).scale(r.currentLengthRadius);
	}
}
