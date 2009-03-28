package com.browseengine.bobo.search;

import java.util.Locale;

import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

/** Expert: Every composite-key in the internal cache is of this type. */
public class SortFieldEntry {
  final String field;        // which Fieldable
  final int type;            // which SortField type
  final SortComparatorSource custom;       // which custom comparator
  final Locale locale;       // the locale we're sorting (if string)

  /** Creates one of these objects. */
  public SortFieldEntry (String field, int type, Locale locale) {
    this.field = field.intern();
    this.type = type;
    this.custom = null;
    this.locale = locale;
  }

  /** Creates one of these objects for a custom comparator. */
  public SortFieldEntry (String field, SortComparatorSource custom) {
    this.field = field.intern();
    this.type = SortField.CUSTOM;
    this.custom = custom;
    this.locale = null;
  }

  /** Two of these are equal iff they reference the same field and type. */
  public boolean equals (Object o) {
    if (o instanceof SortFieldEntry) {
      SortFieldEntry other = (SortFieldEntry) o;
      if (other.field == field && other.type == type) {
        if (other.locale == null ? locale == null : other.locale.equals(locale)) {
          if (other.custom == null) {
            if (custom == null) return true;
          } else if (other.custom.equals (custom)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /** Composes a hashcode based on the field and type. */
  public int hashCode() {
    return field.hashCode() ^ type ^ (custom==null ? 0 : custom.hashCode()) ^ (locale==null ? 0 : locale.hashCode());
  }
}
