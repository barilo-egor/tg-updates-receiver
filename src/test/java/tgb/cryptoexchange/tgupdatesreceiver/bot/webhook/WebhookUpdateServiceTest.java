package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookUpdateServiceTest {

    @Mock
    private WebhookBotRegistry webhookBotRegistry;

    private WebhookUpdateService webhookUpdateService;

    @BeforeEach
    void setUp() {
        webhookUpdateService = new WebhookUpdateService(webhookBotRegistry);
    }

    @CsvSource({
            "donaldBOT,qj4q4WTgubHiEjTQHnHQDIwQi1AmlQJC,54256",
            "BTC24MONEY,5huJbdE3rekx0kRZ3Qk4y1goKIIC8JMnuPiY8YVqfhEAcvRekk98il0DHGf5l7C6,5425615"
    })
    @ParameterizedTest
    void consume_shouldNotPassUpdateToBotIfSecretNotValid(String username, String secret, Integer updateId) {
        Update update = new Update();
        update.setUpdateId(updateId);
        WebhookBot webhookBot = mock(WebhookBot.class);
        when(webhookBot.isValidSecret(secret)).thenReturn(false);
        when(webhookBotRegistry.get(username)).thenReturn(Optional.of(webhookBot));
        webhookUpdateService.consume(username, secret, update);
        verify(webhookBot, never()).consumeUpdate(any(Update.class));
    }

    @CsvSource({
            "donaldBOT,qj4q4WTgubHiEjTQHnHQDIwQi1AmlQJC,54256",
            "BTC24MONEY,5huJbdE3rekx0kRZ3Qk4y1goKIIC8JMnuPiY8YVqfhEAcvRekk98il0DHGf5l7C6,5425615"
    })
    @ParameterizedTest
    void consume_shouldPassUpdateIfSecretIsValid(String username, String secret, Integer updateId) {
        Update update = new Update();
        update.setUpdateId(updateId);
        WebhookBot webhookBot = mock(WebhookBot.class);
        when(webhookBot.isValidSecret(secret)).thenReturn(true);
        when(webhookBotRegistry.get(username)).thenReturn(Optional.of(webhookBot));
        webhookUpdateService.consume(username, secret, update);
        verify(webhookBot).consumeUpdate(any(Update.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"donaldBOT", "BTC24MONEY"})
    void consume_shouldLogAndDoNothingIfBotNotFoundInRegistry(String username) {
        Update update = new Update();
        update.setUpdateId(1536162);
        when(webhookBotRegistry.get(username)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> webhookUpdateService.consume(username, "secret", update));
    }
}