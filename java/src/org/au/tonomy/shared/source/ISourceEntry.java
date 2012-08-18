package org.au.tonomy.shared.source;

import java.util.Map;

import org.au.tonomy.shared.util.Promise;

/**
 * An entry in a source tree; either a file or a folder.
 */
public interface ISourceEntry {

  /**
   * The short name of this entry.
   */
  public String getShortName();

  /**
   * The full path of this entry.
   */
  public String getFullPath();

  /**
   * True iff this is a folder.
   */
  public boolean isFolder();

  /**
   * Returns the contents of this entry, assuming it is a file.
   */
  public Promise<String> readFile();

  /**
   * Returns a map from entry names to entries, assuming this is a folder.
   */
  public Promise<? extends Map<String, ? extends ISourceEntry>> listEntries();

}