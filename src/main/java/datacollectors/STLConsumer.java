package datacollectors;

import core.fitness.AbstractFitnessFunction;
import it.units.malelab.jgea.core.consumer.NamedFunction;
import nodes.AbstractSTLNode;

public class STLConsumer {
    private STLConsumer() {
    }

    public static NamedFunction<AbstractSTLNode, Number> temporalLength() {
        return NamedFunction.build("temporalLength", "%3d", AbstractSTLNode::getMinLength);
    }

    public static NamedFunction<AbstractSTLNode, Number> coverage() {
        return NamedFunction.build("coverage", "%3f", AbstractSTLNode::getCoverage);
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> FPR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("FPR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("FPR"));
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> FNR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("FNR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("FNR"));
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> TPR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("TPR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("TPR"));
    }

}
