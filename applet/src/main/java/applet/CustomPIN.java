package applet;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class CustomPIN {

    // Possible PIN status values.
    public static final byte PIN_STATUS_CORRECT = (byte) 0;
    public static final byte PIN_STATUS_INCORRECT = (byte) 1;
    public static final byte PIN_STATUS_DURESS = (byte) 2;
    public static final byte PIN_STATUS_UNDEFINED = (byte) 3;

    final private OwnerPIN pin;
    private byte[] duressPin;
    final private byte pinMaxLength;
    private byte latestCheckResult = PIN_STATUS_UNDEFINED;

    /**
     * Construct CustomPIN.
     *
     * @param retries - number of PIN retries
     * @param pinMaxLength - max length of the PIN
     */
    public CustomPIN(byte retries, byte pinMaxLength) {
        this.pin = new OwnerPIN(retries, pinMaxLength);
        this.duressPin = new byte[pinMaxLength];
        this.pinMaxLength = pinMaxLength;
	}

    /**
     * Initialize PIN.
     *
     * @param pinValue - the new value of the PIN
     * @param pinLength - the length of the PIN
     */
    public void initPin(byte[] pinValue, byte pinLength) throws PINException {
        this.pin.update(pinValue, (short) 0, pinLength);
    }

    /**
     * Initialize duress PIN.
     *
     * @param pinValue - the new value of the duress PIN
     * @param pinLength - the length of the duress PIN
     */
    public void initDuressPin(byte[] pinValue, byte pinLength) throws PINException {
        if (pinLength > this.pinMaxLength) {
            throw new PINException(PINException.ILLEGAL_VALUE);
        }
        this.duressPin = pinValue;
    }

    /**
     * Check value of the PIN.
     *
     * @param pinValue - the value of the PIN to check
     * @param pinLength - the length of the PIN to check
     * @return PIN_STATUS_*
     */
    public byte check(byte[] pinValue, byte pinLength) throws PINException {
        latestCheckResult = PIN_STATUS_INCORRECT;
        if (!this.pin.check(pinValue, (short) 0, pinLength)) {
            if (this.pin.getTriesRemaining() == (byte) 0 || pinValue == this.duressPin) {
                latestCheckResult = PIN_STATUS_DURESS;
            }
        } else {
            latestCheckResult = PIN_STATUS_CORRECT;
        }
        return latestCheckResult;
    }

    /**
     * Get the latest result of the check function.
     *
     * @return PIN_STATUS_*
     */
    public byte getLatestCheckResult() {
        return latestCheckResult;
    }
}
