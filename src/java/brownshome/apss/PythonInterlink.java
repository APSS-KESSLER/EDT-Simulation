package brownshome.apss;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PythonInterlink {
    private static final String SCRIPT_NAME = "springModel4.py";

    private final PrintWriter toPython;
    private final BufferedReader fromPython;

    private final Process process;
    private final OrbitalSimulation simulation;

    public PythonInterlink(OrbitalSimulation simulation, int simulationDensity) {
        this.simulation = simulation;

        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> arguments = new ArrayList<>();

        arguments.add("python3");
        arguments.add(SCRIPT_NAME);
        processBuilder.command(arguments);

        processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);

        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start python", e);
        }

        toPython = new PrintWriter(process.getOutputStream());
        fromPython = new BufferedReader(new InputStreamReader(process.getInputStream()));

        sendInitializationMessage();
    }

    private void sendInitializationMessage() {

    }

    public void simulate(int timestep, List<Vec3> forces) {
        toPython.println(timestep);

        for()

        try {
            fromPython.readLine();
        } catch (IOException e) {
            throw new IllegalStateException("Communication with the python simulation failed: " + e.getMessage());
        }
    }
}
