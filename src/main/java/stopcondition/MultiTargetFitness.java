//package stopcondition;
//
//import it.units.malelab.jgea.core.listener.Event;
//
//import java.util.function.Predicate;
//
//public class MultiTargetFitness<F> implements Predicate<Event<?, ?, F>> {
//
//    private final F targetFitness;
//    private final int targetNumber;
//
//    public MultiTargetFitness(F targetFitness, int targetNumber) {
//        this.targetFitness = targetFitness;
//        this.targetNumber = targetNumber;
//    }
//
//    @Override
//    public boolean test(Event<?, ?, F> event) {
//        return event.getOrderedPopulation().all().stream()
//                    .filter(i -> targetFitness.equals(i.getFitness())).count() >= targetNumber;
//    }
//
//    @Override
//    public String toString() {
//        return "TargetFitness{" +
//                "targetFitness=" + targetFitness +
//                '}';
//    }
//}
