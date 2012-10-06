package com.continuuity.metadata;

/**
 * Defines standard fields for different types of data.
 */
public class FieldTypes {

  /**
   * Class representing constants for fields stored for Stream
   */
  public static class Stream {
    public static final String ID = "stream";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CREATE_DATE = "createDate";
    public static final String CAPACITY_IN_BYTES = "capacityInBytes";
    public static final String EXPIRY_IN_SECONDS = "expiryInSeconds";
  }

  /**
   * Class representing constants for fields stored for Dataset
   */
  public static class Dataset {
    public static final String ID = "dataset";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CREATE_DATE = "createDate";
    public static final String TYPE = "type";
  }

  /**
   * Class representing constants for fields stored for Application
   */
  public static class Application {
    public static final String ID = "application";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String CREATE_DATE = "createDate";
  }
}
