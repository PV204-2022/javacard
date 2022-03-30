package applet;

import javacard.framework.OwnerPINx;

public class CustomPIN extends OwnerPINx {

    public CustomPIN(byte tryLimit, byte maxPINSize) throws PINException
        super(tryLimit, maxPINSize)
    	register();
	}

}
