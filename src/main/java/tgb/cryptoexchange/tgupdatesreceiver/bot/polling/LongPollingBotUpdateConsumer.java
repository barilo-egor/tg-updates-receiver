package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class LongPollingBotUpdateConsumer implements LongPollingUpdateConsumer {

    private final String username;

    @Getter
    private final BlockingQueue<Update> queue;

    public LongPollingBotUpdateConsumer(String username, Integer capacity) {
        this.username = username;
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    @Override
    public void consume(List<Update> updates) {
        for (Update update : updates) {
            if (!queue.offer(update)) {
                log.error("Очередь переполнена, апдейт id={} username={} пропущен.", update.getUpdateId(), username);
            }
        }
    }
}
