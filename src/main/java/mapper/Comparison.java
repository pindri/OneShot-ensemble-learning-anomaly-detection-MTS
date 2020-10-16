package mapper;

import java.util.function.BiFunction;

public enum Comparison {

    SMALLER("<", (Double a, Double b) -> b - a),
    GREATER(">", (Double a, Double b) -> a - b),
    EQUAL("==", (Double a, Double b) -> - Math.abs(a - b));

    private final String symbol;
    private final BiFunction<Double, Double, Double> function;

    Comparison(String comparisonSymbol, BiFunction<Double, Double, Double> comparisonFunction) {
        this.symbol = comparisonSymbol;
        this.function = comparisonFunction;
    }


    @Override
    public String toString() {
        return this.symbol;
    }

    public BiFunction<Double, Double, Double> getValue() {
        return this.function;
    }
}
