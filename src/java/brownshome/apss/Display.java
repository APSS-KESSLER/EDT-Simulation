package brownshome.apss;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.stage.Stage;

public class Display extends Application {
	/** Satellite text fields */
	@FXML private TextField eccentricity, semiMajorAxis, trueAnomaly, inclination, argumentOfPeriapsis,
			longitudeOfAscendingNode, mass, setBias, currentLimit, powerLimit, cableLength;

	@FXML private ChoiceBox<String> cableDirection;
	
	@FXML private TextField timeStep, time, customSpeed;
	
	@FXML private Canvas canvas;
	
	private OrbitalSimulation simulation;
	private Duration timePerFrame = Duration.ZERO;
	private GraphicsContext context;
	
	@Override public void start(Stage stage) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource("GUI.fxml"));
		loader.setController(this);
		HBox root = loader.load();
		stage.setTitle("APSS Simulation");
		stage.setScene(new Scene(root));
		stage.show();
		
		setup();
	}

	private void setup() {
		cableDirection.getItems().addAll(Arrays.asList(
				"Down", "Across", "Across Spin"
		));

		cableDirection.getSelectionModel().select(0);
		context = canvas.getGraphicsContext2D();
		
		new AnimationTimer() {
			@Override public void handle(long now) {
				animationLoop();
			}
		}.start();
		
		setOrbit();
	}
	
	private static final double SAT_SIZE = 2.5e5;
	private void animationLoop() {
		if(simulation != null) {
			simulation.step(timePerFrame);
			time.setText(Duration.ofNanos(simulation.getCurrentTime()).withNanos(0).toString());
			
			context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			OrbitalSimulation.State state = simulation.getState();
			context.translate(canvas.getWidth() * 4 / 7, canvas.getHeight() / 2);
			//context.strokeLine(0, 0, 100, 100);
			
			context.scale(4e-5, -4e-5);
			
			context.setStroke(Color.BLUE);
			for(double x = -9e6; x < 9e6; x += 4e5) {
				for(double y = -9e6; y < 9e6; y += 4e5) {
					Vec3 v = new Vec3(x, y, state.position.z);
					
					if(v.lengthSquared() < UnderlyingModels.rE * UnderlyingModels.rE * 0.5)
						continue;
					
					Vec3 s = UnderlyingModels.getMagneticFieldStrength(v);
					drawVector(s.scale(6e9), v);
				}
			}
			
			context.setFill(Color.LIGHTSKYBLUE);
			context.fillOval(-UnderlyingModels.rE, -UnderlyingModels.rE, UnderlyingModels.rE * 2, UnderlyingModels.rE * 2);
			
			context.setStroke(Color.RED);
			drawVector(state.cableVector.withLength(1e6), state.position);
			
			context.setFill(Color.BLACK);
			context.fillOval(state.position.x - SAT_SIZE / 2, state.position.y - SAT_SIZE / 2, SAT_SIZE, SAT_SIZE);
			
			context.setLineWidth(2e4);
			
			context.setTransform(new Affine());

			Vec3 torqueDirection = simulation.getState().velocity.cross(simulation.getState().cableVector).withLength(1.0);

			List<String> items = new ArrayList<>(), names = new ArrayList<>();

			Vec3 unitVelocity = state.velocity.withLength(1.0);

			names.add("Height");
			items.add(formatDouble(state.position.length() - UnderlyingModels.rE, "m"));

			names.add("Velocity");
			items.add(formatDouble(state.velocity.length(), "m/s"));

			names.add("Lorentz Force");
			items.add(formatDouble(state.lorentzForce.length(), "N"));

			names.add("Field Strength");
			items.add(formatDouble(state.magneticField.length(), "T"));

			names.add("Electron Density");
			items.add(formatDouble(state.plasmaDensity, "e/m3"));

			names.add("Voltage Gradient");
			items.add(formatDouble(state.magneticField.cross(state.velocity).dot(state.cableVector.withLength(1.0)), "V/m"));

			names.add("Bias");
			items.add(formatDouble(state.emitterResult.chosenBias, "V"));

			names.add("Current");
			items.add(formatDouble(state.current, "A"));

			names.add("Power Usage");
			items.add(formatDouble(state.emitterResult.powerUsage, "W"));

			names.add("Power Extracted");
			items.add(formatDouble(-state.lorentzForce.dot(state.velocity), "W"));

			displayTextItems(names, items);
		}
	}

	private static final String[] prefix = { "T", "G", "M", "k", "", "m", "μ", "n", "p" };
	private static final int onesIndex = 4;
	private String formatDouble(double number, String unit) {
		int index = onesIndex;

		do {
			if(Math.abs(number) < 1.0) {
				index++;
				number *= 1000.0;
			} else if(Math.abs(number) > 1000.0) {
				index--;
				number /= 1000.0;
			} else {
				break;
			}
		} while(index > 0 && index < prefix.length - 1);

		return String.format("%.3f %s%s", number, prefix[index], unit);
	}

	private void displayTextItems(List<String> names, List<String> items) {
		context.clearRect(0, 0, 200, items.size() * 20 + 50);

		for(int i = 0; i < names.size(); i++) {
			context.fillText(names.get(i) + ": " + items.get(i), 10, 40 + i * 20);
		}
	}
	
	private void drawVector(Vec3 v, Vec3 p) {
		Vec3 end = p.add(v);
		context.fillOval(p.x - 3e4, p.y - 3e4, 6e4, 6e4);
		context.strokeLine(p.x, p.y, end.x, end.y);
	}

	@FXML private void setOrbit() {
		try {
			double e, a, v, ω, Ω, i;
		
			e = Double.parseDouble(eccentricity.getText());
			a = Double.parseDouble(semiMajorAxis.getText());
			v = Double.parseDouble(trueAnomaly.getText());
			i = Double.parseDouble(inclination.getText());
			ω = Double.parseDouble(argumentOfPeriapsis.getText());
			Ω = Double.parseDouble(longitudeOfAscendingNode.getText());

			OrbitCharacteristics orbit = new OrbitCharacteristics(e, a, i, ω, v, Ω);

			double bias, powerLimitNo, currentLimitNo;

			bias = setBias.getText().isEmpty() ? Double.NaN : Double.parseDouble(setBias.getText());
			powerLimitNo = powerLimit.getText().isEmpty() ? Double.NaN : Double.parseDouble(powerLimit.getText());
			currentLimitNo = currentLimit.getText().isEmpty() ? Double.NaN : Double.parseDouble(currentLimit.getText());

			if(!Double.isFinite(bias) && !Double.isFinite(powerLimitNo) && !Double.isFinite(currentLimitNo)) {
				displayError("At least one of the emitter limits or bias must be defined");
				return;
			}

			Emitter emitter = Emitter.createThermionicCathode(powerLimitNo, currentLimitNo, bias);

			double m = Double.parseDouble(mass.getText());

			CableFunction func;

			switch(cableDirection.getValue()) {
				case "Down":
					func = CableFunction.towardsGravity(Double.parseDouble(cableLength.getText()));
					break;
				case "Across":
					func = CableFunction.acrossVelocity(Double.parseDouble(cableLength.getText()));
					break;
				case "Across Spin":
					func = CableFunction.acrossVelocitySpin(Double.parseDouble(cableLength.getText()));
					break;
				default:
					throw new IllegalArgumentException(cableDirection.getValue() + " is not a valid direction.");
			}

			Satellite satellite = new Satellite(func, emitter, m, UnderlyingModels.σAluminium);

			simulation = new OrbitalSimulation(orbit, satellite, simulation == null ? getTimeStep() : simulation.getTimeStep());
		} catch(IllegalArgumentException nfe) {
			displayError("Error parsing data");
			nfe.printStackTrace();
			return;
		}
		
		x10speed();
	}

	private long getTimeStep() {
		try {
			return Duration.parse(timeStep.getText()).toNanos();
		} catch(DateTimeParseException dtpe) {
			displayError("Invalid timestep");
			return simulation.getTimeStep();
		}
	}


	private void displayError(String string) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setContentText(string);
		alert.show();
	}

	@FXML private void pause() { timePerFrame = Duration.ZERO; }
	@FXML private void x10speed() { timePerFrame = Duration.ofSeconds(10).dividedBy(60); }
	@FXML private void x60speed() { timePerFrame = Duration.ofSeconds(60).dividedBy(60); }
	@FXML private void x500speed() { timePerFrame = Duration.ofSeconds(500).dividedBy(60); }
	@FXML private void x3000speed() { timePerFrame = Duration.ofSeconds(3000).dividedBy(60); }
	
	@FXML private void setCustomSpeed() {
		try {
			timePerFrame = Duration.ofNanos((long) (Double.parseDouble(customSpeed.getText()) * 1e9 / 60));
		} catch(NumberFormatException nfe) {
			displayError("Error parsing rate");
		}
	}
	
	@FXML private void setTimestep() {
		if(simulation != null)
			simulation.setTimeStep(getTimeStep());
	}
}
