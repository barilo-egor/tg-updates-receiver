package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.TelegramUrl;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;
import tgb.cryptoexchange.tgupdatesreceiver.config.AppConfiguration;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebhookBotFactoryTest {

    @ValueSource(strings = {
            "donaldBTCBot, mariobot"
    })
    @ParameterizedTest
    void create_shouldCreateWebhookBot(String username) {
        BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
        when(botConfig.username()).thenReturn(username);
        when(botConfig.token()).thenReturn("token");
        UpdateConsumer updateConsumer = mock(UpdateConsumer.class);
        AppConfiguration appConfiguration = mock(AppConfiguration.class);
        OkHttpClient okHttpClient = mock(OkHttpClient.class);
        WebhookBotFactory webhookBotFactory = new WebhookBotFactory(updateConsumer, appConfiguration, okHttpClient, TelegramUrl.DEFAULT_URL);
        WebhookBot actual = webhookBotFactory.create(botConfig);
        assertEquals("/webhook/" + username, actual.getBotPath());
    }
}