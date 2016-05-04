package src.main.java.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Siyao on 16/5/1.
 */
class PackageTest {

    public static void main(String[] args) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd mm");
        Calendar cDate = new GregorianCalendar(2015,6,6);
        cDate.setTime(date);
        System.out.println(cDate.get(Calendar.YEAR));
        String strDate = formatter.format(date);
        System.out.println(strDate);

        PackPerDay pack = new PackPerDay("./archive/test","./archive/");

        Timer timer;
        timer = new Timer();
        timer.schedule(pack,0,3000);

//        Unpacker.unZip("./archive/2016-05-02-01-51-17-test.zip","./archive/2016-05-02-01-51-17-test");
    }
}

public class PackPerDay extends TimerTask {

    private String iPath;
    private String oPath;

    public PackPerDay(String iPath, String oPath){
        File directory = new File(oPath);
        if(!directory.exists()&&!directory.isDirectory()){
            directory.mkdir();
        }
        this.iPath = iPath;
        this.oPath = oPath;
    }

    @Override
    public void run() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(date);
        System.out.println(strDate + "Packing......");
        Packer packer = new Packer(iPath, oPath + strDate + "test.zip");
        try {
            packer.packupSuffix(".log");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}