package applet;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Stores exactly one value
 */
public class SecretItem {
  private byte key = 0;
  private final byte[] value;
  private byte valueLength = 0;

  /**
   * Just a basic constructor
   */
  public SecretItem() {
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
  public byte getKey() {
    return key;
  }

  /**
   * A Setter for value attribute
   * @param src - What do we set to
   */
  public void setValue(byte[] src) {
    if (src.length > Configuration.SECRET_VALUE_MAX_LENGTH) {
      ISOException.throwIt(ISO7816.SW_WRONG_DATA);
    }

    this.valueLength = (byte) src.length;
    Util.arrayCopyNonAtomic(src, (short) 0, value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * A Setter for key attribute
   * @param src - What do we set to
   */
  public void setKey(byte src) {
    key = src;
  }

  public void deleteValue() {
    Util.arrayFillNonAtomic(value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH, (byte) 0);
    this.valueLength = 0;
  }

  /**
   * A Getter
   * @return used length of value
   */
  public byte getValueLength() {
      return valueLength;
  }
}
