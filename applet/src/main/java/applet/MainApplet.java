package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class MainApplet extends javacard.framework.Applet {
	// storage for all key:value pairs, capacity is defined in "Configuration" class
	private SecretList storage = new SecretList();
	// PINs
	private OwnerPIN pin = null;
	private OwnerPIN duressPin = null;
	// SecureChannel related stuff
	private Cipher mediaCipherEnc = null;
	private Cipher mediaCipherDec = null;
	private AESKey mediaKey = null;

	// APDU offsets
	final static byte CLA_MAINAPPLET = (byte) 0xB0;
	final static byte INS_GET = (byte) 0x50;
	final static byte INS_SET = (byte) 0x51;
	final static byte INS_DEL = (byte) 0x54;
	final static byte INS_LIST = (byte) 0x52;
	final static byte INS_AUTH = (byte) 0x53;

	// just a garbage array
	private byte[] tempArray = null;

	/**
	 * Method installing the MainApplet
	 * @param bArray the array containing installation parameters
	 * @param bOffset the starting offset in bArray
	 * @param bLength the length in bytes of the data parameter in bArray
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new MainApplet(bArray, bOffset, bLength);
	}

	/**
	 * MainApplet default constructor Only this class's install method should
	 * create the applet object.
	 */
	public MainApplet(byte[] buffer, short offset, byte length)	{
		// data offset is used for application specific parameter.
		// initialization with default offset (AID offset).
		short dataOffset = offset;
		boolean isOP2 = false;

		// initialize PINs
		pin = new OwnerPIN((byte) Configuration.PIN_MAX_ATTEMPTS, (byte) Configuration.PIN_MAX_LENGTH);
		pin.update(Configuration.PIN, (short) 0, (byte) Configuration.PIN.length);
		duressPin = new OwnerPIN((byte) Configuration.PIN_MAX_ATTEMPTS, (byte) Configuration.PIN_MAX_LENGTH);
		duressPin.update(Configuration.DURESS_PIN, (short) 0, (byte) Configuration.PIN.length);

		// initialize media encryption stuff
		mediaKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.ALG_TYPE_AES, KeyBuilder.LENGTH_AES_256, false);
		mediaCipherEnc = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		mediaCipherEnc.init(mediaKey, Cipher.MODE_ENCRYPT);
		mediaCipherDec = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		mediaCipherDec.init(mediaKey, Cipher.MODE_DECRYPT);

		// init garbage array
		tempArray = JCSystem.makeTransientByteArray((short) 260, JCSystem.CLEAR_ON_DESELECT);

		register();
	}

	public void process(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();

		// byte cla = apduBuffer[ISO7816.OFFSET_CLA];
		// byte ins = apduBuffer[ISO7816.OFFSET_INS];
		// short lc = (short)apduBuffer[ISO7816.OFFSET_LC];
		// short p1 = (short)apduBuffer[ISO7816.OFFSET_P1];
		// short p2 = (short)apduBuffer[ISO7816.OFFSET_P2];

		// ignore the applet select command dispatched to the process
		if (selectingApplet()) {
			return;
		}

	    try {
		    // APDU instruction parser
			if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_MAINAPPLET) {
				// verify PIN, if duress, then nuke
				verifyPIN(apdu);

				switch (apduBuffer[ISO7816.OFFSET_INS]) {
					case INS_SET:
						Set(apdu);
						break;
					case INS_GET:
						Get(apdu);
						break;
					case INS_LIST:
						List(apdu);
						break;
					default:
						// Unsupported instruction
						ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
						break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * Set provided key to the provided value
     * @param
     */
	private void Set(APDU apdu) {
		// get the buffer with incoming APDU
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();

		// mediaCipherEnc.doFinal(apduBuffer, ISO7816.OFFSET_CDATA, dataLen, tempArray, (short) 0);

		// select key by an offset

		// select value by an offset

		// storage.setSecret();

		return;
	}

	/**
	 * Get the value of the provided key
	 * @param apdu
	 */
	private void Get(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();


		// note: copy data straight to apduBuffer ???

		storage.getSecret();

		// COPY ENCRYPTED DATA INTO OUTGOING BUFFER
    	Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, m_hash.getLength());

    	// SEND OUTGOING BUFFER
    	apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, m_hash.getLength());

		return;
	}

	/**
	 * List all keys
	 * @param apdu
	 */
	private void List(APDU apdu) {
		// get the buffer with incoming APDU
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();

		storage.listSecrets();

		return;
	}

	/**
	 * Basic (duress) PIN logic
	 * @param apdu
	 */
	private void verifyPIN(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();

		if (duressPin.check(apduBuffer, ISO7816.OFFSET_CDATA, (byte) dataLen) == true) {
			// wipe the key
			mediaKey.clearKey();
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		} else {
			// reset the counter, so that duress PIN can't be blocked
			duressPin.reset();
		}

		// verify the "good" pin
		if (pin.check(apduBuffer, ISO7816.OFFSET_CDATA, (byte) dataLen) == false) {
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}
	}

	public boolean select(boolean b) {
		return true;
	}

	public void deselect(boolean b) {
		return;
	}
}
