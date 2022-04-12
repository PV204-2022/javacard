package applet;

import javacard.framework.Util;

/**
 * One instance stores all secrets on a particular card
 */
public class SecretList {

  private final SecretItem[] secrets;

  /**
   * Construct SecretList.
   */
  public SecretList() {
      secrets = new SecretItem[Configuration.SECRET_MAX_COUNT];
      for (byte i = 0; i < Configuration.SECRET_MAX_COUNT; i++) {
          secrets[i] = new SecretItem();
      }
  }

  /**
   * Get the secret value.
   * @param key - which key to retrieve.
   * @param dst - where to copy the value of the secret
   * @return the length of the value
   */
  public byte getSecret(byte key, byte[] dst) {
    byte valueLength = -1;
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key && valueLength == -1) {
        secrets[i].getValue(dst);
        valueLength = secrets[i].getValueLength();
      }
    }
    return valueLength;
  }

  /**
   * Set the secret key and value.
   * @param key - which key to set
   * @param value - to what value
   * @return whether the secret was set or not
   */
  public boolean setSecret(byte key, byte[] value) {
    boolean set = false;
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key && !set) {
        secrets[i].setValue(value);
        set = true;
      }
    }
    if (!set) {
      set = createSecret(key, value);
    }
    return set;
  }

  /**
   * Get list of secret keys.
   * @param dst - where to copy the keys
   * @return the number of keys
   */
  public byte listSecrets(byte[] dst) {
    for (byte i = 0; i < secrets.length; i++) {
      byte[] currentKey = new byte[] { secrets[i].getKey() };
      // fill the "dst" array by shifting offset in each iteration
      Util.arrayCopyNonAtomic(currentKey, (short) 0, dst, (short) i, (byte) 1);
    }
    return (byte) secrets.length;
  }

  /**
   * Delete secret.
   * @param key - the key of the secret to delete
   * @return whether the secret was deleted or not
   */
  public boolean deleteSecret(byte key) {
    boolean deleted = false;
    for (byte i = 0; i < secrets.length; i++) {
      if (secrets[i].getKey() == key && !deleted) {
        secrets[i].setKey((byte) 0);
        secrets[i].deleteValue();
        deleted = true;
      }
    }
    return deleted;
  }

  /**
   * Store key and value to the first empty secret available.
   * @param key - which key to set
   * @param value - to what value
   * @return - whether the secret was stored or not
   */
  private boolean createSecret(byte key, byte[] value) {
    boolean created = false;
    for (byte i = 0; i < secrets.length; i++) {
      // this is considered "Empty"
      if (secrets[i].getKey() == 0 && secrets[i].getValueLength() == 0 && !created) {
        secrets[i].setKey(key);
        secrets[i].setValue(value);
        created = true;
      }
    }
    return created;
  }
}
