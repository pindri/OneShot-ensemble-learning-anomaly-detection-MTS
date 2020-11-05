package signal;

import eu.quanticol.moonlight.signal.Signal;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SignalBuilder {
    // TODO: abstraction if more signal builders are necessary.
    // TODO: maybe better exception handling?

    public List<Signal<Record>> build(String path, List<Integer> boolIndexes,
                                      List<Integer> numIndexes, int traceLength) throws IOException {
        List<Signal<Record>> signals = new ArrayList<>();
        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
        String[] header = buffReader.readLine().split(",");

        String line;
        double[] numValues;
        boolean[] boolValues = new boolean[boolIndexes.size()]; // Dummy variable.
        int time = 0;

        Signal<Record> trace = new Signal<>();
        while ((line = buffReader.readLine()) != null) {
            List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
            numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
                    .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
            trace.add(time, new Record(boolValues, numValues));
            time++;

            if (time == traceLength) {
                signals.add(trace);
                trace = new Signal<>();
                time = 0;
            }
        }

        buffReader.close();

        return signals;
    }


    public List<Integer> parseLabels(String path, int traceLength) throws IOException {
        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
        buffReader.readLine(); // Header.
        List<Integer> result = new ArrayList<>();

        String line;
        int time = 0;
        int input = 0;

        while ((line = buffReader.readLine()) != null) {
            input += Integer.parseInt(line);
            time++;

            if (time == traceLength) {
                result.add(input);
                time = 0;
                input = 0;
            }
        }

        buffReader.close();

        return result;
    }

}
