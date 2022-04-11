package applet;

import javacard.framework.APDU;
import javacard.framework.ISO7816;
import javacard.framework.Util;
import javacard.security.*;
import javacardx.crypto.*;

/**
 * Class for management of communication between APDU and Applet
 */
public class SecureChannel {
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private KeyAgreement keyAgreement;

    /**
     * A basic constructor
     */
    public SecureChannel() {
        keyPair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_F2M_163);
        keyPair.genKeyPair();

        keyAgreement = KeyAgreement.getInstance(KeyAgreement.ALG_EC_SVDP_DH, false);
        keyAgreement.init(keyPair.getPrivate());

        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
    }

    /**
     * Establish the SecureChannel - generate and send DH
     * @param apdu - an APDU
     */
    public void establish(APDU apdu) {
        // DISCLAIMER: following section is borrowed from StackOverflow and needs to be adapter
        byte[] buf = apdu.getBuffer();
        byte temp[] = new byte[100];
        byte secret[] = new byte[100];
        byte size = buf[ISO7816.OFFSET_LC];

        Util.arrayCopy(buf, ISO7816.OFFSET_CDATA, temp, (byte) 0, size);

        // the public key is in temp
        short len = dh.generateSecret(temp, (byte) 0, size, secret, (byte) 0);

        Util.arrayCopy(temp, (byte) 0, buf, ISO7816.OFFSET_CDATA, size);
        //Util.arrayCopy(secret, (byte) 0, buf, ISO7816.OFFSET_CDATA, len);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, size);
    }


    /**
     *
     * @param src - data to be encrypted
     * @param dst - where to store the output
     */
    public void encrypt(byte[] src, byte[] dst) {

    }

    /**
     *
     * @param src - to be decrypted
     * @param dst - where to store the ouput
     */
    public void decrypt(byte[] src, byte[] dst) {

    }

    /**
     * Get our DH thingy (to be sent to the other party)
     */
    public void getPublicDH(byte[] dst) {
        Util.arrayCopyNonAtomic(this.secret, (short) 0, dst, (short) 0, (byte) 100);
    }

    /**
     * Set public DH thingy received from the other party
     */
    public void setPublicDH() {

    }
}
