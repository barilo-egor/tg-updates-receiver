package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookDeliveryServiceTest {

    @Mock
    private WebhookBotFactory webhookBotFactory;

    @Mock
    private WebhookBotRegistry webhookBotRegistry;

    @InjectMocks
    private WebhookDeliveryService webhookDeliveryService;

    @Test
    void getMethod_shouldReturnWebhook() {
        assertEquals(UpdateDeliveryMethod.WEBHOOK, webhookDeliveryService.getMethod());
    }

    @ValueSource(strings = {
            "donald_btc_bot", "BTC24MONEYBOT"
    })
    @ParameterizedTest
    void start_shouldCallSetWebhook(String username) {
        WebhookBot webhookBot = mock(WebhookBot.class);
        BotConfig botConfig = mock(BotConfig.class);
        when(botConfig.username()).thenReturn(username);
        when(webhookBotFactory.create(botConfig)).thenReturn(webhookBot);
        webhookDeliveryService.start(List.of(botConfig));
        verify(webhookBotFactory).create(botConfig);
        verify(webhookBotRegistry).add(username, webhookBot);
        verify(webhookBot).runSetWebhook();
    }

    @Test
    void start_shouldNotThrowException() {
        when(webhookBotFactory.create(any(BotConfig.class))).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> webhookDeliveryService.start(List.of(mock(BotConfig.class))));
    }

    @Test
    void start_shouldNotAddBotToRegistryIfSetWebhookThrowsException() {
        WebhookBot webhookBot = mock(WebhookBot.class);
        when(webhookBotFactory.create(any(BotConfig.class))).thenReturn(webhookBot);
        doThrow(new RuntimeException()).when(webhookBotRegistry).add(anyString(), any(WebhookBot.class));
        webhookDeliveryService.start(List.of(mock(BotConfig.class)));
        verify(webhookBotRegistry, times(0)).add(anyString(), any(WebhookBot.class));
    }

    @Test
    void shutdown_shouldRunDeleteWebhook() {
        WebhookBot webhookBot = mock(WebhookBot.class);
        when(webhookBotRegistry.getAll()).thenReturn(List.of(webhookBot));
        webhookDeliveryService.shutdown();
        verify(webhookBot).runDeleteWebhook();
    }

    @Test
    void shutdown_shouldNotThrowException() {
        WebhookBot webhookBot = mock(WebhookBot.class);
        doThrow(RuntimeException.class).when(webhookBot).runDeleteWebhook();
        when(webhookBotRegistry.getAll()).thenReturn(List.of(webhookBot));
        assertDoesNotThrow(() -> webhookDeliveryService.shutdown());
    }
}