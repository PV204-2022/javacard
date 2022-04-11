package applet;

import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 * One instance stores all secrets on a particular card
 */
public class SecretList {
  // this array stores the secrets objects
  private final SecretItem[] secrets;

  /**
   * Just a basic constructor
   */
  public SecretList() {
      secrets = new SecretItem[Configuration.SECRET_MAX_COUNT];
      for (byte i = 0; i < Configuration.SECRET_MAX_COUNT; i++) {
          secrets[i] = new SecretItem();
      }
  }

  /**
   * A Getter for secrets
   * @param key - Which key to retrieve
   * @param dst - Where to put it
   */
  public byte getSecret(byte key, byte[] dst) {
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key) {
        secrets[i].getValue(dst);
        return secrets[i].getValueLength();
      }
    }
    return -1;
  }

  /**
   * A Setter for secrets
   * @param key - Which key to set
   * @param value - To what value
   */
  public boolean setSecret(byte key, byte[] value) {
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key) {
        secrets[i].setValue(value);
        return true;
      }
    }
    return false;
  }

  /**
   * A basic list method
   * @param dst - Where to put it
   */
  public byte listSecrets(byte[] dst) {
    for (byte i = 0; i < secrets.length; i++) {
      byte[] currentKey = new byte[] { secrets[i].getKey() };
      // fill the "dst" array by shifting offset in each iteration
      Util.arrayCopyNonAtomic(currentKey, (short) 0, dst, (short) i, (byte) 1);
    }
    return (byte) secrets.length;
  }

  public byte deleteSecret(byte key) {
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key) {
        secrets[i].setKey((byte) 0);
        secrets[i].deleteValue();
      }
    }
    return 1;
  }

  /**
   * Populates the first empty Secret available
   * @param key - How is it going to be named
   * @param value - What do we store
   * @return - Which index is it stored at
   */
  public byte createSecret(byte key, byte[] value) {
    for (byte i = 0; i < secrets.length; i++) {
      // this is considered "Empty"
      if (secrets[i].getKey() == 0 && secrets[i].getValueLength() == 0) {
        secrets[i].setKey(key);
        secrets[i].setValue(value);

        return i;
      };
    }

    return -1;
  }
}
