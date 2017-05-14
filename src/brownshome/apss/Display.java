package brownshome.apss;

import java.util.function.Function;

import javafx.application.Application;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Display extends Application {
	/** Satellite text fields */
	@FXML private TextField periapsis, apsis, trueAnomaly, inclination, argumentOfPeriapsis,
			longitudeOfAscendingNode, mass;
	
	@FXML private ChoiceBox<Function<OrbitalSimulation.State, Vec3>> cableDirection;
	
	
	@Override
	public void start(Stage stage) throws Exception {
		HBox root = FXMLLoader.load(getClass().getResource("GUI.fxml"));
		stage.setTitle("APSS Simulation");
		stage.setScene(new Scene(root));
		
		populateChoiceBox();
		
		stage.showAndWait();
	}


	private void populateChoiceBox() {
		cableDirection.getItems().addAll(Satellite.CABLE_FUNCTIONS);
	}
}
