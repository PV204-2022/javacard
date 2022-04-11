package applet;

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
  public void getSecret(byte key, byte[] dst) {
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key) {
        secrets[i].getValue(dst);
        return;
      }
    }
  }

  /**
   * A Setter for secrets
   * @param key - Which key to set
   * @param src - To what value
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
  public void listSecrets(byte[] dst) {
    dst = new byte[Configuration.SECRET_MAX_COUNT];

    for (byte i = 0; i < secrets.length; i++) {
      byte[] currentKey = new byte[] {secrets[i].getKey()};
      // fill the "dst" array by shifting offset in each iteration
      Util.arrayCopyNonAtomic(currentKey, (short) 0, dst, (short) (0 * i), (byte) 1);
    }
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
      if (secrets[i].getValueLength() == 0) {
        secrets[i].setKey(key);
        secrets[i].setValue(value);

        return i;
      };
    }

    return -1;
  }
}
