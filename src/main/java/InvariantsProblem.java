import it.units.malelab.jgea.problem.symbolicregression.element.Element;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.function.Function;

public class InvariantsProblem implements GrammarBasedProblem<String, Tree<Element>, Double > {

    private final Grammar<String> grammar;
    private final Function<Tree<String>, Tree<Element>> solutionMapper;
    private final Function<Tree<Element>, Double> fitnessFunction;

    public InvariantsProblem() {
        grammar = null;
        solutionMapper = null;
        fitnessFunction = null;
    }

    @Override
    public Grammar<String> getGrammar() {
        return grammar;
    }

    @Override
    public Function<Tree<String>, Tree<Element>> getSolutionMapper() {
        return solutionMapper;
    }

    @Override
    public Function<Tree<Element>, Double> getFitnessFunction() {
        return fitnessFunction;
    }
}
