package simulator;

import applet.MainApplet;
import cardTools.CardManager;
import cardTools.CardType;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

public class SimpleAPDU {
    private static String APPLET_AID = "abc1564cd216f13cccab";

    private static final String STR_APDU_GET = "B050100000";
    private static final String STR_APDU_SET = "B051100000";
    private static final String STR_APDU_LIST = "B052100000";
    private static final String STR_APDU_AUTH = "B053100000";
    private static final String STR_APDU_DEL = "B054100000";

    private enum CARD_TYPE {
        SIMULATED, PHYSICAL
    }

    final CardManager cardManager = new CardManager(true, Util.hexStringToByteArray(APPLET_AID));

    public static void main(String[] args) {
        try {
            SimpleAPDU main = new SimpleAPDU();
            main.setup(CARD_TYPE.SIMULATED);

        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public void setup(CARD_TYPE cardType) throws Exception {
        final RunConfig runConfig = RunConfig.getDefaultConfig();
        if (cardType == CARD_TYPE.SIMULATED) {
            runConfig.setAppletToSimulate(MainApplet.class); // main class of applet to simulate
            runConfig.setTestCardType(CardType.JCARDSIMLOCAL); // Use local simulator
        } else if (cardType == CARD_TYPE.PHYSICAL) {
            runConfig.setTestCardType(CardType.PHYSICAL);
        } else {
            throw new Exception("Nonexistent card type.");
        }

        System.out.print("Connecting to card...");
        if (!this.cardManager.connect(runConfig)) {
            System.out.println("Connection failed.");
        }
        System.out.println("Connection successful.");
    }

    public void demo() throws Exception {
        // Transmit single APDU
        ResponseAPDU response = this.cardManager.transmit(new CommandAPDU(Util.hexStringToByteArray()));
        byte[] data = response.getData();

        response = this.cardManager.transmit(new CommandAPDU(Util.hexStringToByteArray(STR_APDU_GET))); // Use other constructor for CommandAPDU
        data = response.getData();

        System.out.println(response);
    }

    public void getCmd() throws Exception {

    }

    public void setCmd() throws Exception {

    }

    public void listCmd() throws Exception {

    }

    public void authCmd() throws Exception {

    }

    public void delCmd() throws Exception {

    }
}
