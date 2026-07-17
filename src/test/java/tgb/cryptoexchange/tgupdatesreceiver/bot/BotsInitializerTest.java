package tgb.cryptoexchange.tgupdatesreceiver.bot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.tgupdatesreceiver.bot.delivery.DeliveryService;
import tgb.cryptoexchange.tgupdatesreceiver.bot.polling.PollingDeliveryService;
import tgb.cryptoexchange.tgupdatesreceiver.bot.webhook.WebhookDeliveryService;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BotsInitializerTest {

    @Mock
    private PollingDeliveryService pollingDeliveryService;

    @Mock
    private WebhookDeliveryService webhookDeliveryService;

    @Mock
    private BotsConfiguration botsConfiguration;

    private BotsInitializer botsInitializer;

    @Captor
    private ArgumentCaptor<List<BotConfig>> pollingBotConfigCaptor;

    @Captor
    private ArgumentCaptor<List<BotConfig>> webhookBotConfigCaptor;

    @Test
    void constructor_shouldThrowExceptionIfMethodIsNull() {
        List<DeliveryService> deliveryServiceList = List.of(new DeliveryService() {
            @Override
            public UpdateDeliveryMethod getMethod() {
                return null;
            }

            @Override
            public void start(List<? extends BotConfig> botConfigs) {
                // test stub
            }

            @Override
            public void shutdown() {
                // test stub
            }
        });
        assertThrows(NullPointerException.class, () -> new BotsInitializer(deliveryServiceList, botsConfiguration));
    }

    @CsvSource({
            "5,10",
            "10,5",
            "10,10",
            "1,1"
    })
    @ParameterizedTest
    void onApplicationReady_shouldStartBots(int webhookBotsSize, int pollingBotsSize) {
        when(webhookDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        when(pollingDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.POLLING);
        this.botsInitializer = new BotsInitializer(List.of(pollingDeliveryService, webhookDeliveryService), botsConfiguration);
        List<BotsConfiguration.BotConfig> botConfigs = new ArrayList<>();
        for (int i = 0; i < webhookBotsSize; i++) {
            BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
            botConfigs.add(botConfig);
            when(botConfig.updateDeliveryMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        }
        for (int i = 0; i < pollingBotsSize; i++) {
            BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
            botConfigs.add(botConfig);
            when(botConfig.updateDeliveryMethod()).thenReturn(UpdateDeliveryMethod.POLLING);
        }
        when(botsConfiguration.configs()).thenReturn(botConfigs);
        botsInitializer.start();
        verify(webhookDeliveryService).start(webhookBotConfigCaptor.capture());
        verify(pollingDeliveryService).start(pollingBotConfigCaptor.capture());
        assertEquals(webhookBotsSize, webhookBotConfigCaptor.getValue().size());
        assertEquals(pollingBotsSize, pollingBotConfigCaptor.getValue().size());
    }

    @CsvSource({
            "5,10",
            "10,5",
            "10,10",
            "1,1"
    })
    @ParameterizedTest
    void onApplicationReady_shouldNotCallStartIfNoServiceForMethod(int webhookBotsSize, int pollingBotsSize) {
        when(webhookDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        this.botsInitializer = new BotsInitializer(List.of(webhookDeliveryService), botsConfiguration);
        List<BotsConfiguration.BotConfig> botConfigs = new ArrayList<>();
        for (int i = 0; i < webhookBotsSize; i++) {
            BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
            botConfigs.add(botConfig);
            when(botConfig.updateDeliveryMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        }
        for (int i = 0; i < pollingBotsSize; i++) {
            BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
            botConfigs.add(botConfig);
            when(botConfig.updateDeliveryMethod()).thenReturn(UpdateDeliveryMethod.POLLING);
        }
        when(botsConfiguration.configs()).thenReturn(botConfigs);
        botsInitializer.start();
        verify(pollingDeliveryService, never()).start(any());
    }

    @Test
    void start_shouldNotThrowException() {
        when(webhookDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        this.botsInitializer = new BotsInitializer(List.of(webhookDeliveryService), botsConfiguration);
        List<BotsConfiguration.BotConfig> botConfigs = new ArrayList<>();
        BotsConfiguration.BotConfig botConfig = mock(BotsConfiguration.BotConfig.class);
        botConfigs.add(botConfig);
        when(botConfig.updateDeliveryMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        when(botsConfiguration.configs()).thenReturn(botConfigs);
        doThrow(RuntimeException.class).when(webhookDeliveryService).start(any());
        assertDoesNotThrow(() -> botsInitializer.start());
    }

    @Test
    void beforeShutdown_shouldCallShutdown() {
        when(webhookDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        when(pollingDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.POLLING);
        this.botsInitializer = new BotsInitializer(List.of(pollingDeliveryService, webhookDeliveryService), botsConfiguration);
        botsInitializer.stop();
        verify(webhookDeliveryService).shutdown();
        verify(pollingDeliveryService).shutdown();
    }

    @Test
    void beforeShutdown_shouldNotThrowException() {
        when(webhookDeliveryService.getMethod()).thenReturn(UpdateDeliveryMethod.WEBHOOK);
        this.botsInitializer = new BotsInitializer(List.of(webhookDeliveryService), botsConfiguration);
        doThrow(RuntimeException.class).when(webhookDeliveryService).shutdown();
        assertDoesNotThrow(() -> botsInitializer.stop());
    }
}