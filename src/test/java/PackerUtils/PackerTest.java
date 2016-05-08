package src.test.java.PackerUtils;

import org.junit.Before;
import org.junit.Test;
import src.main.java.PackerUtils.Packer;

/**
 * Created by Siyao on 16/5/8.
 */
public class PackerTest {
    Packer packer;

    @Before
    public void setUp() throws Exception {
        packer = new Packer("./log/client/Msg/","./log/client/Msg.zip");
    }

    @Test
    public void packupSuffix() throws Exception {
        packer.packupSuffix("log");
    }
}