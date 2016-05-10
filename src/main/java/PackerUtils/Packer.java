package PackerUtils;

import utils.FileUtils;

import java.io.*;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by shieh on 4/20/16.
 */
public class Packer {
    private String ipath;
    private String opath;

    public Packer(String ipath, String opath) {
        this.ipath = ipath;
        this.opath = opath;
    }

    public void packupSuffix(String suffix, boolean encryptIt) throws Exception {
        File [] files = new File(ipath).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(suffix);
            }
        });

        FileOutputStream fos = new FileOutputStream(opath);
        ZipOutputStream zos = new ZipOutputStream(fos);

        for (File fn : files) {
            System.out.println("Writing '" + fn.getName() + "' to zip file");
            addToZipFile(fn, zos);
        }

        zos.close();
        fos.close();

        if (encryptIt) {
            DESEncryptor td = new DESEncryptor();
            try {
                td.saveKey(td.key, new File(FileUtils.filenameWithoutExt(opath)+".key"));
                td.encrypt(opath, opath); //加密
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void addToZipFile(File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
}


class PackerTimer extends TimerTask {
    private String logFile;

    public PackerTimer(String logFilename) {
        this.logFile = logFilename;
    }

    public void run() {
        return;
    }
}