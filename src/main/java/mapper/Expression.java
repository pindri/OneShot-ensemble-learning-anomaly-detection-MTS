package mapper;

import it.units.malelab.jgea.problem.symbolicregression.element.Element;

public enum Expression implements Element {

    ONCE("once"),
    HISTORICALLY("historically"),
    EVENTUALLY("eventually"),
    GLOBALLY("globally"),

    SINCE("since"),
    UNTIL("until"),

    PROP("proposition"),
    NOT("not"),
    AND("and"),
    OR("or"),
    IMPLIES("implies");

    private final String expression;

    Expression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
