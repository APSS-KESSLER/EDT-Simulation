package brownshome.apss;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

public class ExternalInterlink {
	private final Process process;

	// IO streams from process
	private final PrintWriter toProcess;
	private final BufferedReader fromProcess;

	public ExternalInterlink(List<String> startupCall) {
		ProcessBuilder processBuilder = new ProcessBuilder();

		processBuilder.command(startupCall);

		// redirect errors to the console
		processBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);

		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new IllegalStateException("Unable to start external program '" + startupCall + "'", e);
		}

		toProcess = new PrintWriter(process.getOutputStream());
		fromProcess = new BufferedReader(new InputStreamReader(process.getInputStream()));
	}

	public void send(int i) {
		toProcess.println(i);
	}

	/** Prints the length of the array, and then the items separated by a space. */
	public void send(int[] values) {
		toProcess.print(values.length);

		for(int x : values) {
			toProcess.print(" ");
			toProcess.print(x);
		}

		toProcess.println();
	}

	public String readString() throws IOException {
		// We flush to ensure that the program has received all commands before we wait for input
		toProcess.flush();

		return fromProcess.readLine();
	}

	public int readInteger() throws IOException {
		return Integer.parseInt(readString());
	}

	public int[] readIntegerArray() throws IOException {
		String line = readString();
		String[] splitLine = line.split(" ");
		int[] result = new int[splitLine.length];

		for(int i = 0; i < result.length; i++) {
			result[i] = Integer.parseInt(splitLine[i]);
		}

		return result;
	}

	public double readDouble() throws IOException {
		return Double.parseDouble(readString());
	}
}