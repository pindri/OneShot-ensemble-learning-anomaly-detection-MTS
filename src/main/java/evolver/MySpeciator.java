package evolver;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.speciation.Speciator;
import it.units.malelab.jgea.core.evolver.speciation.Species;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import nodes.AbstractSTLNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MySpeciator<G, S extends AbstractSTLNode, F> implements Speciator<Individual<G, S, F>> {

//    List<String> variableList = List.of("FIT101", "LIT101", "MV101", "P101", "P102", "AIT201", "AIT202", "AIT203",
//                                        "FIT201", "MV201", "P202", "P203", "P204", "P205", "P206", "DPIT301", "FIT301",
//                                        "LIT301", "MV301", "MV302", "MV303", "MV304", "P301", "P302", "AIT401",
//                                        "AIT402", "FIT401", "LIT401", "P401", "P402", "P403", "P404", "UV401", "AIT501",
//                                        "AIT502", "AIT503", "AIT504", "FIT501", "FIT502", "FIT503", "FIT504", "P501",
//                                        "P502", "PIT501", "PIT502", "PIT503", "FIT601", "P601", "P602", "P603");

    List<String> variableList;

//    public MySpeciator(){}

    public MySpeciator(List<String> variableList) {
        this.variableList = variableList;
    }


    @Override
    public Collection<Species<Individual<G, S, F>>> speciate(
            PartiallyOrderedCollection<Individual<G, S, F>> population) {

        List<List<Individual<G, S, F>>> clusters = new ArrayList<>();
        for (String var : variableList) {
            List<Individual<G, S, F>> cluster = new ArrayList<>();
            for (Individual<G, S, F> individual : population.all()) {
                if (individual.getSolution().getVariablesList().contains(var)) {
                   cluster.add(individual);
                }
            }
            clusters.add(cluster);
        }

        return clusters.stream().filter(c -> c.size() > 0).map(c -> new Species<>(c, c.get(0))).collect(Collectors.toList());
    }
}
