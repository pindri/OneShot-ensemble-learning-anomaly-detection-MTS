package arrayUtilities;

import java.util.Arrays;
import java.util.List;

public class ArraysUtilities {

    static int[] sumIntArrays(List<int[]> arrays) {

        int maxLength = 0;

        for (int[] arr : arrays) {
            if (arr.length > maxLength) maxLength = arr.length;
        }

        int[] sum = new int[maxLength];

        for (int[] arr : arrays) {
            for (int i = 0; i < arr.length; i++) {
                sum[i] += arr[i];
            }
        }

        return sum;
    }

    static int[] arraysAND(List<int[]> arrays) {

        int[] sum = sumIntArrays(arrays);
        int threshold = arrays.size();

        return Arrays.stream(sum).map(x -> x == threshold ? 1 : 0).toArray();
    }

    static int[] arraysOR(List<int[]> arrays) {

        int[] sum = sumIntArrays(arrays);

        return Arrays.stream(sum).map(x -> x > 0 ? 1 : 0).toArray();
    }
}
