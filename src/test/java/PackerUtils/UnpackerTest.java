package src.test.java.PackerUtils;

import org.junit.Before;
import org.junit.Test;
import src.main.java.PackerUtils.Unpacker;

/**
 * Created by Siyao on 16/5/8.
 */
public class UnpackerTest {

    @Test
    public void packupSuffix() throws Exception {
        Unpacker.unZip("./log/client/Msg.zip","./log/client/Msg");
    }
}
