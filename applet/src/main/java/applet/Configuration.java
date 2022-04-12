package applet;

/**
 * Configuration constants for the entire applet.
 */

public class Configuration {
  // Maximum length of values (checked by setters).
  public static final byte SECRET_VALUE_MAX_LENGTH = (byte) 64;

  // Maximum value of keys (checked by setters).
  public static final byte SECRET_KEY_MAX = (byte) 0x80;

  // Maximum number of the stored secrets.
  public static final byte SECRET_MAX_COUNT = (byte) 16;

  // How many attempts before self-destruct.
  public static final byte PIN_MAX_ATTEMPTS = (byte) 5;

  // Maximum supported length of PIN.
  public static final byte PIN_MAX_LENGTH = (byte) 8;

  // The actual PIN value (must be < PIN_MAX_LENGTH).
  public static final byte[] PIN = { (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8 };

  // The DURESS_PIN value (must be < PIN_MAX_LENGTH).
  public static final byte[] DURESS_PIN = { (byte) 6, (byte) 6, (byte) 6 };
}
