package mvc.Helpers;

import java.security.SecureRandom;
import java.sql.Blob;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.tika.Tika;

import Models.Chats.Chat;

import java.sql.SQLException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import mvc.FileType;

public class Helpers {
    // TODO: The escape String logic is not working properly, need to fix it
    public static String escapeString(String str) {
        if (str == null) {
            return null;
        }
        return str.replace("'", "''")
                .replace("&", "&amp;")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String escapeJS(String str) {
        if (str == null)
            return "";
        return str
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public static String generateOrderRefNo(Date date, String userId) {
        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String formattedDate = formatter.format(date);

        return "ORD-" + formattedDate + "-" + userId;
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public static FileType getFileTypeFromBytes(byte[] file) {
        Tika tika = new Tika();
        String contentType = tika.detect(file);
        return FileType.fromContentType(contentType);
    }

    public static byte[] convertToByte(Blob blob) throws Exception {
        if (blob == null) {
            return null;
        }
        byte[] bytes = blob.getBytes(1, (int) blob.length());
        return bytes;
    }

    public static byte[] convertToByte2(Blob blob) throws SQLException, IOException {
        if (blob == null)
            return null;
        try (InputStream in = blob.getBinaryStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        }
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || "".equals(str);
    }

    public static boolean promptChatBot(Chat chat) {
        return true;
    }

    public static String hashPassword(String password) {
        return PasswordHasher.hashPassword(password);
    }

    public static boolean verifyPassword(String password, String hashedPassword) {
        return PasswordHasher.verifyPassword(password, hashedPassword);
    }

    // IDK MAN IT JUST WORKS SO I PASTE IT
    private class PasswordHasher {
        private static final String ALGORITHM = System.getenv("HASH_ALGORITHM");
        private static final int ITERATION = Integer.parseInt(System.getenv("HASH_ITERATION"));
        private static final int KEY_LENGTH = Integer.parseInt(System.getenv("HASH_KEY_LENGTH")); // bits
        private static final String SECRET_KEY = System.getenv("HASH_SECRET_KEY");

        // Hash the password with a generated salt
        public static String hashPassword(String password) {
            byte[] salt = generateSalt();
            byte[] hash = hash((password + SECRET_KEY).toCharArray(), salt);

            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        }

        // Verify by re-hashing with extracted salt and same secret key (pepper)
        public static boolean verifyPassword(String password, String hashedPassword) {
            String[] parts = hashedPassword.split(":");
            if (parts.length != 2)
                return false;

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] originalHash = Base64.getDecoder().decode(parts[1]);
            byte[] newHash = hash((password + SECRET_KEY).toCharArray(), salt);

            return java.util.Arrays.equals(originalHash, newHash);
        }

        // Internal hash logic
        private static byte[] hash(char[] password, byte[] salt) {
            try {
                PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATION, KEY_LENGTH);
                SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
                return factory.generateSecret(spec).getEncoded();
            } catch (Exception e) {
                throw new RuntimeException("Error while hashing password", e);
            }
        }

        // Generate secure random salt
        private static byte[] generateSalt() {
            byte[] salt = new byte[16]; // 128-bit salt
            new SecureRandom().nextBytes(salt);
            return salt;
        }
    }

    // private class BinListLookup {
    // private static final String BINLIST_BASE_URL =
    // System.getenv("BINLIST_BASE_URL");
    // private OkHttpClient httpClient = HttpByPassSSLCertificate.getUnsafeClient();
    // private String bin = "00000000";
    // private AccountDA accountDA = new AccountDA();

    // public BinListLookup(String bin) {
    // this.bin = bin;
    // }

    // public BankType toBankType() {
    // Request request = new Request.Builder()
    // .url(BINLIST_BASE_URL + "/" + bin)
    // .build();

    // try (Response response = httpClient.newCall(request).execute()) {
    // if (response.isSuccessful()) {
    // String json = response.body().toString();

    // } else {
    // String json = response.body().toString();
    // System.out.println("Error: " + json);
    // return null;
    // }

    // } catch (IOException e) {
    // return null;
    // }

    // }
    // }
}
