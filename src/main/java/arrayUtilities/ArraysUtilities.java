package arrayUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArraysUtilities {

    public static int minLength(List<int[]> arrays) {
        int min = Integer.MAX_VALUE;
        for (int[] arr : arrays) {
            if (arr.length < min) {
                min = arr.length;
            }
        }
        return min;
    }

    public static List<int[]> trimHeadSameSize(List<int[]> arrays) {
        int min = minLength(arrays);
        List<int[]> result = new ArrayList<>();

        for (int[] arr : arrays) {
            int[] trimmed = new int[min];
            System.arraycopy(arr, arr.length-min, trimmed, 0, min);
            result.add(trimmed);
        }

        return result;
    }

    public static int[] sumIntArrays(List<int[]> arrays) {

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

    public static int[] labelsOR(List<int[]> arrays) {

        int[] sum = sumIntArrays(arrays);
        int threshold = arrays.size();

        return Arrays.stream(sum).map(x -> x == threshold ? 1 : 0).toArray();
    }

    public static int[] labelsAND(List<int[]> arrays) {

        int[] sum = sumIntArrays(arrays);

        return Arrays.stream(sum).map(x -> x > 0 ? 1 : 0).toArray();
    }

    public static int[] labelsMajority(List<int[]> arrays) {

        int[] sum = sumIntArrays(arrays);
        int threshold = arrays.size()/2 + 1; // More than half.

        return Arrays.stream(sum).map(x -> x >= threshold ? 1 : 0).toArray();
    }
}
