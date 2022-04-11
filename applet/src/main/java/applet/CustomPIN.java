package applet;

import javacard.framework.OwnerPIN;
import javacard.framework.PINException;

public class CustomPIN {

    public static final byte PIN_STATUS_CORRECT = (byte) 0;
    public static final byte PIN_STATUS_INCORRECT = (byte) 1;
    public static final byte PIN_STATUS_DURESS = (byte) 2;
    public static final byte PIN_STATUS_UNDEFINED = (byte) 3;

    final private OwnerPIN pin;
    private byte[] duressPin;
    final private byte pinMaxLength;
    private byte latestCheckResult = PIN_STATUS_UNDEFINED;

    public CustomPIN(byte retries, byte pinMaxLength) {
        this.pin = new OwnerPIN(retries, pinMaxLength);
        this.duressPin = new byte[pinMaxLength];
        this.pinMaxLength = pinMaxLength;
	}

    public void initPin(byte[] pinValue, byte pinLength) throws PINException {
        this.pin.update(pinValue, (short) 0, pinLength);
    }

    public void initDuressPin(byte[] pinValue, byte pinLength) throws PINException {
        if (pinLength > this.pinMaxLength) {
            throw new PINException(PINException.ILLEGAL_VALUE);
        }
        this.duressPin = pinValue;
    }

    public byte check(byte[] pinValue, byte pinLength) throws PINException {
        if (!this.pin.check(pinValue, (short) 0, pinLength)) {
            if (pinValue == this.duressPin) {
                latestCheckResult = PIN_STATUS_DURESS;
            } else {
                latestCheckResult = PIN_STATUS_CORRECT;
            }
        }
        latestCheckResult = PIN_STATUS_CORRECT;
        return latestCheckResult;
    }

    public byte getLatestCheckResult() {
        return latestCheckResult;
    }

}