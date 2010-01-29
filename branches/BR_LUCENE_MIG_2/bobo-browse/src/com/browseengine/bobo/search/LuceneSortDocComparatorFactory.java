package com.browseengine.bobo.search;

import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.search.ExtendedFieldCache;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

public class LuceneSortDocComparatorFactory
{
  private static Logger logger = Logger.getLogger(LuceneSortDocComparatorFactory.class);
  
  public static final ScoreDocComparator buildScoreDocComparator(IndexReader reader, SortFieldEntry entry) throws IOException
  {
    String fieldname = entry.field;
    int type = entry.type;
    
    if (type == SortField.DOC) return ScoreDocComparator.INDEXORDER;
    if (type == SortField.SCORE) return ScoreDocComparator.RELEVANCE;
    
    if (type == SortField.CUSTOM && entry.custom!=null){
    	return entry.custom.newComparator(reader, fieldname);
    }
    
    Collection indexFieldnames = reader.getFieldNames(FieldOption.INDEXED);
    if (!indexFieldnames.contains(fieldname)){
    	logger.warn(fieldname+" is not a sortable field, ignored.");
    	return null;
    }
    
    Locale locale = entry.locale;
    SortComparatorSource factory = entry.custom;
    ScoreDocComparator comparator=null;
    switch (type) {
      case SortField.AUTO:
        comparator = comparatorAuto (reader, fieldname);
        break;
      case SortField.INT:
        comparator = comparatorInt (reader, fieldname);
        break;
      case SortField.FLOAT:
        comparator = comparatorFloat (reader, fieldname);
        break;
      case SortField.LONG:
        comparator = comparatorLong(reader, fieldname);
        break;
      case SortField.DOUBLE:
        comparator = comparatorDouble(reader, fieldname);
        break;
      case SortField.SHORT:
        comparator = comparatorShort(reader, fieldname);
        break;
      case SortField.BYTE:
        comparator = comparatorByte(reader, fieldname);
        break;
      case SortField.STRING:
        if (locale != null) comparator = comparatorStringLocale (reader, fieldname, locale);
        else comparator = comparatorString (reader, fieldname);
        break;
      case SortField.CUSTOM:
    	if (factory!=null){
          comparator = factory.newComparator(reader, fieldname);
    	}
    	else{
    	  logger.warn(fieldname+": no factory specified, ignored.");
    	}
        break;
      default:
        throw new RuntimeException ("unknown field type: "+type);
    }
    return comparator;
  }
  

   /**
   * Returns a comparator for sorting hits according to a field containing bytes.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg integer values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorByte(final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final byte[] fieldOrder = FieldCache.DEFAULT.getBytes(reader, field);
    return new ScoreDocComparator() {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final int fi = fieldOrder[i.doc];
        final int fj = fieldOrder[j.doc];
        if (fi < fj) return -1;
        if (fi > fj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Byte(fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.INT;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing shorts.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg integer values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorShort(final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final short[] fieldOrder = FieldCache.DEFAULT.getShorts(reader, field);
    return new ScoreDocComparator() {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final int fi = fieldOrder[i.doc];
        final int fj = fieldOrder[j.doc];
        if (fi < fj) return -1;
        if (fi > fj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Short(fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.SHORT;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing integers.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg integer values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorInt (final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final int[] fieldOrder = FieldCache.DEFAULT.getInts (reader, field);
    return new ScoreDocComparator() {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final int fi = fieldOrder[i.doc];
        final int fj = fieldOrder[j.doc];
        if (fi < fj) return -1;
        if (fi > fj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Integer (fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.INT;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing integers.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg integer values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorLong (final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final long[] fieldOrder = ExtendedFieldCache.EXT_DEFAULT.getLongs (reader, field);
    return new ScoreDocComparator() {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final long li = fieldOrder[i.doc];
        final long lj = fieldOrder[j.doc];
        if (li < lj) return -1;
        if (li > lj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Long(fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.LONG;
      }
    };
  }


  /**
   * Returns a comparator for sorting hits according to a field containing floats.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg float values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorFloat (final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final float[] fieldOrder = FieldCache.DEFAULT.getFloats (reader, field);
    return new ScoreDocComparator () {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final float fi = fieldOrder[i.doc];
        final float fj = fieldOrder[j.doc];
        if (fi < fj) return -1;
        if (fi > fj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Float (fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.FLOAT;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing doubles.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg float values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorDouble(final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final double[] fieldOrder = ExtendedFieldCache.EXT_DEFAULT.getDoubles (reader, field);
    return new ScoreDocComparator () {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final double di = fieldOrder[i.doc];
        final double dj = fieldOrder[j.doc];
        if (di < dj) return -1;
        if (di > dj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return new Double (fieldOrder[i.doc]);
      }

      public int sortType() {
        return SortField.DOUBLE;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing strings.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg string values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorString (final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    final FieldCache.StringIndex index = FieldCache.DEFAULT.getStringIndex (reader, field);
    return new ScoreDocComparator () {

      public final int compare (final ScoreDoc i, final ScoreDoc j) {
        final int fi = index.order[i.doc];
        final int fj = index.order[j.doc];
        if (fi < fj) return -1;
        if (fi > fj) return 1;
        return 0;
      }

      public Comparable sortValue (final ScoreDoc i) {
        return index.lookup[index.order[i.doc]];
      }

      public int sortType() {
        return SortField.STRING;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to a field containing strings.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg string values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorStringLocale (final IndexReader reader, final String fieldname, final Locale locale)
  throws IOException {
    final Collator collator = Collator.getInstance (locale);
    final String field = fieldname.intern();
    final String[] index = FieldCache.DEFAULT.getStrings (reader, field);
    return new ScoreDocComparator() {

        public final int compare(final ScoreDoc i, final ScoreDoc j) {
            String is = index[i.doc];
            String js = index[j.doc];
            if (is == js) {
                return 0;
            } else if (is == null) {
                return -1;
            } else if (js == null) {
                return 1;
            } else {
                return collator.compare(is, js);
            }
        }

      public Comparable sortValue (final ScoreDoc i) {
        return index[i.doc];
      }

      public int sortType() {
        return SortField.STRING;
      }
    };
  }

  /**
   * Returns a comparator for sorting hits according to values in the given field.
   * The terms in the field are looked at to determine whether they contain integers,
   * floats or strings.  Once the type is determined, one of the other static methods
   * in this class is called to get the comparator.
   * @param reader  Index to use.
   * @param fieldname  Fieldable containg values.
   * @return  Comparator for sorting hits.
   * @throws IOException If an error occurs reading the index.
   */
  static ScoreDocComparator comparatorAuto (final IndexReader reader, final String fieldname)
  throws IOException {
    final String field = fieldname.intern();
    Object lookupArray = ExtendedFieldCache.EXT_DEFAULT.getAuto (reader, field);
    if (lookupArray instanceof FieldCache.StringIndex) {
      return comparatorString (reader, field);
    } else if (lookupArray instanceof int[]) {
      return comparatorInt (reader, field);
    } else if (lookupArray instanceof long[]) {
      return comparatorLong (reader, field);
    } else if (lookupArray instanceof float[]) {
      return comparatorFloat (reader, field);
    } else if (lookupArray instanceof String[]) {
      return comparatorString (reader, field);
    } else {
      throw new RuntimeException ("unknown data type in field '"+field+"'");
    }
  }
}
