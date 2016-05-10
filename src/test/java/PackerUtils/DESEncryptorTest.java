package PackerUtils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 5/10/16.
 */
public class DESEncryptorTest {
    DESEncryptor td;

    @Before
    public void setUp() throws Exception {
        td = new DESEncryptor();
    }

    @Test
    public void encrypt() throws Exception {
        td.encrypt("application.conf", "application2.conf"); //加密
        td.decrypt("application2.conf", "application3.conf"); //解密
    }
}