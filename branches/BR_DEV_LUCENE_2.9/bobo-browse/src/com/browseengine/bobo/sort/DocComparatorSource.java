package com.browseengine.bobo.sort;

import java.io.IOException;
import java.text.Collator;
import java.util.Locale;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.StringIndex;

public abstract class DocComparatorSource {
	public abstract DocComparator getComparator(IndexReader reader)
			throws IOException;

	public static class IntDocComparatorSource extends DocComparatorSource {
		private final String field;

		public IntDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final int[] values = FieldCache.DEFAULT.getInts(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final int v1 = values[doc1];
					final int v2 = values[doc2];
					// cannot return v1-v2 because it could overflow
					if (v1 < v2) {
						return 1;
					} else if (v1 > v2) {
						return -1;
					} else {
						return 0;
					}
				}

				public Integer value(int doc) {
					return Integer.valueOf(values[doc]);
				}
			};
		}
	}
	
	public static class StringLocaleComparatorSource extends DocComparatorSource {
		private final String field;
		private final Collator _collator;

		public StringLocaleComparatorSource(String field,Locale locale) {
			this.field = field;
			_collator = Collator.getInstance(locale);
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final String[] values = FieldCache.DEFAULT.getStrings(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final String val1 = values[doc1];
				    final String val2 = values[doc2];
				    if (val1 == null) {
				      if (val2 == null) {
				        return 0;
				      }
				      return -1;
				    } else if (val2 == null) {
				      return 1;
				    }

				    return _collator.compare(val1, val2);
				}

				public String value(int doc) {
					return values[doc];
				}
			};
		}
	}
	
	public static class StringValComparatorSource extends DocComparatorSource {
		private final String field;

		public StringValComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final String[] values = FieldCache.DEFAULT.getStrings(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final String val1 = values[doc1];
				    final String val2 = values[doc2];
				    if (val1 == null) {
				      if (val2 == null) {
				        return 0;
				      }
				      return -1;
				    } else if (val2 == null) {
				      return 1;
				    }

				    return val1.compareTo(val2);
				}

				public String value(int doc) {
					return values[doc];
				}
			};
		}
	}
	
	public static class StringOrdComparatorSource extends DocComparatorSource {
		private final String field;

		public StringOrdComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final StringIndex values = FieldCache.DEFAULT.getStringIndex(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					return values.order[doc1] -  values.order[doc2];
				}

				public String value(int doc) {
					return String.valueOf(values.lookup[values.order[doc]]);
				}
			};
		}
	}
	
	public static class ShortDocComparatorSource extends DocComparatorSource {
		private final String field;

		public ShortDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final short[] values = FieldCache.DEFAULT.getShorts(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					return values[doc1] - values[doc2];
				}

				public Short value(int doc) {
					return Short.valueOf(values[doc]);
				}
			};
		}
	}
	
	public static class LongDocComparatorSource extends DocComparatorSource {
		private final String field;

		public LongDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final long[] values = FieldCache.DEFAULT.getLongs(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final long v1 = values[doc1];
					final long v2 = values[doc2];
					// cannot return v1-v2 because it could overflow
					if (v1 < v2) {
						return 1;
					} else if (v1 > v2) {
						return -1;
					} else {
						return 0;
					}
				}

				public Long value(int doc) {
					return Long.valueOf(values[doc]);
				}
			};
		}
	}
	
	public static class FloatDocComparatorSource extends DocComparatorSource {
		private final String field;

		public FloatDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final float[] values = FieldCache.DEFAULT.getFloats(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final float v1 = values[doc1];
					final float v2 = values[doc2];
					// cannot return v1-v2 because it could overflow
					if (v1 < v2) {
						return 1;
					} else if (v1 > v2) {
						return -1;
					} else {
						return 0;
					}
				}

				public Float value(int doc) {
					return Float.valueOf(values[doc]);
				}
			};
		}
	}
	
	public static class DoubleDocComparatorSource extends DocComparatorSource {
		private final String field;

		public DoubleDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final double[] values = FieldCache.DEFAULT.getDoubles(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					final double v1 = values[doc1];
					final double v2 = values[doc2];
					// cannot return v1-v2 because it could overflow
					if (v1 < v2) {
						return 1;
					} else if (v1 > v2) {
						return -1;
					} else {
						return 0;
					}
				}

				public Double value(int doc) {
					return Double.valueOf(values[doc]);
				}
			};
		}
	}
	
	public static class ByteDocComparatorSource extends DocComparatorSource {
		private final String field;

		public ByteDocComparatorSource(String field) {
			this.field = field;
		}

		public DocComparator getComparator(IndexReader reader)
				throws IOException {

			final byte[] values = FieldCache.DEFAULT.getBytes(reader, field);

			return new DocComparator() {
				public int compare(int doc1, int doc2) {
					return values[doc1] - values[doc2];
				}

				public Byte value(int doc) {
					return Byte.valueOf(values[doc]);
				}
			};
		}
	}
	
	
}
