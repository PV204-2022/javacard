package applet;

/**
 * Arbitrary configuration constants for the entire Applet
 */

public class Configuration {
  // Maximum length of values (checked by setters)
  public static final byte SECRET_VALUE_MAX_LENGTH = (byte) 64;

  // Maximum length of keys (checked by setters)
  public static final byte SECRET_KEY_MAX_LENGHT = (byte) 32;

  // Maximum number of the stored secrets
  public static final byte SECRET_MAX_COUNT = (byte) 16;

  // How many attempts before self-destruct
  public static final byte PIN_MAX_ATTEMPTS = (byte) 5;

  // Maximum supported length of PIN
  public static final byte PIN_MAX_LENGTH = (byte) 8;

  // The actual PIN value (must be < PIN_MAX_LENGTH)
  public static final byte PIN = (byte) 12345678;
}