package core.fitness;

public enum Operator {

    OR("OR"),
    AND("AND"),
    TWO("TWO"),
    MAJORITY("MAJORITY");


    private final String operator;

    Operator(String operator) {this.operator = operator;}

    @Override
    public String toString() {
        return this.operator;
    }
}
