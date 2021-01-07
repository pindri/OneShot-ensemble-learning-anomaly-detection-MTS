//package datacollectors;
//
//import it.units.malelab.jgea.core.listener.collector.FunctionOfOneBest;
//import it.units.malelab.jgea.core.listener.collector.Item;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class BestTreeInfo extends FunctionOfOneBest<Object, Object, Object> {
//    public BestTreeInfo(String... fitnessFormats) {
//        super(new IndividualTreeInfo<>((f) -> {
//            List<Item> items = new ArrayList<>();
//            if (f instanceof List) {
//                for(int i = 0; i < ((List<?>)f).size(); ++i) {
//                    items.add(new Item("objective." + i, ((List<?>)f).get(i), fitnessFormats.length > 0 ? fitnessFormats[i % fitnessFormats.length] : "%s"));
//                }
//            } else {
//                items.add(new Item("value", f, fitnessFormats.length > 0 ? fitnessFormats[0] : "%s"));
//            }
//
//            return items;
//        }));
//    }
//}
