package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LongPollingBotUpdateConsumerTest {

    @ValueSource(ints = {10, 50, 100})
    @ParameterizedTest
    void consume_shouldPutUpdateIfQueueHasSpace(Integer capacity) {
        LongPollingBotUpdateConsumer longPollingBotUpdateConsumer = new LongPollingBotUpdateConsumer(
                "username", capacity
        );

        Set<Update> shouldPutUpdates = new HashSet<>();
        for (int i = 0; i < capacity; i++) {
            Update update = new Update();
            update.setUpdateId(i);
            shouldPutUpdates.add(update);
            longPollingBotUpdateConsumer.consume(List.of(update));
        }
        Set<Update> shouldNotPutUpdates = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            Update update = new Update();
            update.setUpdateId(capacity + i);
            shouldNotPutUpdates.add(update);
            longPollingBotUpdateConsumer.consume(List.of(update));
        }
        assertEquals(capacity, longPollingBotUpdateConsumer.getQueue().size());
        for (Update update : shouldPutUpdates) {
            assertTrue(longPollingBotUpdateConsumer.getQueue().contains(update));
        }
        for (Update update : shouldNotPutUpdates) {
            assertFalse(longPollingBotUpdateConsumer.getQueue().contains(update));
        }
    }
}