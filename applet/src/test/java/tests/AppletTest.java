package tests;

import applet.Configuration;
import cardTools.CardManager;
import cardTools.CardType;
import org.junit.jupiter.api.*;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.Arrays;

/**
 * Example test class for the applet
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author xsvenda, Dusan Klinec (ph4r05)
 */
public class AppletTest extends BaseTest {

    private CardManager cardManager = null;

    public AppletTest() {
        setCardType(CardType.JCARDSIMLOCAL);
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUpMethod() throws Exception {
        cardManager = connect();
    }

    @AfterEach
    public void tearDownMethod() throws Exception {
    }

    @Test
    public void testAuthSuccess() throws Exception {
        final CommandAPDU cmd = new CommandAPDU(
                0xB0, 0x53, 0, 0, new byte[] { (byte) 1, (byte) 2, (byte) 3, (byte) 4, (byte) 5, (byte) 6, (byte) 7, (byte) 8 }
        );
        final ResponseAPDU responseAPDU = connectAndSend(cmd);
        Assertions.assertNotNull(responseAPDU);
        Assertions.assertEquals(responseAPDU.getData()[0], (byte) 1);
    }

    @Test
    public void testSetSuccess() throws Exception {
        final CommandAPDU cmd = new CommandAPDU(
            0xB0, 0x51, 0X01, 0, new byte[] { 0x40, 0x41, 0x42, 0x43, 0x44, 0x45 }
        );
        final ResponseAPDU responseAPDU = connectAndSend(cmd);
        Assertions.assertNotNull(responseAPDU);
        Assertions.assertEquals(0x9000, responseAPDU.getSW());
        Assertions.assertEquals(responseAPDU.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);
    }

    @Test
    public void testSetFailureBigKey() throws Exception {
        final CommandAPDU cmd = new CommandAPDU(
            0xB0, 0x51, Configuration.SECRET_KEY_MAX, 0, new byte[] { 0x40, 0x41, 0x42, 0x43, 0x44, 0x45 }
        );
        final ResponseAPDU responseAPDU = connectAndSend(cmd);
        Assertions.assertNotNull(responseAPDU);
        Assertions.assertEquals(0x6A80, responseAPDU.getSW());
    }

    @Test
    public void testSetFailureLongValue() throws Exception {
        byte[] tooLongValue = new byte[65];
        Arrays.fill(tooLongValue, (byte) 0x20);
        final CommandAPDU cmd = new CommandAPDU(0xB0, 0x51, 0X90, 0, tooLongValue );
        final ResponseAPDU responseAPDU = connectAndSend(cmd);
        Assertions.assertNotNull(responseAPDU);
        Assertions.assertEquals(0x6A80, responseAPDU.getSW());
    }

    @Test
    public void testSetFailureTooMuchSecrets() throws Exception {
        for (int i = 1; i <= Configuration.SECRET_MAX_COUNT; i++) {
            final CommandAPDU cmd = new CommandAPDU(
                0xB0, 0x51, i, 0, new byte[]{(byte) (0x40 + i), 0x41, 0x42, 0x43, 0x44, 0x45}
            );
            final ResponseAPDU responseAPDU = cardManager.transmit(cmd);
            Assertions.assertNotNull(responseAPDU);
            Assertions.assertEquals(0x9000, responseAPDU.getSW());
            Assertions.assertEquals(responseAPDU.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);
        }
        final CommandAPDU cmd = new CommandAPDU(
            0xB0, 0x51, 18, 0, new byte[]{0x51, 0x41, 0x42, 0x43, 0x44, 0x45}
        );
        final ResponseAPDU responseAPDU = cardManager.transmit(cmd);
        Assertions.assertNotNull(responseAPDU);
        Assertions.assertEquals(0x9000, responseAPDU.getSW());
        Assertions.assertEquals(responseAPDU.getData()[0], 0);
    }

    @Test
    public void testGetSuccess() throws Exception {
        byte[] value = new byte[] { 0x40, 0x41, 0x42, 0x43, 0x44, 0x45 };
        final CommandAPDU cmdSet = new CommandAPDU(
            0xB0, 0x51, 0X01, 0, value
        );
        final ResponseAPDU responseAPDUSet = cardManager.transmit(cmdSet);
        Assertions.assertNotNull(responseAPDUSet);
        Assertions.assertEquals(0x9000, responseAPDUSet.getSW());
        Assertions.assertEquals(responseAPDUSet.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);

        final CommandAPDU cmdGet = new CommandAPDU(0xB0, 0x50, 0X01, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdGet);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x9000, responseAPDUGet.getSW());
        byte[] result = Arrays.copyOf(responseAPDUGet.getData(), 6);
        Assertions.assertArrayEquals(result, value);
    }

