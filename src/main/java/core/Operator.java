package core;

public enum Operator {

    OR("OR"),
    AND("AND"),
    MAJORITY("MAJORITY");

    private final String operator;

    Operator(String operator) {this.operator = operator;}

    @Override
    public String toString() {
        return this.operator;
    }
}
