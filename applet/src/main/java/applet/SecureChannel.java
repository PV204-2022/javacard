package applet;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * Class for management of communication between APDU and Applet
 *   Proposed usage: run "establish" on card connect, keep instance as applet attribute, use "encrypt"/"decrypt" on each apduBuffer
 */
public class SecureChannel {
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyAgreement keyAgreement;
    private byte[] sharedSecret;
    private AESKey derivedkey;

    /**
     * A basic constructor
     */
    public SecureChannel() {
        keyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_2048);
        keyPair.genKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        keyAgreement = KeyAgreement.getInstance(KeyAgreement.ALG_DH_PLAIN, false);
        keyAgreement.init(keyPair.getPrivate());

        sharedSecret = new byte[publicKey.getSize()];
        derivedkey = (AESKey) KeyBuilder.buildKey(KeyBuilder.ALG_TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
    }

    /**
     * Establish the SecureChannel - generate and send DH
     * @param apdu - an APDU
     */
    public void establish(APDU apdu) {
        byte[] apduBuffer = apdu.getBuffer();

        // publicData - buffer holding the public data of the second party
        // publicOffset - offset into the publicData buffer at which the data begins
        // publicLength - byte length of the public data
        // secret - buffer to hold the secret output
        // secretOffset - offset into the secret array at which to start writing the secret
        keyAgreement.generateSecret(apduBuffer, ISO7816.OFFSET_CDATA, publicKey.getSize(), sharedSecret, (short) 0);
        derivedkey.setKey(sharedSecret, (short) 0);
    }

    /**
     * Encrypt data via derivedKey
     * @param src - data to be encrypted
     * @param dst - where to store the output
     */
    public void encrypt(byte[] src, byte[] dst) {
        Cipher cipherEnc = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        cipherEnc.init(derivedkey, Cipher.MODE_ENCRYPT);

        cipherEnc.doFinal(src, (short) 0, (short) src.length, dst, (short) 0);
    }

    /**
     * Decrypt data via derivedKey
     * @param src - to be decrypted
     * @param dst - where to store the output
     */
    public void decrypt(byte[] src, byte[] dst) {
        Cipher cipherDec = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        cipherDec.init(derivedkey, Cipher.MODE_DECRYPT);

        cipherDec.doFinal(src, (short) 0, (short) src.length, dst, (short) 0);
    }
}
