package License;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by shieh on 4/13/16.
 */
public class LicenseTest {

    License license;

    @Before
    public void setUp() throws Exception {
        license = new License(2,10,0,1000);
    }

    @Test
    public void increaseMsg() throws Exception {
        int i = 0;

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 8; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());
    }

    @Test
    public void reset() throws Exception {
        int i = 0;

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 8; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());

        license.reset();

        System.out.println(license.checkMsgInSecond());
        System.out.println(license.checkTotalMsg());
    }

    @Test
    public void checkMsgInSecond() throws Exception {
        int i = 0;

        System.out.println(license.checkMsgInSecond());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());

        for (i = 0; i < 8; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());
    }

    @Test
    public void checkTotalMsg() throws Exception {
        int i = 0;

        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 8; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkTotalMsg());
    }

    @Test
    public void testsetPerSec() {

        int i = 0;

        System.out.println(license.checkMsgInSecond());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());

        license.setPerSec(1);

        i = 0;

        System.out.println(license.checkMsgInSecond());

        for (i = 0; i < 2; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkMsgInSecond());

    }

    @Test
    public void testsetMAX() {

        int i = 0;

        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 3; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkTotalMsg());

        license.setMAX(1);

        i = 0;

        System.out.println(license.checkTotalMsg());

        for (i = 0; i < 2; ++i) {
            license.increaseMsg();
        }

        System.out.println(license.checkTotalMsg());

    }
}