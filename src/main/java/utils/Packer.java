package utils;

import java.io.*;
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

    public void packupSuffix(String suffix) throws IOException {
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

    public static void main(String[] args) throws IOException {
        Packer packer = new Packer("/Users/huangli/Documents/IntelliJ/software-reuse-group/doc", "/Users/huangli/Documents/IntelliJ/software-reuse-group/doc/test.zip");
        packer.packupSuffix(".docx");
    }

}
