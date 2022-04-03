package applet;

import javacard.framework.Util;

/**
 * Stores exactly one value
 */
public class SecretItem {
  private final byte[] key;
  private final byte[] value;
  private int valueLength = 0;
  private int keyLength = 0;

  /**
   * Just a basic constructor
   */
  public SecretItem() {
    key = new byte[Configuration.SECRET_KEY_MAX_LENGTH];
    value = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
  }

  /**
   * A Getter for value attribute
   * @param dst - Where do we put it
   */
  public void getValue(byte[] dst) {
    // length is always the same
    // no offset, no need for them yet
    Util.arrayCopyNonAtomic(value, (short) 0, dst, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * A Getter for key attribute
   * @param dst - Where do we put it
   */
  public void getKey(byte[] dst) {
    Util.arrayCopyNonAtomic(key, (short) 0, dst, (short) 0, Configuration.SECRET_KEY_MAX_LENGTH);
  }

  /**
   * A Setter for value attribute
   * @param src - What do we set to
   */
  public void setValue(byte[] src) {
    if (src.length > Configuration.SECRET_VALUE_MAX_LENGTH) {
      throw new RuntimeException("Value is too long!");
    }

    this.valueLength = src.length;
    Util.arrayCopyNonAtomic(src, (short) 0, value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * A Setter for key attribute
   * @param src - What do we set to
   */
  public void setKey(byte[] src) {
    if (src.length > Configuration.SECRET_KEY_MAX_LENGTH) {
      throw new RuntimeException("Key is too long!");
    }

    this.keyLength = src.length;
    Util.arrayCopyNonAtomic(src, (short) 0, key, (short) 0, Configuration.SECRET_KEY_MAX_LENGTH);
  }

  /**
   * A Getter
   * @return used length of value
   */
  public int getValueLength() {
    return valueLength;
  }

  /**
   * A Getter
   * @return used length of key
   */
  public int getKeyLength() { return keyLength; }
}
