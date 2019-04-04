package brownshome.apss;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import brownshome.apss.OrbitalSimulation.State;
import javafx.application.Application;

public class APSSSimulator {
	public static void main(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("v", "Use the visualization");
		
		//Command line options
		options.addOption("dir", true, "Cable direction (Named)");
		options.addOption("inc", true, "Inclination (radians from pole)");
		options.addOption("height", true, "Height (m above the core of the earth)");
		options.addOption("mass", true, "Mass (kg)");
		options.addOption("diam", true, "The cable diameter (m)");
		options.addOption("cond", true, "The cable conductivity (ohm^-1 * m^-1)");
		
		if(args.length == 1 && args[0].equals("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "java -jar JAR", options );
			
			return;
		}
		
		CommandLineParser parser = new DefaultParser();
		CommandLine line = parser.parse(options, args);
		
		if(line.hasOption('v')) {
			Application.launch(Display.class, args);
		} else {
			String direction = line.getOptionValue("dir", "Across velocity - 200.0m");
			
			double inclination = Double.parseDouble(line.getOptionValue("inc", "0.0"));
			double height = Double.parseDouble(line.getOptionValue("height", "6.771e6"));
			double mass = Double.parseDouble(line.getOptionValue("mass", "1.0"));
			double cableWidth = Double.parseDouble(line.getOptionValue("width", "0.005"));
			double cableThickness = Double.parseDouble(line.getOptionValue("thickness", "0.0005"));
			double conductivity = Double.parseDouble(line.getOptionValue("cond", Double.toString(UnderlyingModels.σAluminium)));
			
			CableFunction chosenFunction = null;
			for(CableFunction func : CableFunction.CABLE_FUNCTIONS) {
				if(func.toString().equals(direction)) {
					chosenFunction = func;
				}
			}
			
			if(chosenFunction == null) {
				throw new ParseException("No function found: " + CableFunction.CABLE_FUNCTIONS.toString());
			}
			
			//TODO fill in drag values
			Satellite sat = new Satellite(chosenFunction, Emitter.createThermionicCathode(), mass, cableWidth, cableThickness, conductivity);

			List<State> states = runHeadlessSimulation(sat, new OrbitCharacteristics(0.0, height, inclination, 0.0, 0.0, 0.0), Duration.ofHours(6), Duration.ofMinutes(1),
					Duration.ofMillis(50));
			
			try(BufferedWriter writer = Files.newBufferedWriter(Paths.get("output.csv"), 
					StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
				writer.write("\"Timestep\",\"Velocity\",\"Lorrentz Force\",\"Height\",\"Current\",\"Field Strength\"\n");
				
				for(State state : states) {
					writer.write(String.format("\"%.3f\",\"%.3e\",\"%.3e\",\"%.3e\",\"%.3e\",\"%.3e\"\n", 
							state.time / 1e9,
							state.velocity.length(),
							state.lorentzForce.length(),
							state.position.length(),
							state.current,
							state.magneticField.length()));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static List<State> runHeadlessSimulation(Satellite satellite, OrbitCharacteristics c, Duration length, Duration sampleFrequency, Duration simFrequency) {
		OrbitalSimulation simulation = new OrbitalSimulation(c, satellite, simFrequency.toNanos());
		
		List<State> states = new ArrayList<>();
		
		long sampleNanos = sampleFrequency.toNanos();
		int lastSample = 0;
		
		long printFrequency = Duration.ofMinutes(30).toNanos();
		int lastPrint = 0;
		
		while(true) {
			if(lastSample < simulation.getState().time / sampleNanos) {
				lastSample = (int) (simulation.getState().time / sampleNanos);
				states.add(simulation.getState());
			}
			
			if(lastPrint < simulation.getState().time / printFrequency) {
				lastPrint = (int) (simulation.getState().time / printFrequency);
				System.out.println("Simulated: " + Duration.ofNanos(simulation.getState().time));
			}
			
			if(simulation.getState().time > length.toNanos()) {
				break;
			}
			
			if(simulation.getState().position.length() < 6.371e6) {
				System.out.println("Tether impacted Earth");
				break;
			}
			
			simulation.step();
		}
		
		return states;
	}
}
