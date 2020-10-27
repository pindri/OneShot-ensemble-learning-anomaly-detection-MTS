package signal;

import eu.quanticol.moonlight.signal.Signal;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SignalBuilder {
    // TODO: abstraction if more signal builders are necessary.
    // TODO: maybe better exception handling?

    public Signal<Record> build(String path, List<Integer> boolIndexes, List<Integer> numIndexes) throws IOException {
        Signal<Record> signal = new Signal<>();
        BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
        String[] header = buffReader.readLine().split(",");

        String line;
        double[] numValues;
        boolean[] boolValues = new boolean[boolIndexes.size()]; // Dummy variable.
        int time = 0;

        while ((line = buffReader.readLine()) != null) {
            List<String> input = Arrays.stream(line.split(",")).collect(Collectors.toList());
            numValues = IntStream.range(0, header.length).filter(numIndexes::contains)
                        .mapToDouble(i -> Double.parseDouble(input.get(i))).toArray();

            signal.add(time, new Record(boolValues, numValues));
            time++;
        }

        buffReader.close();

        return signal;
    }


}
