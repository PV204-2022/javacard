package applet;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * Stores exactly one key value pair.
 */
public class SecretItem {
  private byte key = 0;
  private final byte[] value;
  private byte valueLength = 0;

  /**
   * Construct SecretItem.
   */
  public SecretItem() {
    value = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
  }

  /**
   * Get value of the secret.
   * @param dst - the destination where to copy the value
   */
  public void getValue(byte[] dst) {
    // length is always the same
    // no offset, no need for them yet
    Util.arrayCopyNonAtomic(value, (short) 0, dst, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * Get key of the secret.
   */
  public byte getKey() {
    return key;
  }

  /**
   * Set value of the secret.
   * @param src - the source from where to copy the value
   */
  public void setValue(byte[] src) {
    if (src.length > Configuration.SECRET_VALUE_MAX_LENGTH) {
      ISOException.throwIt(ISO7816.SW_WRONG_DATA);
    }

    this.valueLength = (byte) src.length;
    Util.arrayCopyNonAtomic(src, (short) 0, value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * Set key of the secret.
   * @param key - the key to set
   */
  public void setKey(byte key) {
    this.key = key;
  }

  /**
   * Delete the value of the secret.
   */
  public void deleteValue() {
    Util.arrayFillNonAtomic(value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH, (byte) 0);
    this.valueLength = 0;
  }

  /**
   * Get length of the secret value.
   * @return the length of the secret value
   */
  public byte getValueLength() {
      return valueLength;
  }
}
