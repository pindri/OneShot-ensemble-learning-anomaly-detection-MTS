package signal;

import eu.quanticol.moonlight.signal.Signal;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
//                        .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
                        .mapToDouble(i -> roundDouble(Double.parseDouble(input.get(i)), 2)).toArray();
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
//                        .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();
                        .mapToDouble(i -> roundDouble(Double.parseDouble(input.get(i)), 2)).toArray();
                trace.add(time, new Record(boolValues, numValues));
                time++;
            }
            signals.add(trace);
        }

        buffReader.close();

        return signals;
    }

    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public List<Signal<Record>> extractPortion(List<Signal<Record>> signals, double from, double to) {
        // If more than one signal (traceLength > 0), extract portion of signals.
        if (traceLength > 0) {
            int fromIndex = (int) (from * signals.size());
            int toIndex = (int) (to * signals.size());

            return signals.subList(fromIndex, toIndex);

        } else { // If one signal, extract portion of record.

            Signal<Record> signal = signals.get(0);
            List<Signal<Record>> resultList = new ArrayList<>();
            Signal<Record> resultSignal = new Signal<>();
            int range = (int) signal.end() - (int) signal.start() + 1;
            int fromIndex = (int) (signal.start() + (range * from));
            int toIndex = (int) (signal.start() + (range * to));

            double[] timePoints = IntStream.range(fromIndex, toIndex).asDoubleStream().toArray();
            List<Record> records = signal.getTimePoints(timePoints);

            int time = 0;

            for (Record record: records) {
                resultSignal.add(time, record);
                time++;
            }

            resultList.add(resultSignal);

            return resultList;
        }
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

