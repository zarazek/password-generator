package org.srwk.passwordgenerator.client;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

public class LetterHistogram {
  private static final int NUM_OF_LETTERS = 'Z' - 'A' + 1;

  private final long[] upperCase;
  private final long[] lowerCase;

  public LetterHistogram() {
    this(new long[NUM_OF_LETTERS], new long[NUM_OF_LETTERS]);
  }

  public synchronized void addString(final String str) {
    final char[] chars = str.toCharArray();
    for (int i = 0; i < chars.length; ++i) {
      final char c = chars[i];
      if ('A' <= c && c <= 'Z') {
        ++upperCase[c - 'A'];
      } else if ('a' <= c && c <= 'z') {
        ++lowerCase[c - 'a'];
      } else {
        throw new IllegalArgumentException(String.format("'%s': invalid character at position %d: '%c'", str, i + 1, c));
      }
    }
  }

  public static LetterHistogram combine(final Iterable<LetterHistogram> histograms) {
    final long[] combinedUpperCase = new long[NUM_OF_LETTERS];
    final long[] combinedLowerCase = new long[NUM_OF_LETTERS];
    for (final LetterHistogram histogram : histograms) {
      for (int i = 0; i < NUM_OF_LETTERS; ++i) {
        combinedUpperCase[i] += histogram.upperCase[i];
        combinedLowerCase[i] += histogram.lowerCase[i];
      }
    }
    return new LetterHistogram(combinedUpperCase, combinedLowerCase);
  }

  public Map<Character, Pair<Long, Double>> getOccurrences() {
    final Map<Character, Pair<Long, Double>> result = new TreeMap<>();
    final long allOccurrences = Arrays.stream(upperCase).sum() + Arrays.stream(lowerCase).sum();
    for (int i = 0; i < NUM_OF_LETTERS; ++i) {
      char c = (char)('A' + i);
      final double probability = (double) upperCase[i] / (double) allOccurrences;
      result.put(c, Pair.of(upperCase[i], probability));
    }
    for (int i = 0; i < NUM_OF_LETTERS; ++i) {
      char c = (char)('a' + i); {
        final double probability = (double) lowerCase[i] / (double) allOccurrences;
        result.put(c, Pair.of(lowerCase[i], probability));
      }
    }
    return result;
  }

  private LetterHistogram(final long[] upperCase, final long[] lowerCase) {
    this.upperCase = upperCase;
    this.lowerCase = lowerCase;
  }
}
