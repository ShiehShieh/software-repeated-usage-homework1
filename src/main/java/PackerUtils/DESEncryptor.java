package PackerUtils;

import org.apache.commons.codec.DecoderException;

import static org.apache.commons.codec.binary.Hex.*;
import static org.apache.commons.io.FileUtils.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by shieh on 5/6/16.
 */
public class DESEncryptor {
    SecretKey key;
    public DESEncryptor() throws NoSuchAlgorithmException {
        this.key = generateKey();//生成密匙
    }

    public DESEncryptor(SecretKey ikey) throws NoSuchAlgorithmException {
        this.key = ikey;
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException
    {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56); // 128 default; 192 and 256 also possible
        return keyGenerator.generateKey();
    }

    public static void saveKey(SecretKey key, File file) throws IOException
    {
        char[] hex = encodeHex(key.getEncoded());
        writeStringToFile(file, String.valueOf(hex));
    }

    public static SecretKey loadKey(File file) throws IOException
    {
        String data = new String(readFileToByteArray(file));
        byte[] encoded;
        try {
            encoded = decodeHex(data.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }
        return new SecretKeySpec(encoded, "DES");
    }

    /**
     * 文件file进行加密并保存目标文件destFile中
     *
     * @param file   要加密的文件 如c:/test/srcFile.txt
     * @param destFile 加密后存放的文件名 如c:/加密后文件.txt
     */
    public void encrypt(String file, String destFile) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        // cipher.init(Cipher.ENCRYPT_MODE, getKey());
        cipher.init(Cipher.ENCRYPT_MODE, this.key);
        InputStream is = new FileInputStream(file);
        File tempfile = File.createTempFile("temp-file-name", ".tmp");
        OutputStream out = new FileOutputStream(tempfile);
        CipherInputStream cis = new CipherInputStream(is, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = cis.read(buffer)) > 0) {
            out.write(buffer, 0, r);
        }
        cis.close();
        is.close();
        out.close();

        Path from = tempfile.toPath(); //convert from File to Path
        Path to = Paths.get(destFile); //convert from String to Path
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
    /**
     * 文件采用DES算法解密文件
     *
     * @param file 已加密的文件 如c:/加密后文件.txt
     *         * @param destFile
     *         解密后存放的文件名 如c:/ test/解密后文件.txt
     */
    public void decrypt(String file, String dest) throws Exception {
        Cipher cipher = Cipher.getInstance("DES");
        cipher.init(Cipher.DECRYPT_MODE, this.key);
        InputStream is = new FileInputStream(file);
        File tempfile = File.createTempFile("temp-file-name", ".tmp");
        OutputStream out = new FileOutputStream(tempfile);
        CipherOutputStream cos = new CipherOutputStream(out, cipher);
        byte[] buffer = new byte[1024];
        int r;
        while ((r = is.read(buffer)) >= 0) {
            System.out.println();
            cos.write(buffer, 0, r);
        }
        cos.close();
        out.close();
        is.close();

        Path from = tempfile.toPath(); //convert from File to Path
        Path to = Paths.get(dest); //convert from String to Path
        Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
    }
}