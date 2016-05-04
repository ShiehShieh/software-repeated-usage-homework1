package PackerUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

/**
 * Created by Siyao on 16/5/2.
 */
public class PackPerWeek extends TimerTask{
    private String iPath;
    private String oPath;

    public PackPerWeek(String iPath, String oPath){
        File directory = new File(oPath);
        if(!directory.exists()&&!directory.isDirectory()){
            directory.mkdir();
        }

        this.iPath = iPath;
        this.oPath = oPath;
    }

    public static void main(String[] args) {
        String iPa = "./archive/day/";
        File directory = new File(iPa);
        if(!directory.exists()&&!directory.isDirectory()){
            directory.mkdir();
            //System.out.println("directory");
        }
//        PackPerWeek pack = new PackPerWeek("./archive/day/","./archive/week/");
//        pack.run();
    }

        @Override
    public void run() {
        File[] files = new File(iPath).listFiles();
        for(int i=0; i<files.length; i++){
            Unpacker.unZip(iPath+files[i].getName(),iPath);
            files[i].delete();
        }

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = formatter.format(date);
        System.out.println(strDate + "Packing......");
        Packer packer = new Packer(iPath, oPath + strDate + "test.zip");
        files = new File(iPath).listFiles();
        try {
            packer.packupSuffix(".log");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for(int i=0; i<files.length; i++){
            Unpacker.unZip(iPath+files[i].getName(),iPath);
            files[i].delete();
        }
    }
}
