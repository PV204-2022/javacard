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
  public SecretList(short capacity) {
    secrets = new SecretItem[Configuration.SECRET_MAX_COUNT];
  }

  /**
   * A Getter for secrets
   * @param key - Which key to retrieve
   * @param dst - Where to put it
   */
  public void getSecret(byte[] key, byte[] dst) {
    byte[] currentKey = new byte[Configuration.SECRET_KEY_MAX_LENGHT];

    for (int i = 0; i < secrets.length; i++) {
      secrets[i].getKey(currentKey);

      if (currentKey == key) {
        secrets[i].getValue(dst);
      }
    }
  }

  /**
   * A Setter for secrets
   * @param key - Which key to set
   * @param src - To what value
   */
  public void setSecret(byte[] key, byte[] value, byte[] src) {
    byte[] currentKey = new byte[Configuration.SECRET_KEY_MAX_LENGHT];
    
    for (int i = 0; i < secrets.length; i++) {
      secrets[i].getKey(currentKey);
      
      if (currentKey == key) {
        secrets[i].setKey(src);
      }
    }
  }

  /**
   * A basic list method
   * @param dst - Where to put it
   */
  public void listSecrets(byte[] dst) {
    dst = new byte[Configuration.SECRET_KEY_MAX_LENGHT * Configuration.SECRET_MAX_COUNT];
    byte[] currentKey = new byte[Configuration.SECRET_KEY_MAX_LENGHT];
    
    for (int i = 0; i < secrets.length; i++) {
      secrets[i].getKey(currentKey);
      // fill the "dst" array by shifting offset in each iteration
      Util.arrayCopyNonAtomic(currentKey, (short) 0, dst, (short) (0 * i), Configuration.SECRET_KEY_MAX_LENGHT);
    }
  }
}
