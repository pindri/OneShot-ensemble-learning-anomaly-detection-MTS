package evolution;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.speciation.Speciator;
import it.units.malelab.jgea.core.evolver.speciation.Species;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import nodes.AbstractSTLNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeSpeciator<G, S extends AbstractSTLNode, F> implements Speciator<Individual<G, S, F>> {

    List<String> variableList;

    public AttributeSpeciator(List<String> variableList) {
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

        return clusters.stream().filter(c -> c.size() > 0)
                       .map(c -> new Species<>(c, c.get(0))).collect(Collectors.toList());
    }
}
