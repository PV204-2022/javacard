package simulator;

import applet.MainApplet;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.io.ByteArrayOutputStream;

public class SimpleAPDU {
    private static String APPLET_AID = "73696d706c656170706c6574";

    private static final String STR_APDU_GET = "B050";
    private static final String STR_APDU_SET = "B051";
    private static final String STR_APDU_LIST = "B052";
    private static final String STR_APDU_AUTH = "B053";
    private static final String STR_APDU_DEL = "B054";

    private enum CARD_TYPE {
        SIMULATED, PHYSICAL
    }

    CardManager cardManager = null;

    public static void main(String[] args) {
        try {
            SimpleAPDU main = new SimpleAPDU();
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
        setCmd((byte) 0x01, Util.hexStringToByteArray("414243444546"));
        getCmd((byte) 0x01);
        setCmd((byte) 0x02, Util.hexStringToByteArray("FFFFFFFF"));
        getCmd((byte) 0x02);
    }

    public void getCmd(byte key) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_GET));
        commandStream.write(key);
        commandStream.write(0);
        commandStream.write(0);
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(commandStream.toByteArray()));
        byte[] data = response.getData();

        System.out.println(response);
    }

    public void setCmd(byte key, byte[] value) throws Exception {
        ByteArrayOutputStream commandStream = new ByteArrayOutputStream();
        commandStream.write(Util.hexStringToByteArray(STR_APDU_SET));
        commandStream.write(key);
        commandStream.write(0);
        commandStream.write(value.length);
        commandStream.write(value);
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(new byte[] {-80, 81, 1, 0, 6, 65, 66, 67, 68, 69, 70, 1}));
        byte[] data = response.getData();

        System.out.println(response);
    }

    public void listCmd() throws Exception {

    }

    public void authCmd() throws Exception {

    }

    public void delCmd() throws Exception {

    }
}
