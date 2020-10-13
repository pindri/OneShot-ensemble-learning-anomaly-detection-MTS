import eu.quanticol.moonlight.signal.Signal;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class SignalBuilder {
    // TODO: abstraction if more signal builders are necessary.
    // TODO: use boolean variables as well.
    // TODO: would it be useful to keep timestamps?
    // TODO: info on temporal and variable bounds?

    public static final String[] numNames = new String[]{"LIT101","AIT201","AIT202","AIT203"};
    public static final String[] boolNames = new String[]{};

    public Signal<Record> build(String path, List<Integer> boolIndexes, List<Integer> doubleIndexes) throws IOException {
        Signal<Record> signal = new Signal<>();
        try {
            BufferedReader buffReader = Files.newBufferedReader(Paths.get(path));
            String[] header = buffReader.readLine().split(",");

            String line;
            double[] numValues;
            boolean[] boolValues = new boolean[0]; // Dummy variable.
            int time = 0;

            while ((line = buffReader.readLine()) != null) {
                numValues = Arrays.stream(line.split(",")).mapToDouble(Double::parseDouble).toArray();
                signal.add(time, new Record(boolValues, numValues));
                time++;
            }

            buffReader.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return signal;
    }


}
