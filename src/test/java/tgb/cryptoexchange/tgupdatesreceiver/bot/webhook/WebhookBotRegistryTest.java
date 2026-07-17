package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WebhookBotRegistryTest {

    private WebhookBotRegistry webhookBotRegistry;

    @BeforeEach
    void setUp() {
        webhookBotRegistry = new WebhookBotRegistry();
    }

    @Test
    void add_shouldThrowIllegalArgumentExceptionIfUsernameIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> webhookBotRegistry.add(null, mock(WebhookBot.class)),
                "username и botId не могут быть null.");
    }

    @Test
    void add_shouldThrowIllegalArgumentExceptionIfBotIsNull() {
        assertThrows(IllegalArgumentException.class,
                () -> webhookBotRegistry.add("username", null),
                "username и botId не могут быть null.");
    }

    @ValueSource(strings = {
            "donaldbot", "mariobot"
    })
    @ParameterizedTest
    void add_shouldPutBot(String username) {
        WebhookBot webhookBot = mock(WebhookBot.class);
        webhookBotRegistry.add(username, webhookBot);
        assertTrue(webhookBotRegistry.get(username).isPresent());
    }

    @ValueSource(strings = {
            "donaldbot", "mariobot"
    })
    @ParameterizedTest
    void add_shouldNotPutBotIfPresent(String username) {
        WebhookBot webhookBot = mock(WebhookBot.class);
        webhookBotRegistry.add(username, webhookBot);
        webhookBotRegistry.add(username, webhookBot);
        assertEquals(1, webhookBotRegistry.getAll().size());
    }

    @Test
    void get_shouldReturnEmptyIfUsername() {
        assertTrue(webhookBotRegistry.get(null).isEmpty());
    }

    @Test
    void get_shouldReturnEmptyIfNoBot() {
        webhookBotRegistry.add("firstbot", mock(WebhookBot.class));
        webhookBotRegistry.add("secondbot", mock(WebhookBot.class));
        assertTrue(webhookBotRegistry.get("thirdbot").isEmpty());
    }

    @Test
    void getAll_shouldReturnUnmodifiableCollection() {
        webhookBotRegistry.add("firstbot", mock(WebhookBot.class));
        webhookBotRegistry.add("secondbot", mock(WebhookBot.class));
        Collection<WebhookBot> webhookBots = webhookBotRegistry.getAll();
        assertAll(
                () -> assertThrows(UnsupportedOperationException.class, webhookBots::clear),
                () -> assertThrows(UnsupportedOperationException.class, () -> webhookBots.add(mock(WebhookBot.class)))
        );
    }
}