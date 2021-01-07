//package stopcondition;
//
//import it.units.malelab.jgea.core.listener.Event;
//
//import java.util.List;
//import java.util.function.Predicate;
//
//public class ParetoTarget implements Predicate<Event<?, ?, List<Double>>> {
//
//    private final List<Double> paretoTarget;
//
//    public ParetoTarget(List<Double> paretoTarget) {
//        this.paretoTarget = paretoTarget;
//    }
//
//    @Override
//    public boolean test(Event<?, ?, List<Double>> event) {
//        return event.getOrderedPopulation().all()
//                    .stream().anyMatch(i -> (((i.getFitness().get(0) <= paretoTarget.get(0))
//                                               && (i.getFitness().get(1) <= 1.0/paretoTarget.get(1)))));
//    }
//
//    @Override
//    public String toString() {
//        return "ParetoTarget{" +
//                "paretoTarget=" + paretoTarget +
//                '}';
//    }
//}
