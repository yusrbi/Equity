package utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility functions for Strings.
 */
public class StringUtils {

  public static final String BOUNDARY_CHAR = "_";

  /**
   * Returns all ngrams in the string of the specified length. Contains boundary ngrams.
   *
   * For the string "a" and a length of 2, the ngrams "_a" and "a_" would be returned.
   *
   * @param s String to exrract ngrams from.
   * @param length  Length of the ngrams.
   * @return  Set of all ngrams in the string, including boundary ngrams.
   */
  public static Set<String> getNgrams(String s, int length) {
    Set<String> ngrams = new HashSet<>();
    StringBuilder sb = new StringBuilder(3);
    for (int i = -length + 1; i < s.length(); ++i) {
      int j = i;
      // Fill up the beginning of the string with placeholder chars.
      while (j < 0) {
        sb.append(BOUNDARY_CHAR);
        ++j;
      }
      while (sb.length() < length && j < s.length()) {
        sb.append(s.charAt(j));
        ++j;
      }
      // Fill up the end of the string with placeholder chars.
      while (sb.length() < length) {
        sb.append(BOUNDARY_CHAR);
        ++j;
      }
      ngrams.add(sb.toString());
      sb.setLength(0);
    }
    return ngrams;
  }
}
