package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSendWorkerTest {

    @Mock
    private UpdateConsumer updateConsumer;

    @Test
    void runShouldSendUpdates() throws InterruptedException {
        BotConfig botConfig = mock(BotConfig.class);
        BlockingQueue<Update> queue = new ArrayBlockingQueue<>(10);
        UpdateSendWorker worker = new UpdateSendWorker(updateConsumer, botConfig, queue);
        Thread.ofVirtual().start(worker);
        for (int i = 0; i < 10; i++) {
            queue.put(new Update());
        }
        verify(updateConsumer, timeout(1000)
                .times(10))
                .consume(eq(botConfig), any(Update.class));
    }

    @Test
    void run_shouldEndIfInterruptedExceptionWasThrown() throws InterruptedException {
        BlockingQueue<Update> queue = new LinkedBlockingQueue<>(10);
        UpdateSendWorker worker = new UpdateSendWorker(updateConsumer, mock(BotConfig.class), queue);
        Thread thread = Thread.ofVirtual().start(worker);
        verify(updateConsumer, timeout(1000).times(0)).consume(any(), any());
        thread.interrupt();
        thread.join(1000);
        assertFalse(thread.isAlive());
    }

    @Test
    void run_shouldEndIfThreadInterrupted() throws InterruptedException {
        BlockingQueue<Update> queue = new LinkedBlockingQueue<>(10);
        UpdateSendWorker worker = new UpdateSendWorker(updateConsumer, mock(BotConfig.class), queue);
        for (int i = 0; i < 3; i++) {
            queue.put(new Update());
        }
        doAnswer(invocation -> {
            await().atLeast(3, TimeUnit.SECONDS);
            return null;
        }).when(updateConsumer).consume(any(), any());
        Thread thread = Thread.ofVirtual().start(worker);
        thread.join(1000);
        thread.interrupt();
        thread.join();
        assertFalse(thread.isAlive());
    }

    @Test
    void run_shouldNotThrownExceptionIfSendThrownAnException() throws InterruptedException {
        doThrow(RuntimeException.class).when(updateConsumer).consume(any(), any());
        BlockingQueue<Update> queue = new LinkedBlockingQueue<>(10);
        UpdateSendWorker worker = new UpdateSendWorker(updateConsumer, mock(BotConfig.class), queue);
        Thread thread = Thread.ofVirtual().start(worker);
        queue.put(new Update());
        queue.put(new Update());
        verify(updateConsumer, timeout(1000).times(2)).consume(any(), any());
        thread.interrupt();
        thread.join();
    }

    @Test
    void run_shouldSendAllUpdatesInQueue() throws InterruptedException {
        BlockingQueue<Update> queue = new LinkedBlockingQueue<>(3);
        UpdateSendWorker worker = new UpdateSendWorker(updateConsumer, mock(BotConfig.class), queue);
        for (int i = 0; i < 3; i++) {
            queue.put(new Update());
        }
        Thread thread = Thread.ofVirtual().start(worker);
        await().atMost(2, TimeUnit.SECONDS).until(() -> queue.size() < 3);
        thread.interrupt();
        thread.join();
        assertTrue(queue.isEmpty());
        verify(updateConsumer, times(3)).consume(any(), any());
    }
}