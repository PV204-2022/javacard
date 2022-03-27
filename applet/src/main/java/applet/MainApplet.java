package applet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.*;

public class MainApplet extends Applet implements MultiSelectable {
	private static final short BUFFER_SIZE = 32;

	private byte[] tmpBuffer = JCSystem.makeTransientByteArray(BUFFER_SIZE, JCSystem.CLEAR_ON_DESELECT);
	private RandomData random;

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new MainApplet(bArray, bOffset, bLength);
	}
	
	public MainApplet(byte[] buffer, short offset, byte length)	{
		random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
		register();
	}

	public void process(APDU apdu) {
    // get the buffer with incoming APDU
		byte[] apduBuffer = apdu.getBuffer();

		// byte cla = apduBuffer[ISO7816.OFFSET_CLA];
		// byte ins = apduBuffer[ISO7816.OFFSET_INS];
		// short lc = (short)apduBuffer[ISO7816.OFFSET_LC];
		// short p1 = (short)apduBuffer[ISO7816.OFFSET_P1];
		// short p2 = (short)apduBuffer[ISO7816.OFFSET_P2];

    // ignore the applet select command dispached to the process
    if (selectingApplet()) {
        return;
    }

    try {
      // APDU instruction parser
    	if (apduBuffer[ISO7816.OFFSET_CLA] == CLA_SIMPLEAPPLET) {
        switch (apduBuffer[ISO7816.OFFSET_INS]) {
					case INS_SET:
						Set(apdu);
						break;
					case INS_GET:
						Get(apdu)
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
		}
	}

  // Set provided key to the provided value
	void set(APDU apdu) {
    // TODO: add stuff here
		 
		return;
	}

  // Get the value of the provided key
	void get(APDU apdu) {

		// TODO: add stuff, adopt things from cv3

    // COPY ENCRYPTED DATA INTO OUTGOING BUFFER
    Util.arrayCopyNonAtomic(m_ramArray, (short) 0, apdubuf, ISO7816.OFFSET_CDATA, m_hash.getLength());

    // SEND OUTGOING BUFFER
    apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, m_hash.getLength());

		return;
	}

	// List all keys
	void list(APDU apdu) {
		// TODO: add stuff

		return;
	}

	public boolean select(boolean b) {
		return true;
	}

	public void deselect(boolean b) {
		// TODO: wipe all keys and stuff
	}
}
