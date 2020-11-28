package signal;

import eu.quanticol.moonlight.signal.Signal;

import java.util.stream.IntStream;

public class SignalHandler {

    public static double[][] toDoubleArray(Signal<Double> signal) {
        double[] range = IntStream.rangeClosed((int) signal.start(), (int) signal.end()).asDoubleStream().toArray();
        // signalArray: double[][time, value]
        return signal.arrayOf(range, Double::valueOf);
    }
}
