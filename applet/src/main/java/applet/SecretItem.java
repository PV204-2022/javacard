package applet;

import javacard.framework.Util;

/**
 * Stores exactly one value
 */
public class SecretItem {
  private final bytes[] value;
  
  /**
   * Just a basic constructor
   *
   */
  public SecretItem() {
    value = new bytes[Configuration.SECRET_VALUE_MAX_LENGTH];
  }

  /**
   * A Getter
   * @param dest - Where do we put it
   */
  public void getValue(byte[] dst) {
    Util.arrayCopy(value, (short) 0, dst, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }

  /**
   * A Setter
   * @param src - What do we store
   */
  public void setValue(byte[] src) {
    if (src.length > SECRET_VALUE_MAX_LENGTH) {
      throw new UserException();
    }
    
    Util.arrayCopy(src, (short) 0, value, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH);
  }
}
