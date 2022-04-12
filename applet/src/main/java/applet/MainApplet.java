package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.*;

public class MainApplet extends javacard.framework.Applet {
	// storage for all key:value pairs, capacity is defined in "Configuration" class
	private SecretList storage = new SecretList();
	// PINs/mediaEncryption/SecureChannel (so that it cannot be easily flipped)
	private CustomPIN pin = null;
	private byte secured = WEIRD_FALSE;
	private byte authenticated = WEIRD_FALSE;
	final static byte WEIRD_TRUE = (byte) 0xf0e9;
	final static byte WEIRD_FALSE = (byte) 0x1111;
	private Cipher mediaCipherEnc = null;
	private Cipher mediaCipherDec = null;
	private AESKey mediaKey = null;
	private SecureChannel channel;

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
	final

	/**
	 * Method installing the MainApplet.
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new MainApplet(bArray, bOffset, bLength);
	}

	/**
	 * MainApplet default constructor.
	 * Only this class's install method should create the applet object.
	 */
	public MainApplet(byte[] buffer, short offset, byte length)	{
		// initialize media encryption stuff
		RandomData randomGen = RandomData.getInstance(RandomData.ALG_KEYGENERATION);
		byte[] randomKey = new byte[32];
		randomGen.nextBytes(randomKey, (short) 0, (short) 32);

		mediaKey = (AESKey) KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
		mediaKey.setKey(randomKey, (short) 0);
		mediaCipherEnc = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		mediaCipherDec = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);

		// initialize PINs
		pin = new CustomPIN(Configuration.PIN_MAX_ATTEMPTS, Configuration.PIN_MAX_LENGTH);
		pin.initPin(Configuration.PIN, (byte) Configuration.PIN.length);
		pin.initDuressPin(Configuration.DURESS_PIN, (byte) Configuration.DURESS_PIN.length);

		// initialize SecureChannel
		channel = new SecureChannel();

