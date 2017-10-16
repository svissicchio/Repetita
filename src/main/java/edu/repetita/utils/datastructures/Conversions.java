package edu.repetita.utils.datastructures;

import java.util.ArrayList;;

/**
 * Conversion utilities from {@code ArrayList<Integer/Double>} to int/double[].
 * 
 * @author Steven Gay
 */

public class Conversions {
  private Conversions() {} // only static methods
  
  /**
   * Converts an {@code ArrayList<Integer>} to an {@code int[]}.
   * 
   * @param values an ArrayList of Integers
   * @return the corresponding int[]
   */
  public static int[] arrayListInteger2arrayint(ArrayList<Integer> values) {
    int n = values.size();
    int[] ret = new int[n];
    for (int i = 0; i < n; i++) ret[i] = values.get(i);
    return ret;
  }
  
  /**
   * Converts an {@code ArrayList<Double>} to a {@code double[]}.
   * 
   * @param values an ArrayList of Doubles
   * @return the corresponding double[]
   */
  public static double[] arrayListDouble2arraydouble(ArrayList<Double> values) {
    int n = values.size();
    double[] ret = new double[n];
    for (int i = 0; i < n; i++) ret[i] = values.get(i);
    return ret;
  }
}
