package signal;

import java.io.IOException;
import java.util.List;

abstract public class AbstractSignalBuilder<T> {

    public abstract T build(String path, List<Integer> boolIndexes, List<Integer> numIndexes) throws IOException;

    public abstract List<Integer> parseLabels(String path) throws IOException;

}