		register();
	}

	public void process(APDU apdu) {
		byte[] apduBuffer;
		byte[] encApduBuffer;

		if (secured == WEIRD_FALSE) {
			channel.establish(apdu);
			secured = WEIRD_TRUE;
		}

		encApduBuffer = apdu.getBuffer();
		channel.decrypt(encApduBuffer, apduBuffer);

		// ignore the applet select command dispatched to the process
		if (selectingApplet()) {
			return;
		}

		// APDU instruction parser
		try {
			if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_MAINAPPLET) {
				// in order to continue, one needs to be authenticated (send correct PIN)
				if (authenticated != WEIRD_TRUE && apduBuffer[ISO7816.OFFSET_INS] != INS_AUTH) {
					ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
				}

				switch (apduBuffer[ISO7816.OFFSET_INS]) {
					case INS_AUTH:
						verifyPIN(apdu);
						break;
					case INS_SET:
						Set(apdu);
						break;
					case INS_GET:
						Get(apdu);
						break;
					case INS_LIST:
						List(apdu);
						break;
					case INS_DEL:
						Delete(apdu);
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
	 * Set provided key to the provided value.
	 * @param apdu - APDU command
	 */
	private void Set(APDU apdu) {
		byte[] encApduBuffer = apdu.getBuffer();
		byte[] apduBuffer = new byte[encApduBuffer.length];
		channel.decrypt(encApduBuffer, apduBuffer);

		short dataLen = apdu.getIncomingLength();
		if (dataLen > Configuration.SECRET_VALUE_MAX_LENGTH) {
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}

		byte key = apduBuffer[ISO7816.OFFSET_P1];

		mediaCipherEnc.init(mediaKey, Cipher.MODE_ENCRYPT);
		byte[] temp = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
		short bytesWritten = mediaCipherEnc.doFinal(apduBuffer, ISO7816.OFFSET_CDATA, Configuration.SECRET_VALUE_MAX_LENGTH, temp, (short) 0);
		byte[] tempSized = new byte[bytesWritten];
		Util.arrayCopy(temp, (short) 0, tempSized, (short) 0, bytesWritten);
		if (key <= 0) {
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}
		if (!storage.setSecret(key, tempSized)) {
			bytesWritten = 0;
		}

		short offset = (short) (ISO7816.OFFSET_CDATA + dataLen);
		channel.encrypt(apduBuffer, encApduBuffer);
		Util.arrayCopy(new byte[] { (byte) bytesWritten }, (short) 0, encApduBuffer, offset, (short) 1);
		apdu.setOutgoingAndSend(offset, (short) 1);
	}

	/**
	 * Get the value for the provided key.
	 * @param apdu - APDU command
	 */
	private void Get(APDU apdu) {
		byte[] encApduBuffer = apdu.getBuffer();
		byte[] apduBuffer = new byte[encApduBuffer.length];
		channel.decrypt(encApduBuffer, apduBuffer);
		byte key = apduBuffer[ISO7816.OFFSET_P1];

		byte[] temp = new byte[Configuration.SECRET_VALUE_MAX_LENGTH];
		byte length = storage.getSecret(key, temp);
		if (length < 0) {
			ISOException.throwIt(ISO7816.SW_WRONG_DATA);
		}

		mediaCipherDec.init(mediaKey, Cipher.MODE_DECRYPT);
		short bytesRead = mediaCipherDec.doFinal(temp, (short) 0, length, apduBuffer, ISO7816.OFFSET_CDATA);

		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, bytesRead);
	}

	/**
	 * List all stored keys.
	 * @param apdu - APDU command
	 */
	private void List(APDU apdu) {
		byte[] encApduBuffer = apdu.getBuffer();
		byte[] apduBuffer = new byte[encApduBuffer.length];
		channel.decrypt(apduBuffer, encApduBuffer);

		byte [] temp = new byte[Configuration.SECRET_MAX_COUNT];
		byte bytesWritten = storage.listSecrets(temp);
		Util.arrayCopy(temp, (short) 0, apduBuffer, ISO7816.OFFSET_CDATA, bytesWritten);

		channel.encrypt(apduBuffer, encApduBuffer);
		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, bytesWritten);
	}

	/**
	 * Delete key and value for provided key.
	 * @param apdu - APDU command
	 */
	private void Delete(APDU apdu) {
		byte[] encApduBuffer = apdu.getBuffer();
		byte[] apduBuffer = new byte[encApduBuffer.length];
		channel.decrypt(encApduBuffer, apduBuffer);
		byte key = apduBuffer[ISO7816.OFFSET_P1];

		boolean deleteSuccess = storage.deleteSecret(key);
		Util.arrayCopy(
			new byte[] { (byte) (deleteSuccess ? 1 : 0 ) }, (short) 0, apduBuffer, ISO7816.OFFSET_CDATA, (short) 1
		);

		channel.encrypt(apduBuffer, encApduBuffer);
		apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) 1);
	}

	/**
	 * Verify PIN.
	 * @param apdu - APDU command
	 */
	private void verifyPIN(APDU apdu) {
		byte[] encApduBuffer = apdu.getBuffer();
		byte[] apduBuffer = new byte[encApduBuffer.length];
		short dataLen = apdu.setIncomingAndReceive();
		channel.decrypt(encApduBuffer, apduBuffer);

		byte[] pinValue = null;
		Util.arrayCopy(apduBuffer, ISO7816.OFFSET_CDATA, pinValue, (short) 0, dataLen);

		switch (pin.check(pinValue, (byte) dataLen)) {
			case CustomPIN.PIN_STATUS_DURESS:
				mediaKey.clearKey();
				authenticated = WEIRD_FALSE;
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);
			case CustomPIN.PIN_STATUS_INCORRECT:
				authenticated = WEIRD_FALSE;
				ISOException.throwIt(ISO7816.SW_WRONG_DATA);
			case CustomPIN.PIN_STATUS_CORRECT:
				authenticated = WEIRD_TRUE;
				Util.arrayCopyNonAtomic(pinValue, ISO7816.OFFSET_CDATA, apduBuffer, (short) 0, dataLen);
				channel.encrypt(apduBuffer, encApduBuffer);
				apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, dataLen);
		}
	}
}