    @Test
    public void testGetFailureDoesNotExist() throws Exception {
        final CommandAPDU cmdGet = new CommandAPDU(0xB0, 0x50, 0X01, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdGet);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x6A80, responseAPDUGet.getSW());
    }

    @Test
    public void testListSuccessEmpty() throws Exception {
        byte[] emptyList = new byte[Configuration.SECRET_MAX_COUNT];
        Arrays.fill(emptyList, (byte) 0x00);
        final CommandAPDU cmdList = new CommandAPDU(0xB0, 0x52, 0, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdList);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x9000, responseAPDUGet.getSW());
        Assertions.assertArrayEquals(responseAPDUGet.getData(), emptyList);
    }

    @Test
    public void testListSuccessOneElement() throws Exception {
        byte[] value = new byte[] { 0x40, 0x41, 0x42, 0x43, 0x44, 0x45 };
        final CommandAPDU cmdSet = new CommandAPDU(
                0xB0, 0x51, 0X01, 0, value
        );
        final ResponseAPDU responseAPDUSet = cardManager.transmit(cmdSet);
        Assertions.assertNotNull(responseAPDUSet);
        Assertions.assertEquals(0x9000, responseAPDUSet.getSW());
        Assertions.assertEquals(responseAPDUSet.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);

        byte[] keyList = new byte[Configuration.SECRET_MAX_COUNT];
        Arrays.fill(keyList, (byte) 0x00);
        keyList[0] = 1;
        final CommandAPDU cmdList = new CommandAPDU(0xB0, 0x52, 0, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdList);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x9000, responseAPDUGet.getSW());
        Assertions.assertArrayEquals(responseAPDUGet.getData(), keyList);
    }

    @Test
    public void testListSuccessMultipleElements() throws Exception {
        byte[] keyList = new byte[Configuration.SECRET_MAX_COUNT];
        for (int i = 1; i <= Configuration.SECRET_MAX_COUNT; i++) {
            keyList[i - 1] = (byte) i;
            byte[] value = new byte[]{0x40, 0x41, 0x42, 0x43, 0x44, 0x45};
            final CommandAPDU cmdSet = new CommandAPDU(
                    0xB0, 0x51, i, 0, value
            );
            final ResponseAPDU responseAPDUSet = cardManager.transmit(cmdSet);
            Assertions.assertNotNull(responseAPDUSet);
            Assertions.assertEquals(0x9000, responseAPDUSet.getSW());
            Assertions.assertEquals(responseAPDUSet.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);
        }

        final CommandAPDU cmdList = new CommandAPDU(0xB0, 0x52, 0, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdList);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x9000, responseAPDUGet.getSW());
        Assertions.assertArrayEquals(responseAPDUGet.getData(), keyList);
    }

    @Test
    public void testDeleteSuccess() throws Exception {
        byte[] value = new byte[] { 0x40, 0x41, 0x42, 0x43, 0x44, 0x45 };
        final CommandAPDU cmdSet = new CommandAPDU(
                0xB0, 0x51, 0X01, 0, value
        );
        final ResponseAPDU responseAPDUSet = cardManager.transmit(cmdSet);
        Assertions.assertNotNull(responseAPDUSet);
        Assertions.assertEquals(0x9000, responseAPDUSet.getSW());
        Assertions.assertEquals(responseAPDUSet.getData()[0], Configuration.SECRET_VALUE_MAX_LENGTH);

        final CommandAPDU cmdDelete = new CommandAPDU(0xB0, 0x54, 0x01, 0);
        final ResponseAPDU responseAPDUDel = cardManager.transmit(cmdDelete);
        Assertions.assertNotNull(responseAPDUDel);
        Assertions.assertEquals(0x9000, responseAPDUDel.getSW());
        Assertions.assertEquals(responseAPDUDel.getData()[0], 1);

        final CommandAPDU cmdGet = new CommandAPDU(0xB0, 0x50, 0X01, 0);
        final ResponseAPDU responseAPDUGet = cardManager.transmit(cmdGet);
        Assertions.assertNotNull(responseAPDUGet);
        Assertions.assertEquals(0x6A80, responseAPDUGet.getSW());
    }

    @Test
    public void testDeleteFailure() throws Exception {
        final CommandAPDU cmdDelete = new CommandAPDU(0xB0, 0x54, 0x01, 0);
        final ResponseAPDU responseAPDUDel = cardManager.transmit(cmdDelete);
        Assertions.assertNotNull(responseAPDUDel);
        Assertions.assertEquals(0x9000, responseAPDUDel.getSW());
        Assertions.assertEquals(responseAPDUDel.getData()[0], 0);
    }
}
