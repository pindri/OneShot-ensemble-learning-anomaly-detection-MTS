package mapper;

import it.units.malelab.jgea.problem.symbolicregression.element.Element;

public enum Expression implements Element {

//    NOT("not"),
    PROP("proposition"),
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
