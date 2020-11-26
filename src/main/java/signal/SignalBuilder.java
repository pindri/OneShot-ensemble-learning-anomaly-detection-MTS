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

public class SignalBuilder extends AbstractSignalBuilder<List<Signal<Record>>> {

    final private int traceLength;

    public SignalBuilder(int traceLength) {
        this.traceLength = traceLength;
    }

    @Override
    public List<Signal<Record>> build(String path, List<Integer> boolIndexes, List<Integer> numIndexes)
            throws IOException {
        List<Signal<Record>> signals = new ArrayList<>();
        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
        String[] header = buffReader.readLine().split(",");

        String line;
        double[] numValues;
        boolean[] boolValues = new boolean[boolIndexes.size()]; // Dummy variable.
        int time = 0;

        Signal<Record> trace = new Signal<>();

        if (this.traceLength > 0) {
            while ((line = buffReader.readLine()) != null) {
                List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
                numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
                        .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
                trace.add(time, new Record(boolValues, numValues));
                time++;

                if (time == this.traceLength) {
                    signals.add(trace);
                    trace = new Signal<>();
                    time = 0;
                }
            }
        } else {
            while ((line = buffReader.readLine()) != null) {
                List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
                numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
                        .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
                trace.add(time, new Record(boolValues, numValues));
                time++;
            }
            signals.add(trace);
        }

        buffReader.close();

        return signals;
    }

    @Override
    public List<Integer> parseLabels(String path) throws IOException {
        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
        buffReader.readLine(); // Header.
        List<Integer> result = new ArrayList<>();

        String line;
        int time = 0;
        int input = 0;

        while ((line = buffReader.readLine()) != null) {
            input += Integer.parseInt(line);
            time++;

            if (time >= this.traceLength) {
                result.add(input);
                time = 0;
                input = 0;
            }
        }

        buffReader.close();

        return result;
    }


}

