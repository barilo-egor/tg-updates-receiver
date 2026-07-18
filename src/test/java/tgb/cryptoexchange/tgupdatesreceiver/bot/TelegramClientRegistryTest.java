package tgb.cryptoexchange.tgupdatesreceiver.bot;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.TelegramUrl;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelegramClientRegistryTest {

    @Mock
    private BotsConfiguration botsConfiguration;

    @ValueSource(strings = {
            "donaldbot,mariobot,moneybot",
            "rubbot"
    })
    @ParameterizedTest
    void getShouldReturnTelegramClient(String usernamesString) {
        List<BotsConfiguration.BotConfig> botConfigs = new ArrayList<>();
        List<String> usernames = Arrays.asList(usernamesString.split(","));
        for (String username : usernames) {
            BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
            when(botConfig.username()).thenReturn(username);
            when(botConfig.token()).thenReturn("token");
            botConfigs.add(botConfig);
        }
        when(botsConfiguration.configs()).thenReturn(botConfigs);
        TelegramClientRegistry telegramClientRegistry =  new TelegramClientRegistry(botsConfiguration,
                mock(OkHttpClient.class), TelegramUrl.DEFAULT_URL);
        assertEquals(usernames.size(), botConfigs.size());
        for (String username : usernames) {
            assertNotNull(telegramClientRegistry.get(username));
        }
    }

}