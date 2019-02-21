package brownshome.apss;

import java.time.Duration;
import java.time.format.DateTimeParseException;

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
			longitudeOfAscendingNode, mass;
	
	@FXML private ChoiceBox<CableFunction> cableDirection;
	
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
		cableDirection.getItems().addAll(CableFunction.CABLE_FUNCTIONS);
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
			context.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
			//context.strokeLine(0, 0, 100, 100);
			
			context.scale(3e-5, -3e-5);
			
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

			// We project the torque onto the position vector to gain a +/- value for the magnitude of the torque.
			Vec3 torqueDirection = simulation.getState().position.withLength(1.0);
			
			context.clearRect(0, 40, 200, 200);
			context.fillText("Magnetic Field: " + (int) (simulation.getState().magneticField.length() * 1e6) + "uT", 50, 50);
			context.fillText("Velocity: " + (int) (simulation.getState().velocity.length()) + "m/s", 50, 70);
			context.fillText("Distance: " + (int) simulation.getState().position.length() + "m", 50, 90);
			context.fillText("Lorentz Force: " + String.format("%.3f", simulation.getState().
					lorentzForce.dot(simulation.getState().velocity.withLength(1.0)) * 1e6) + "uN", 50, 110);
			context.fillText("Lorentz Torque: " + String.format("%.3f", simulation.getState().
					lorentzTorque.dot(torqueDirection) * 1e3) + "mNm", 50, 130);
			context.fillText("Current: " + String.format("%.3fmA", simulation.getState().current * 1e3), 50, 150);
			context.fillText("Drag Force: " + String.format("%.3f", simulation.getState().
					dragForce.dot(simulation.getState().velocity.withLength(1.0)) * 1e6) + "uN", 50, 170);
			context.fillText("Drag Torque: " + String.format("%.3f", simulation.getState().
					dragTorque.dot(torqueDirection) * 1e3) + "mNm", 50, 190);
			context.fillText("Net Torque: " + String.format("%.3f", simulation.getState().
					netTorque.dot(torqueDirection) * 1e3) + "mNm", 50, 210);
			context.fillText("Average Torque: " + String.format("%.3f", simulation.getState().
					netTorqueAverage * 1e3) + "mNm", 50, 230);
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

			double m = Double.parseDouble(mass.getText());
			Satellite satellite = new Satellite(cableDirection.getValue(), 0, m, UnderlyingModels.σAluminium);

			simulation = new OrbitalSimulation(orbit, satellite, simulation == null ? getTimeStep() : simulation.getTimeStep());
		} catch(NumberFormatException nfe) {
			displayError("Error parsing data");
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
