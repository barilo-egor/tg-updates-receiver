package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollingDeliveryServiceTest {

    @Mock
    private TelegramBotsLongPollingApplication telegramBotsLongPollingApplication;

    @Mock
    private LongPollingConsumerFactory longPollingConsumerFactory;

    @Mock
    private TelegramUrl telegramUrl;

    @InjectMocks
    private PollingDeliveryService pollingDeliveryService;

    @Captor
    private ArgumentCaptor<Supplier<TelegramUrl>> telegramUrlCaptor;

    @Captor
    private ArgumentCaptor<Function<Integer, GetUpdates>> getUpdatesCaptor;

    @Test
    void getMethod_shouldReturnPolling() {
        assertEquals(UpdateDeliveryMethod.POLLING, pollingDeliveryService.getMethod());
    }

    @CsvSource({
            "message:edited_channel_post:callback_query,40,1234567890:ABCdefGhIJKlmNoPQRsTUVwxyZ123456789,Donaldbtc24Bot,543455",
            "'',40,9876543210:ZYXwvuTsrQPonMlKjIHgFedCBA987654321,BTC24MONEYBOT,12155"
    })
    @ParameterizedTest
    void start_ShouldRegisterWithPassedParametersInBotConfig(String allowedUpdated, Integer maxConnections, String token,
                                                             String username, Integer offset) throws TelegramApiException {
        BotConfig config = mock(BotConfig.class);
        when(config.allowedUpdates()).thenReturn(Arrays.asList(allowedUpdated.split(":")));
        when(config.maxConnections()).thenReturn(maxConnections);
        when(config.username()).thenReturn(username);
        when(config.token()).thenReturn(token);
        LongPollingBotUpdateConsumer consumer = mock(LongPollingBotUpdateConsumer.class);
        when(longPollingConsumerFactory.create(config)).thenReturn(consumer);
        pollingDeliveryService.start(List.of(config));
        verify(telegramBotsLongPollingApplication).registerBot(
                eq(token), telegramUrlCaptor.capture(), getUpdatesCaptor.capture(), any(LongPollingBotUpdateConsumer.class));
        verify(longPollingConsumerFactory).create(config);
        GetUpdates getUpdates = getUpdatesCaptor.getValue().apply(offset);
        assertAll(
                () -> assertEquals(telegramUrl, telegramUrlCaptor.getValue().get()),
                () -> assertEquals(allowedUpdated, String.join(":", getUpdates.getAllowedUpdates())),
                () -> assertEquals(offset + 1, getUpdates.getOffset()),
                () -> assertEquals(maxConnections, getUpdates.getLimit()),
                () -> assertEquals(30, getUpdates.getTimeout())
        );
    }

    @Test
    void start_shouldNotThrowException() {
        BotConfig config = mock(BotConfig.class);
        when(config.token()).thenThrow(new RuntimeException());
        assertDoesNotThrow(() -> pollingDeliveryService.start(List.of(config)));
    }

    @Test
    void shutdown_shouldStopBots() throws TelegramApiException {
        pollingDeliveryService.shutdown();
        verify(telegramBotsLongPollingApplication).stop();
    }

    @Test
    void shutdown_shouldNotThrowException() throws TelegramApiException {
        doThrow(new RuntimeException()).when(telegramBotsLongPollingApplication).stop();
        assertDoesNotThrow(() -> pollingDeliveryService.shutdown());
    }
}