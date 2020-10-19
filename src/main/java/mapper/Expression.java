package mapper;

import it.units.malelab.jgea.problem.symbolicregression.element.Element;

public enum Expression implements Element {

    UNTIL("until"),
    PROP("proposition"),
    NOT("not"),
    AND("and");


    private final String expression;

    Expression(String expression) {
        this.expression = expression;
    }

    @Override
    public String toString() {
        return this.expression;
    }
}
