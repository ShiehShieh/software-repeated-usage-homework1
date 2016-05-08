package src.main.java.PackerUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Siyao on 16/5/1.
 */

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