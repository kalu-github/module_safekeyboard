package lib.kalu.safekeyboard;

import androidx.annotation.Nullable;

import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SafeKeyboardUtil {

    //算法/模式/填充
    private static final String CIPHER_MODE = "AES/CFB/NoPadding";

    /**
     * 创建密钥
     *
     * @param key 例如："0123456701234567" 128位 16*8 所有密钥长度不能超过16字符中文占两个。192 24；
     *            256 32
     * @return SecretKeySpec 实例
     */
    private static SecretKeySpec createKey(@Nullable String key) {
        SafeKeyboardLogUtil.log("createKey => key = " + key);

        if (null == key) {
            key = "";
        }

        StringBuffer sb = new StringBuffer(16);
        sb.append(key);
        while (sb.length() < 16) {
            sb.append("0");
        }
        if (sb.length() > 16) {
            sb.setLength(16);
        }
        try {
            byte[] bytes = sb.toString().getBytes("UTF-8");
            return new SecretKeySpec(bytes, "AES");
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("createKey => " + e.getMessage());
            byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 'a', 'b', 'c', 'd', 'e', 'f'};
            return new SecretKeySpec(bytes, "AES");
        }
    }

    /**
     * 加密字节数据
     *
     * @param bytes 需要加密的字节数组
     * @return 加密完后的字节数组
     */
    private static byte[] encryptBytes(byte[] bytes, String key, String iv) {

//        if(TextUtils.isEmpty(iv)){
        iv = "9876543210fedcba";
//        }

        try {
            SecretKeySpec keySpec = createKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
//            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] result = cipher.doFinal(bytes);
            SafeKeyboardLogUtil.log("encryptBytes => result = " + result);
            SafeKeyboardLogUtil.log("encryptBytes => resultLength = " + result.length);

            return result;
        } catch (Exception e) {
            SafeKeyboardLogUtil.log("encryptBytes => " + e.getMessage());
            return null;
        }
    }

    /**
     * 加密(结果为16进制字符串)
     *
     * @param code 要加密的字符串
     * @return 加密后的16进制字符串
     */
    public static String encryptCode(int code, String key, String iv) {

        SafeKeyboardLogUtil.log("encryptCode => code = " + code);
        SafeKeyboardLogUtil.log("encryptCode => key = " + key);
        SafeKeyboardLogUtil.log("encryptCode => iv = " + iv);

        try {

            byte[] bytes = String.valueOf(code).getBytes("UTF-8");
            SafeKeyboardLogUtil.log("encryptCode => bytes = " + bytes);
            SafeKeyboardLogUtil.log("encryptCode => bytesLength = " + bytes.length);

            byte[] encrypt = encryptBytes(bytes, key, iv);
            SafeKeyboardLogUtil.log("encryptCode => encrypt = " + encrypt);
            SafeKeyboardLogUtil.log("encryptCode => encryptLength = " + encrypt.length);

            String hex = byte2hex(encrypt);
            SafeKeyboardLogUtil.log("encryptCode => hex = " + hex);

            return hex;

        } catch (Exception e) {
            SafeKeyboardLogUtil.log("encrypt => " + e.getMessage());
            return null;
        }
    }

    /**
     * 解密字节数组
     */
    private static byte[] decrypt(byte[] content, String key, String iv) {
        try {
            SecretKeySpec keySpec = createKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_MODE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
//            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(content);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密16进制的字符串为字符串
     */
    public static String decrypt(String content, String key, String iv) {
        byte[] data = null;
        try {
            data = hex2byte(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        data = decrypt(data, key, iv);
        if (data == null)
            return null;
        String result = null;
        try {
            result = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 字节数组转成16进制字符串
     *
     * @param b 字节
     * @return 16进制字符串
     */
    private static String byte2hex(byte[] b) { // 一个字节的数，
        StringBuffer sb = new StringBuffer(b.length * 2);
        String tmp = "";
        for (int n = 0; n < b.length; n++) {
            // 整数转成十六进制表示
            tmp = (Integer.toHexString(b[n] & 0XFF));
            if (tmp.length() == 1) {
                sb.append("0");
            }
            sb.append(tmp);
        }
        return sb.toString().toUpperCase(); // 转成大写
    }

    /**
     * 将hex字符串转换成字节数组 *
     *
     * @param inputString 16进制的字符串
     * @return 字节数组
     */
    private static byte[] hex2byte(String inputString) {
        if (inputString == null || inputString.length() < 2) {
            return new byte[0];
        }
        inputString = inputString.toLowerCase();
        int l = inputString.length() / 2;
        byte[] result = new byte[l];
        for (int i = 0; i < l; ++i) {
            String tmp = inputString.substring(2 * i, 2 * i + 2);
            result[i] = (byte) (Integer.parseInt(tmp, 16) & 0xFF);
        }
        return result;
    }
}
