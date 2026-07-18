package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.TelegramClientRegistry;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;
import tgb.cryptoexchange.tgupdatesreceiver.config.AppConfiguration;

@Service
public class WebhookBotFactory {

    private final UpdateConsumer updateConsumer;

    private final String url;

    private final TelegramClientRegistry telegramClientRegistry;

    public WebhookBotFactory(UpdateConsumer updateConsumer, AppConfiguration appConfiguration, TelegramClientRegistry telegramClientRegistry) {
        this.updateConsumer = updateConsumer;
        this.url = appConfiguration.url();
        this.telegramClientRegistry = telegramClientRegistry;
    }

    public WebhookBot create(BotConfig botConfig) {
        return new WebhookBot(url, botConfig, updateConsumer, telegramClientRegistry.get(botConfig.username()));
    }
}
