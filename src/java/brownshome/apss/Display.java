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
		context = canvas.getGraphicsContext2D();
		
		new AnimationTimer() {
			@Override public void handle(long now) {
				animationLoop();
			}
		}.start();
		
		setOrbit();
	}
	
	private static final double SAT_SIZE = 1e5;
	private void animationLoop() {
		simulation.step(timePerFrame);
		time.setText(Duration.ofNanos(simulation.getCurrentTime()).withNanos(0).toString());
		
		context.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		context.fillText("Megnetic Field: " + simulation.getState().magneticField, 50, 50);
		
		if(simulation != null) {
			OrbitalSimulation.State state = simulation.getState();
			context.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
			context.scale(3e-5, -3e-5);
			context.setFill(Color.LIGHTSKYBLUE);
			context.fillOval(-UnderlyingModels.rE, -UnderlyingModels.rE, UnderlyingModels.rE * 2, UnderlyingModels.rE * 2);
			context.setFill(Color.BLACK);
			context.fillOval(state.position.x - SAT_SIZE / 2, state.position.y - SAT_SIZE / 2, SAT_SIZE, SAT_SIZE);
			context.setTransform(new Affine());
		}
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
			Satellite satellite = new Satellite(cableDirection.getValue(), m);

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
