package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;
import tgb.cryptoexchange.tgupdatesreceiver.bot.delivery.DeliveryService;

import java.util.List;

@Component
@Slf4j
public class WebhookDeliveryService implements DeliveryService {

    private final WebhookBotFactory webhookBotFactory;

    private final WebhookBotRegistry webhookBotRegistry;

    public WebhookDeliveryService(WebhookBotFactory webhookBotFactory, WebhookBotRegistry webhookBotRegistry) {
        this.webhookBotFactory = webhookBotFactory;
        this.webhookBotRegistry = webhookBotRegistry;
    }

    @Override
    public UpdateDeliveryMethod getMethod() {
        return UpdateDeliveryMethod.WEBHOOK;
    }

    @Override
    public void start(List<? extends BotConfig> botConfig) {
        botConfig.forEach(config -> {
            try {
                log.debug("Запуск вебхук бота {}. {}", config.username(), config);
                WebhookBot webhookBot = webhookBotFactory.create(config);
                webhookBot.runSetWebhook();
                webhookBotRegistry.add(config.username(), webhookBot);
            } catch (Exception e) {
                log.error("Ошибка при попытке запуска бота {}: {}", config.username(), e.getMessage(), e);
            }
        });
    }

    @Override
    public void shutdown() {
        webhookBotRegistry.getAll().forEach(bot -> {
            try {
                bot.runDeleteWebhook();
            } catch (Exception e) {
                log.error("Ошибка при попытке остановить вебхук бот(botPath={}): {}", bot.getBotPath(), e.getMessage(), e);
            }
        });
    }
}
