package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class MainApplet extends javacard.framework.Applet {
	// storage for all key:value pairs, capacity is defined in "Configuration" class
	private SecretList storage = new SecretList();
	// PINs
	private CustomPIN pin = null;
	// SecureChannel related stuff
	private Cipher mediaCipherEnc = null;
	private Cipher mediaCipherDec = null;
	private AESKey mediaKey = null;

	// TODO REMOVE
	final static short SW_Exception = (short) 0xff01;
	final static short SW_ArrayIndexOutOfBoundsException = (short) 0xff02;
	final static short SW_ArithmeticException = (short) 0xff03;
	final static short SW_ArrayStoreException = (short) 0xff04;
	final static short SW_NullPointerException = (short) 0xff05;
	final static short SW_NegativeArraySizeException = (short) 0xff06;
	final static short SW_CryptoException_prefix = (short) 0xf100;
	final static short SW_SystemException_prefix = (short) 0xf200;
	final static short SW_PINException_prefix = (short) 0xf300;
	final static short SW_TransactionException_prefix = (short) 0xf400;
	final static short SW_CardRuntimeException_prefix = (short) 0xf500;

	// APDU offsets
	final static byte CLA_MAINAPPLET = (byte) 0xB0;
	final static byte INS_GET = (byte) 0x50;
	final static byte INS_SET = (byte) 0x51;
	final static byte INS_DEL = (byte) 0x54;
	final static byte INS_LIST = (byte) 0x52;
	final static byte INS_AUTH = (byte) 0x53;

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
		// initialize media encryption stuff
		mediaKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
		mediaKey.setKey(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, (short) 0);
		mediaCipherEnc = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		mediaCipherDec.init(mediaKey, Cipher.MODE_ENCRYPT);
		mediaCipherDec = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		mediaCipherDec.init(mediaKey, Cipher.MODE_DECRYPT);

		// initialize PINs
		pin = new CustomPIN(Configuration.PIN_MAX_ATTEMPTS, Configuration.PIN_MAX_LENGTH);
		pin.initPin(Configuration.PIN, (byte) Configuration.PIN.length);
		pin.initDuressPin(Configuration.DURESS_PIN, (byte) Configuration.DURESS_PIN.length);

		register();
	}

	public void process(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();

		// byte cla = apduBuffer[ISO7816.OFFSET_CLA];
		// byte ins = apduBuffer[ISO7816.OFFSET_INS];
		// short lc = (short)apduBuffer[ISO7816.OFFSET_LC];
		// short p2 = (short)apduBuffer[ISO7816.OFFSET_P2];

		// ignore the applet select command dispatched to the process
		if (selectingApplet()) {
			return;
		}

		// APDU instruction parser
		try {
			if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_MAINAPPLET) {
				// verify PIN, if duress, then nuke
				// verifyPIN(apdu);

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
						ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
						break;
				}
			}
		} catch (ISOException e) {
			throw e; // Our exception from code, just re-emit
		} catch (ArrayIndexOutOfBoundsException e) {
			ISOException.throwIt(SW_ArrayIndexOutOfBoundsException);
		} catch (ArithmeticException e) {
			ISOException.throwIt(SW_ArithmeticException);
		} catch (ArrayStoreException e) {
			ISOException.throwIt(SW_ArrayStoreException);
		} catch (NullPointerException e) {
			ISOException.throwIt(SW_NullPointerException);
		} catch (NegativeArraySizeException e) {
			ISOException.throwIt(SW_NegativeArraySizeException);
		} catch (CryptoException e) {
			ISOException.throwIt((short) (SW_CryptoException_prefix | e.getReason()));
		} catch (SystemException e) {
			ISOException.throwIt((short) (SW_SystemException_prefix | e.getReason()));
		} catch (PINException e) {
			ISOException.throwIt((short) (SW_PINException_prefix | e.getReason()));
		} catch (TransactionException e) {
			ISOException.throwIt((short) (SW_TransactionException_prefix | e.getReason()));
		} catch (CardRuntimeException e) {
			ISOException.throwIt((short) (SW_CardRuntimeException_prefix | e.getReason()));
		} catch (Exception e) {
			ISOException.throwIt(SW_Exception);
		}
	}

	/**
	 * Set provided key to the provided value
	 * @param apdu - an apdu
	 */
	private void Set(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.getIncomingLength();
		byte key = apduBuffer[ISO7816.OFFSET_P1];

		byte[] temp = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
		short bytesWritten = mediaCipherEnc.doFinal(apduBuffer, ISO7816.OFFSET_CDATA, Configuration.SECRET_VALUE_MAX_LENGTH, temp, (short) 0);
		byte[] tempSized = new byte[bytesWritten];
		Util.arrayCopy(temp, (short) 0, tempSized, (short) 0, bytesWritten);
		if (!storage.setSecret(key, tempSized)) {
			storage.createSecret(key, tempSized);
		}

		short offset = (short) (ISO7816.OFFSET_CDATA + dataLen + 1);
		Util.arrayCopy(new byte[] { (byte) bytesWritten }, (short) 0, apduBuffer, offset, (short) 1);
		apdu.setOutgoingAndSend(offset, (short) 1);
	}

	/**
	 * Get the value of the provided key
	 * @param apdu
	 */
	private void Get(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		byte key = apduBuffer[ISO7816.OFFSET_P1];

		byte[] temp = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
		storage.getSecret(key, temp);
		short bytesRead = mediaCipherDec.doFinal(temp, (short) 0, Configuration.SECRET_VALUE_MAX_LENGTH, apduBuffer, ISO7816.OFFSET_CDATA);

		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, bytesRead);
	}

	/**
	 * List all keys
	 * @param apdu
	 */
	private void List(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();

		byte[] temp = new byte[Configuration.SECRET_MAX_COUNT];
		storage.listSecrets(temp);
		Util.arrayCopyNonAtomic(temp, (short) 0, apduBuffer, (short) 0, Configuration.SECRET_MAX_COUNT);

		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, Configuration.SECRET_MAX_COUNT);
	}

	/**
	 * Basic (duress) PIN logic
	 * @param apdu
	 */
	private void verifyPIN(APDU apdu) {
		byte[] apduBuffer = apdu.getBuffer();
		short dataLen = apdu.setIncomingAndReceive();
		byte[] pinValue = null;
		Util.arrayCopy(apduBuffer, (short) ISO7816.OFFSET_CDATA, pinValue, (short) 0, dataLen);

		switch (pin.check(pinValue, (byte) dataLen)) {
			case CustomPIN.PIN_STATUS_DURESS:
				mediaKey.clearKey();
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);
			case CustomPIN.PIN_STATUS_INCORRECT:
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}
	}

	public boolean select() {
		return true;
	}

	@Override
	public void deselect() {
		return;
	}
}