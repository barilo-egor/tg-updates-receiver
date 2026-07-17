package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.TelegramUrl;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;
import tgb.cryptoexchange.tgupdatesreceiver.config.AppConfiguration;

@Service
public class WebhookBotFactory {

    private final UpdateConsumer updateConsumer;

    private final String url;

    private final OkHttpClient okHttpClient;

    private final TelegramUrl telegramUrl;

    public WebhookBotFactory(UpdateConsumer updateConsumer, AppConfiguration appConfiguration,
                             OkHttpClient okHttpClient, TelegramUrl telegramUrl) {
        this.updateConsumer = updateConsumer;
        this.url = appConfiguration.url();
        this.okHttpClient = okHttpClient;
        this.telegramUrl = telegramUrl;
    }

    public WebhookBot create(BotConfig botConfig) {
        return new WebhookBot(url, botConfig, updateConsumer,
                new OkHttpTelegramClient(okHttpClient, botConfig.token(), telegramUrl)
        );
    }
}
