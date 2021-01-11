package evolver;

import com.google.common.base.Stopwatch;
import it.units.malelab.jgea.core.Factory;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.consumer.Consumer;
import it.units.malelab.jgea.core.consumer.Event;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.speciation.Speciator;
import it.units.malelab.jgea.core.evolver.speciation.Species;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.DAGPartiallyOrderedCollection;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import it.units.malelab.jgea.core.selector.Selector;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import nodes.AbstractSTLNode;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpecEvo<G, S extends AbstractSTLNode> extends StandardEvolver<G, S, Double> {

    private Speciator<Individual<G, S, Double>> speciator;
    private static final Logger L = Logger.getLogger(SpecEvo.class.getName());
    int maxAttempts = 100;
    List<String> variableList;
    double epsilon = 0.0;
    int minSpeciesSizeForElitism = 10;
    double rankBase = 0.75;

    public SpecEvo(
            Function<? super G, ? extends S> solutionMapper,
            Factory<? extends G> genotypeFactory,
            PartialComparator<? super Individual<G, S, Double>> individualComparator,
            int populationSize,
            Map<GeneticOperator<G>, Double> operators,
            Selector<? super Individual<? super G, ? super S, ? super Double>> parentSelector,
            Speciator<Individual<G, S, Double>> speciator) {
        super(solutionMapper, genotypeFactory, individualComparator, populationSize, operators, parentSelector,
              new Worst(), populationSize, false);
        this.speciator = speciator;
        this.variableList = List.of("FIT101", "LIT101", "MV101", "P101", "P102", "AIT201", "AIT202", "AIT203",
                                    "FIT201", "MV201", "P202", "P203", "P204", "P205", "P206", "DPIT301", "FIT301",
                                    "LIT301", "MV301", "MV302", "MV303", "MV304", "P301", "P302", "AIT401",
                                    "AIT402", "FIT401", "LIT401", "P401", "P402", "P403", "P404", "UV401", "AIT501",
                                    "AIT502", "AIT503", "AIT504", "FIT501", "FIT502", "FIT503", "FIT504", "P501",
                                    "P502", "PIT501", "PIT502", "PIT503", "FIT601", "P601", "P602", "P603");
    }

    @Override
    public Collection<S> solve(Function<S, Double> fitnessFunction, Predicate<? super Event<G, S, Double>> stopCondition, Random random, ExecutorService executor, Consumer<? super G, ? super S, ? super Double, ?> consumer) throws InterruptedException, ExecutionException {
        System.out.println("SOLVING! Epsilon: " + epsilon);
        State state = initState();
        List<String> solutionCounter = new ArrayList<>();
        Collection<Individual<G, S, Double>> solutions = new ArrayList<>();
        Stopwatch stopwatch = Stopwatch.createStarted();
        Collection<Individual<G, S, Double>> population = initPopulation(fitnessFunction, random, executor, state);
        L.fine(String.format("Population initialized: %d individuals", population.size()));
        while (true) {
            PartiallyOrderedCollection<Individual<G, S, Double>> orderedPopulation = new DAGPartiallyOrderedCollection<>(population, individualComparator);
            state.setElapsedMillis(stopwatch.elapsed(TimeUnit.MILLISECONDS));
            Event<G, S, Double> event = new Event<>(state, orderedPopulation);
            consumer.consume(event);
//            if (stopCondition.test(event)) {
//                L.fine(String.format("Stop condition met: %s", stopCondition.toString()));
//                break;
//            }
            if (event.getOrderedPopulation().all().stream().filter(i -> i.getFitness() <= epsilon).count() >= 1) {
                List<String> solvedVars = population.stream()
                                               .filter(i -> i.getFitness() <= epsilon)
                                               .flatMap(i -> i.getSolution().getVariablesList().stream()).distinct()
                                               .collect(Collectors.toList());

                System.out.println(solvedVars);

                List<Individual<G, S, Double>> toRemove = population.stream()
                                                               .filter(i -> i.getSolution().getVariablesList()
                                                                             .stream().anyMatch(solvedVars::contains))
                                                               .collect(Collectors.toList());

                // Add solutions (fitness <= epsilon) to solution list.
//                solutions.addAll(population.stream()
//                                           .filter(i -> i.getFitness() <= epsilon)
//                                           .collect(Collectors.toList()));
                solutions.addAll(toRemove);

               // Removing solutions from population and solved variables from speciation.
                System.out.println("Population size before: " + population.size());
                for (Individual<G, S, Double> sol : toRemove) {
                    population = population.stream().filter(ind -> !ind.equals(sol)).collect(Collectors.toList());
                }
                System.out.println("Population size after: " + population.size());

                // Create new individuals.
                if (population.size() == 0) {
                    Collection<Individual<G, S, Double>> newPop = this.initPopulation(toRemove.size(), fitnessFunction,
                                                                                 random, executor, state);
                    population.addAll(newPop);
                }

                System.out.println("Removed " + toRemove.size() + " solutions.");


//                for (String v : solvedVars) {
//                    System.out.println("Removing: " + v);
//                    this.variableList = variableList.stream().filter(s -> !s.equals(v)).collect(Collectors.toList());
//                }


                solutionCounter.addAll(solvedVars);

                System.out.println("Solved " + solutionCounter.stream().distinct().count() + " variables.");
                System.out.println(solutions.size() + " total solutions.");

                orderedPopulation = new DAGPartiallyOrderedCollection<>(population, individualComparator);
            }
            if (solutionCounter.stream().distinct().count() >= 15) {
                break;
            }
            population = updatePopulation(orderedPopulation, fitnessFunction, random, executor, state);
            L.fine(String.format("Population updated: %d individuals", population.size()));
            state.incIterations(1);
        }
//        return new DAGPartiallyOrderedCollection<>(solutions, individualComparator).firsts().stream()
//                                                                                    .map(Individual::getSolution)
//                                                                                    .collect(Collectors.toList());
        return solutions.stream().map(Individual::getSolution).collect(Collectors.toList());
    }

//    @Override
//    protected Collection<Individual<G, S, Double>> buildOffspring(
//            PartiallyOrderedCollection<Individual<G, S, Double>> orderedPopulation,
//            Function<S, Double> fitnessFunction,
//            Random random,
//            ExecutorService executor,
//            State state) throws ExecutionException, InterruptedException {
//
//        Collection<Individual<G, S, Double>> offspring = new ArrayList<>();
//        speciator = new MySpeciator<>(this.variableList);
//
//        // Partition in species.
//        List<Species<Individual<G, S, Double>>> allSpecies = new ArrayList<>(speciator.speciate(orderedPopulation));
//        System.out.printf("Population speciated in %d species of sizes %s%n",
//                          allSpecies.size(),
//                          allSpecies.stream().map(s -> s.getElements().size()).collect(Collectors.toList())
//                         );
//
////        // Find species with solutions and remove.
////        List<String> solvedVars = orderedPopulation.all().stream()
////                                                   .filter(i -> i.getFitness().equals(0.0))
////                                                   .flatMap(i -> i.getSolution().getVariablesList().stream()).distinct()
////                                                   .collect(Collectors.toList());
////        System.out.println(solvedVars);
////
////        List<Individual<G, S, Double>> toRemove =
////                orderedPopulation.all().stream()
////                                 .filter(i -> i.getSolution().getVariablesList()
////                                               .stream().anyMatch(solvedVars::contains))
////                                 .collect(Collectors.toList());
////
////        System.out.println("Trimmed before: " + orderedPopulation.size());
////        for (Individual<G, S, Double> individual : toRemove) {
////            orderedPopulation.remove(individual);
////        }
////        System.out.println("Trimmed after: " + orderedPopulation.size());
//
//        // Elitism for each species.
//        for (Species<Individual<G, S, Double>> species : allSpecies) {
//            List<Individual<G, S, Double>> sortedSpecies = new ArrayList<>(species.getElements());
//            sortedSpecies.sort(individualComparator.comparator());
//            // One elite.
//            offspring.add(sortedSpecies.get(0));
////            offspring.add(sortedSpecies.get(1));
//        }
//
////        System.out.println("List with duplicates: " + offspring.size());
////        offspring = offspring.stream().distinct().collect(Collectors.toList());
////        System.out.println("List without duplicates: " + offspring.size());
//
//        // Remaining offsprings.
//        int remaining = populationSize - offspring.size();
//        List<G> offspringGenotypes = new ArrayList<>(remaining);
//
//        // Enfoced diversity.
//        Collection<G> existingGenotypes = orderedPopulation.all().stream().map(Individual::getGenotype)
//                                                           .collect(Collectors.toList());
//        while (offspringGenotypes.size() < remaining) {
//            GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
//            List<G> parentGenotypes = new ArrayList<>(operator.arity());
//            int attempts = 0;
//            while (true) {
//                parentGenotypes.clear();
//                for (int j = 0; j < operator.arity(); j++) {
//                    Individual<G, S, Double> parent = parentSelector.select(orderedPopulation, random);
//                    parentGenotypes.add(parent.getGenotype());
//                }
//                List<G> childGenotypes = new ArrayList<>(operator.apply(parentGenotypes, random));
//                boolean added = false;
//                for (G childGenotype : childGenotypes) {
//                    if ((!offspringGenotypes.contains(childGenotype) && !existingGenotypes.contains(childGenotype)) ||
//                            (attempts >= maxAttempts - 1)) {
//                        added = true;
//                        offspringGenotypes.add(childGenotype);
//                    }
//                }
//                if (added) {
//                    break;
//                }
//                attempts = attempts + 1;
//            }
//        }
//
////        while (offspringGenotypes.size() < remaining) {
////            GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
////            List<G> parentGenotypes = new ArrayList<>(operator.arity());
////            for (int j = 0; j < operator.arity(); ++j) {
////                Individual<G, S, Double> parent = this.parentSelector.select(orderedPopulation, random);
////                parentGenotypes.add(parent.getGenotype());
////            }
////            offspringGenotypes.addAll(operator.apply(parentGenotypes, random));
////        }
//
//        // Merge.
//        offspring.addAll(buildIndividuals(offspringGenotypes, solutionMapper, fitnessFunction, executor, state));
//        return offspring;
//    }


    @Override
    protected Collection<Individual<G, S, Double>> buildOffspring(
            PartiallyOrderedCollection<Individual<G, S, Double>> orderedPopulation,
            Function<S, Double> fitnessFunction,
            Random random,
            ExecutorService executor,
            State state) throws ExecutionException, InterruptedException {
        Collection<Individual<G, S, Double>> parents = orderedPopulation.all();
        Collection<Individual<G, S, Double>> offspring = new ArrayList<>();
        //partition in species
        List<Species<Individual<G, S, Double>>> allSpecies = new ArrayList<>(speciator.speciate(orderedPopulation));
        System.out.printf("Population speciated in %d species of sizes %s%n",
                          allSpecies.size(),
                          allSpecies.stream().map(s -> s.getElements().size()).collect(Collectors.toList())
                         );
        //put elites
        Individual<G, S, Double> best = parents.stream()
                                          .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
                                          .get();
        offspring.add(best);
        for (Species<Individual<G, S, Double>> species : allSpecies) {
            if (species.getElements().size() >= minSpeciesSizeForElitism) {
                Individual<G, S, Double> speciesBest = species.getElements().stream()
                                                         .reduce((i1, i2) -> individualComparator.compare(i1, i2).equals(PartialComparator.PartialComparatorOutcome.BEFORE) ? i1 : i2)
                                                         .get();
                offspring.add(speciesBest);
            }
        }
        //assign remaining offspring size
        int remaining = populationSize - offspring.size();
        List<Individual<G, S, Double>> representers = allSpecies.stream()
                                                           .map(Species::getRepresentative)
                                                           .collect(Collectors.toList());
        L.fine(String.format("Representers determined for %d species: fitnesses are %s",
                             allSpecies.size(),
                             representers.stream()
                                         .map(i -> String.format("%s", i.getFitness()))
                                         .collect(Collectors.toList())
                            ));
        List<Individual<G, S, Double>> sortedRepresenters = new ArrayList<>(representers);
        sortedRepresenters.sort(individualComparator.comparator());
        List<Double> weights = representers.stream()
                                           .map(r -> Math.pow(rankBase, sortedRepresenters.indexOf(r)))
                                           .collect(Collectors.toList());
        double weightSum = weights.stream()
                                  .mapToDouble(Double::doubleValue)
                                  .sum();
        List<Integer> sizes = weights.stream()
                                     .map(w -> (int) Math.floor(w / weightSum * (double) remaining))
                                     .collect(Collectors.toList());
        int sizeSum = sizes.stream()
                           .mapToInt(Integer::intValue)
                           .sum();
        sizes.set(0, sizes.get(0) + remaining - sizeSum);
        L.fine(String.format("Offspring sizes assigned to %d species: %s",
                             allSpecies.size(),
                             sizes
                            ));
        //reproduce species
        List<G> offspringGenotypes = new ArrayList<>(remaining);
        for (int i = 0; i < allSpecies.size(); i++) {
            int size = sizes.get(i);
            List<Individual<G, S, Double>> species = new ArrayList<>(allSpecies.get(i).getElements());
            species.sort(individualComparator.comparator());
            List<G> speciesOffspringGenotypes = new ArrayList<>();
            int counter = 0;
            while (speciesOffspringGenotypes.size() < size) {
                GeneticOperator<G> operator = Misc.pickRandomly(operators, random);
                List<G> parentGenotypes = new ArrayList<>(operator.arity());
                while (parentGenotypes.size() < operator.arity()) {
                    parentGenotypes.add(species.get(counter % species.size()).getGenotype());
                    counter = counter + 1;
                }
                speciesOffspringGenotypes.addAll(operator.apply(parentGenotypes, random));
            }
            offspringGenotypes.addAll(speciesOffspringGenotypes);
        }
        //merge
        offspring.addAll(buildIndividuals(offspringGenotypes, solutionMapper, fitnessFunction, executor, state));
        return offspring;
    }

}

