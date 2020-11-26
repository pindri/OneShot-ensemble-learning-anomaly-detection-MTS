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

public class SignalBuilderDense extends AbstractSignalBuilder<List<Signal<Record>>> {

    final private int traceLength;

    public SignalBuilderDense(int traceLength) {
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
        boolean[] boolValues = new boolean[boolIndexes.size()]; // Dummy variable, no booleans used.

        List<Record> records = new ArrayList<>();

        // Read records.
        while ((line = buffReader.readLine()) != null) {
            List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
            numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
                    .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
            Record rec = new Record(boolValues, numValues);
            records.add(rec);
        }

        Signal<Record> trace = new Signal<>();
        int time = 0;

        // Extract overlapping sequences of length traceLength.
        for (int i = 0; i < records.size()-this.traceLength; i++) {
            for (int j = i; j < i+this.traceLength; j++) {
                trace.add(time, records.get(j));
                time++;
            }
            signals.add(trace);
            trace = new Signal<>();
            time = 0;
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

        List<Integer> labels = new ArrayList<>();

        // Read labels.
        while ((line = buffReader.readLine()) != null) {
            labels.add(Integer.parseInt(line));
        }

        // Extract overlapping sequences of length traceLength.
        int input = 0;
        for (int i = 0; i < labels.size()-this.traceLength; i++) {
            for (int j = i; j < i+traceLength; j++) {
                input += labels.get(j);
            }
            result.add(input);
            input = 0;
        }

        buffReader.close();

        return result;
    }


//    public Signal<Record> buildNoTrace(String path, List<Integer> boolIndexes, List<Integer> numIndexes)
//            throws IOException {
//        Signal<Record> signal = new Signal<>();
//        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
//        String[] header = buffReader.readLine().split(",");
//
//        String line;
//        double[] numValues;
//        boolean[] boolValues = new boolean[boolIndexes.size()]; // Dummy variable.
//        int time = 0;
//
//        while ((line = buffReader.readLine()) != null) {
//            List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
//            numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
//                    .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
//            signal.add(time, new Record(boolValues, numValues));
//            time++;
//        }
//
//        buffReader.close();
//
//        return signal;
//    }

}
