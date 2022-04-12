package app;

import applet.MainApplet;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PcApp {
    private static String APPLET_AID = "73696d706c656170706c6574";

    private static final String STR_APDU_GET = "B050";
    private static final String STR_APDU_SET = "B051";
    private static final String STR_APDU_LIST = "B052";
    private static final String STR_APDU_AUTH = "B053";
    private static final String STR_APDU_DEL = "B054";

    private static final int APDU_SUCCESS = 0x9000;

    private enum CARD_TYPE {
        SIMULATED, PHYSICAL
    }

    CardManager cardManager = null;

    public static void main(String[] args) {
        try {
            PcApp main = new PcApp();
            main.setup(CARD_TYPE.SIMULATED);
            main.demo();
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public void setup(CARD_TYPE cardType) throws Exception {
        cardManager = new CardManager(true, Util.hexStringToByteArray(APPLET_AID));
        final RunConfig runConfig = RunConfig.getDefaultConfig();
        if (cardType == CARD_TYPE.SIMULATED) {
            runConfig.setAppletToSimulate(MainApplet.class);
            runConfig.setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL);
        } else if (cardType == CARD_TYPE.PHYSICAL) {
            runConfig.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);
        } else {
            throw new Exception("Nonexistent card type.");
        }

        System.out.print("Connecting to card...");
        if (!this.cardManager.Connect(runConfig)) {
            System.out.println("Connection failed.");
        }
        System.out.println("Connection successful.");
    }

    public void demo() throws Exception {
        // authenticate
        byte[] pin = new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8 };
        authCmd(pin);

        // Add four key:value pairs.
        setCmdPrint((byte) 0x01, Util.hexStringToByteArray("313233343536")); // 123456
        setCmdPrint((byte) 0x02, Util.hexStringToByteArray("30313233343536373839414243444546")); // 0123456789ABCDEF
        setCmdPrint((byte) 0x04, Util.hexStringToByteArray("414243444546")); // ABCDEF
        setCmdPrint((byte) 0x7F, Util.hexStringToByteArray("616263646566")); // abcdef

        // Use too long value.
        byte[] tooLongValue = new byte[65];
        Arrays.fill(tooLongValue, (byte) 0x30);
        setCmdPrint((byte) 0x10, tooLongValue);

        // Use too big key.
        setCmdPrint((byte) 0x80, Util.hexStringToByteArray("616263646566"));

        getCmdPrint((byte) 0x04);
        getCmdPrint((byte) 0x02);
        getCmdPrint((byte) 0x01);
        getCmdPrint((byte) 0x7F);

        // Get nonexistent key.
        getCmdPrint((byte) 0x08);

        listCmdPrint();
        delCmdPrint((byte) 0x04);
        listCmdPrint();

        delCmdPrint((byte) 0x04);

        delCmdPrint((byte) 0x01);
        listCmdPrint();

        delCmdPrint((byte) 0x02);
        listCmdPrint();
    }

    public byte[] getCmd(byte key) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_GET));
        commandStream.write(key);
        commandStream.write(new byte[] { 0, 0 });
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));
        byte[] data = response.getData();
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != 0) {
                return Arrays.copyOf(data, i + 1);
            }
        }
        return null;
    }

    public void getCmdPrint(byte key) throws Exception {
        byte[] data = getCmd(key);
        if (data != null) {
            System.out.println(
                "Get Value for Key " +
                String.format("\"%02X\": ", key) +
                "\"" + new String(data, StandardCharsets.UTF_8) + "\""
            );
        } else {
            System.out.println("FAILED to Get Value for Key " + String.format("%02X", key));
        }
    }

    public boolean setCmd(byte key, byte[] value) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_SET));
        commandStream.write(key);
        commandStream.write(0);
        commandStream.write(value.length);
        commandStream.write(value);
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));
        return response.getSW() == APDU_SUCCESS && response.getData()[0] != 0;
    }

    public void setCmdPrint(byte key, byte[] value) throws Exception {
        boolean result = setCmd(key, value);
        System.out.print(
            "Set Key:Value to " +
            String.format("\"%02X\":", key) +
            "\"" + new String(value, StandardCharsets.UTF_8) + "\""
        );
        if (result) {
            System.out.println(" SUCCESS");
        } else {
            System.out.println(" FAILED");
        }
    }

    public byte[] listCmd() throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_LIST));
        commandStream.write(new byte[] { 0, 0, 0 });
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));
        byte[] data = response.getData();

        byte[] validKeys = new byte[data.length];
        int count = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] != 0) {
                validKeys[count] = data[i];
                count++;
            }
        }
        return Arrays.copyOf(validKeys, count);
    }

    public void listCmdPrint() throws Exception {
        byte[] listResult = listCmd();
        System.out.print("List of Keys: ");
        for (byte key : listResult) {
            System.out.print(String.format("\"%02X\"", key));
            if (key != listResult[listResult.length-1]) {
                System.out.print(", ");
            }
        }
        System.out.println("");
    }

    public boolean delCmd(byte key) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_DEL));
        commandStream.write(key);
        commandStream.write(new byte[] { 0, 0 });
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));
        return response.getSW() == APDU_SUCCESS && response.getData()[0] == 1;
    }

    public void delCmdPrint(byte key) throws Exception {
        boolean result = delCmd(key);
        System.out.print("Delete Key " + String.format("\"%02X\" ", key));
        if (result) {
            System.out.println("SUCCESS");
        } else {
            System.out.println("FAILED");
        }
    }

    public boolean authCmd(byte[] pin) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_AUTH));
        commandStream.write((byte) 0); // empty P1
        commandStream.write((byte) 0); // empty P2
        commandStream.write((byte) pin.length); // empty P2
        commandStream.write(pin);
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));

        return response.getSW() == APDU_SUCCESS && response.getData()[0] == 1;
    }
}
