package src.main.java.PackerUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by Siyao on 16/5/18.
 */
public class PackerTimer extends TimerTask{
    private String iPath;
    private String oPath;

    private String packSuffix = ".log";      //文件后缀
    private String packDateFormat = "yyyy-MM-dd";     //日期格式
    private boolean bReserve = false;   //是否保留未压缩的文件
    private boolean bUnpack = false;    //是否需要解压缩
    private boolean bEncryptIt = true; //是否加密

    private Timer packTimer;

    private TimeUnit packTimeUnit = TimeUnit.DAYS;
    private int packPeriod = 1;
    private TimeUnit delayTimeUnit = TimeUnit.DAYS;
    private int packDelay = 0;

    public PackerTimer(String iPath, String oPath){
        File directory = new File(oPath);      //判断输出路径是否存在
        if(!directory.exists()&&!directory.isDirectory()){
            directory.mkdir();
        }
        this.iPath = iPath;
        this.oPath = oPath;

        packTimer = new Timer();
    }

    //getter and setter

    public String getPackSuffix() {
        return packSuffix;
    }

    public void setPackSuffix(String packSuffix) {
        this.packSuffix = packSuffix;
    }

    public String getPackDateFormat() {
        return packDateFormat;
    }

    public void setPackDateFormat(String packDateFormat) {
        this.packDateFormat = packDateFormat;
    }

    public boolean isbReserve() {
        return bReserve;
    }

    public void setbReserve(boolean bReserve) {
        this.bReserve = bReserve;
    }

    public boolean isbUnpack() {
        return bUnpack;
    }

    public void setbUnpack(boolean bUnpack) {
        this.bUnpack = bUnpack;
    }

    public boolean isbEncryptIt() {
        return bEncryptIt;
    }

    public void setbEncryptIt(boolean bEncryptIt) {
        this.bEncryptIt = bEncryptIt;
    }

    public void setInterval(int packPeriod, TimeUnit packTimeUnit){
        this.packPeriod = packPeriod;
        this.packTimeUnit = packTimeUnit;
    }

    public void setDelay(int packDelay, TimeUnit delayTimeUnit){
        this.packDelay = packDelay;
        this.delayTimeUnit = delayTimeUnit;
    }

    @Override
    public void run() {
        //日期获取
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(packDateFormat);
        String strDate = formatter.format(date);

        File[] files;

        if(bUnpack == true) {
            files = new File(iPath).listFiles();
            for(int i=0; i<files.length; i++) {
                Unpacker.unZip(iPath+files[i].getName(),iPath);
                if(bReserve == false){
                    files[i].delete();
                }
            }
        }

        Packer packer = new Packer(iPath, oPath + "/" + strDate + "test.zip");

        try {
            packer.packupSuffix(packSuffix,bEncryptIt);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(bReserve == false){
            files = new File(iPath).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(packSuffix);
                }
            });
            for(int i=0; i<files.length; i++){
                files[i].delete();
            }
        }
    }

    public void start() {
        packTimer.schedule(this,delayTimeUnit.toMillis(packDelay),packTimeUnit.toMillis(packPeriod));
    }

    public void stop() {
        packTimer.cancel();
    }

    public static void main(String[] args) {
        PackerTimer packerTimer = new PackerTimer("/Users/mac/Documents/test","/Users/mac/Documents/testto");
        packerTimer.setInterval(1,TimeUnit.MINUTES);
        packerTimer.setPackDateFormat("yyyy-MM-dd mm");
        packerTimer.setbEncryptIt(false);
        packerTimer.start();
    }
}
