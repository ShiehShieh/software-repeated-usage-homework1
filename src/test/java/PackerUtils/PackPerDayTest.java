package src.test.java.PackerUtils;

import org.junit.Before;
import org.junit.Test;
import src.main.java.PackerUtils.PackPerDay;

import java.util.Timer;

/**
 * Created by Siyao on 16/5/8.
 */
public class PackPerDayTest {
    PackPerDay packer;

    @Before
    public void setUp() throws Exception {
        packer = new PackPerDay("./log/client/Msg/","./archive/day/");
    }


    @Test
    public void run() throws Exception {
        packer.run();
    }

    @Test
    public void runTimer() throws Exception {
        Timer timer;
        timer = new Timer();
        timer.schedule(packer,0,3000);
    }

}