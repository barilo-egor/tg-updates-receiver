package tgb.cryptoexchange.tgupdatesreceiver.bot;

import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramUrl;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramClientRegistry {

    private final Map<String, OkHttpTelegramClient> telegramClientMap = new HashMap<>();

    public TelegramClientRegistry(BotsConfiguration botsConfiguration, OkHttpClient okHttpClient, TelegramUrl telegramUrl) {
        for (BotsConfiguration.BotConfig botConfig : botsConfiguration.configs()) {
            telegramClientMap.put(botConfig.username(), new OkHttpTelegramClient(okHttpClient, botConfig.token(), telegramUrl));
        }
    }

    public OkHttpTelegramClient get(String username) {
        return telegramClientMap.get(username);
    }
}
