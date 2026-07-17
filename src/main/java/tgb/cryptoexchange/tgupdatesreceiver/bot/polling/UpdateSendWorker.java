package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class UpdateSendWorker implements Runnable {

    private final UpdateConsumer updateConsumer;

    private final BotConfig botConfig;

    private final BlockingQueue<Update> queue;

    public UpdateSendWorker(UpdateConsumer updateConsumer, BotConfig botConfig, BlockingQueue<Update> queue) {
        this.updateConsumer = updateConsumer;
        this.botConfig = botConfig;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Update update = queue.poll(1, TimeUnit.SECONDS);
                if (Objects.nonNull(update)) {
                    updateConsumer.consume(botConfig, update);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Ошибка обработки апдейта бота {}: {}", botConfig.username(), e.getMessage(), e);
            }
        }
        Update remainingUpdate;
        while ((remainingUpdate = queue.poll()) != null) {
            try {
                updateConsumer.consume(botConfig, remainingUpdate);
            } catch (Exception e) {
                log.error("Не удалось обработать апдейт ID={} бота {} при остановке приложения: {}",
                        remainingUpdate.getUpdateId(), botConfig.username(), e.getMessage());
            }
        }
    }
}
