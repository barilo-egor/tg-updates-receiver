package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.methods.updates.GetUpdates;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;
import tgb.cryptoexchange.tgupdatesreceiver.bot.delivery.DeliveryService;

import java.util.List;

@Slf4j
@Service
public class PollingDeliveryService implements DeliveryService {

    private final TelegramBotsLongPollingApplication telegramBotsLongPollingApplication;

    private final LongPollingConsumerFactory longPollingConsumerFactory;

    private final TelegramUrl telegramUrl;

    public PollingDeliveryService(TelegramBotsLongPollingApplication telegramBotsLongPollingApplication,
                                  LongPollingConsumerFactory longPollingConsumerFactory, TelegramUrl telegramUrl) {
        this.telegramBotsLongPollingApplication = telegramBotsLongPollingApplication;
        this.longPollingConsumerFactory = longPollingConsumerFactory;
        this.telegramUrl = telegramUrl;
    }

    @Override
    public UpdateDeliveryMethod getMethod() {
        return UpdateDeliveryMethod.POLLING;
    }

    @Override
    public void start(List<? extends BotConfig> botConfigs) {
        botConfigs.forEach(config -> {
            try {
                log.debug("Запуск поллинг бота {}. {}", config.username(), config);
                telegramBotsLongPollingApplication.registerBot(config.token(), () -> telegramUrl,
                        offset -> GetUpdates.builder()
                                .allowedUpdates(config.allowedUpdates())
                                .offset(offset + 1)
                                .limit(config.maxConnections())
                                .timeout(30)
                                .build(), longPollingConsumerFactory.create(config));
                log.debug("Поллинг бот {} успешно запущен.", config.username());
            } catch (Exception e) {
                log.error("Ошибка при попытке запуска бота {}: {}", config.username(), e.getMessage(), e);
            }
        });
    }

    @Override
    public void shutdown() {
        try {
            telegramBotsLongPollingApplication.stop();
            log.debug("POLLING боты остановлены.");
        } catch (Exception e) {
            log.error("Ошибка при попытке остановки Long Polling ботов: {}", e.getMessage(), e);
        }
    }
}
