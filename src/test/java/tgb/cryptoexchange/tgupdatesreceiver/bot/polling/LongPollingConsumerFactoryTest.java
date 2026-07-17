package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.config.PollingConfiguration;

import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LongPollingConsumerFactoryTest {

    @Mock
    private ExecutorService commonVirtualThreadExecutor;

    @Mock
    private PollingConfiguration pollingConfiguration;

    @InjectMocks
    private LongPollingConsumerFactory longPollingConsumerFactory;

    @CsvSource({
            "donaldbtcbot,10",
            "BTCMONEY24bot,100"
    })
    @ParameterizedTest
    void create_shouldCreateLongPollingConsumerAndStartWorker(String username, Integer capacity) {
        PollingConfiguration.Queue queue = mock(PollingConfiguration.Queue.class);
        when(queue.capacity()).thenReturn(capacity);
        when(pollingConfiguration.queue()).thenReturn(queue);
        BotConfig botConfig = mock(BotConfig.class);
        when(botConfig.username()).thenReturn(username);
        LongPollingBotUpdateConsumer longPollingBotUpdateConsumer = longPollingConsumerFactory.create(botConfig);
        verify(commonVirtualThreadExecutor).submit(any(UpdateSendWorker.class));
        assertEquals(capacity, longPollingBotUpdateConsumer.getQueue().remainingCapacity());
    }

}