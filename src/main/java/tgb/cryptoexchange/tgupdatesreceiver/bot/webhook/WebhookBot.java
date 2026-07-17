package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.TelegramWebhookBot;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;

/**
 * Представление вебхук телеграм бота.
 */
@Slf4j
public class WebhookBot implements TelegramWebhookBot {

    private final String botPath;

    private final String url;

    private final BotConfig botConfig;

    private final TelegramClient telegramClient;

    private final UpdateConsumer updateConsumer;

    /**
     * Конструктор для создания вебхук бота.
     * @param url URL хоста
     * @param botConfig конфигурация бота
     * @param updateConsumer потребитель апдейтов
     * @param okHttpTelegramClient клиент для выполнения запросов к Telegram API
     */
    public WebhookBot(String url, BotConfig botConfig, UpdateConsumer updateConsumer,
                      OkHttpTelegramClient okHttpTelegramClient) {
        this.botPath = Paths.get(WebhookController.PATH, botConfig.username())
                .toString()
                .replace('\\', '/');
        this.url = url + botPath;
        this.botConfig = botConfig;
        this.telegramClient = okHttpTelegramClient;
        this.updateConsumer = updateConsumer;
    }

    @Override
    public void runDeleteWebhook() {
        try {
            DeleteWebhook deleteWebhook = DeleteWebhook.builder()
                    .dropPendingUpdates(botConfig.dropPendingUpdatesOnShutdown())
                    .build();
            telegramClient.execute(deleteWebhook);
        } catch (Exception e) {
            log.error("Ошибка при попытке удаления вебхука для бота {}: {}", botConfig.username(), e.getMessage(), e);
        }
    }

    @Override
    public void runSetWebhook() {
        try {
            log.info("Установка вебхука бота: {}", botConfig.toString());
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(url)
                    .maxConnections(botConfig.maxConnections())
                    .allowedUpdates(botConfig.allowedUpdates())
                    .ipAddress(botConfig.ip().getHostAddress())
                    .dropPendingUpdates(botConfig.dropPendingUpdatesOnStart())
                    .secretToken(botConfig.secret())
                    .build();
            telegramClient.execute(setWebhook);
            log.debug("Вебхук бота {} успешно установлен.", botConfig.username());
        } catch (Exception e) {
            log.error("Ошибка при попытке установки вебхука для бота (config={}): {}", botConfig, e.getMessage(), e);
        }
    }

    @Override
    public BotApiMethod<?> consumeUpdate(Update update) {
        updateConsumer.consume(botConfig, update);
        return null;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }

    /**
     * Метод проверки секрета. Время выполнения константно.
     * @param secret секрет, который необходимо проверить
     * @return true если секрет валиден, false если нет
     */
    public boolean isValidSecret(String secret) {
        if (botConfig.secret() == null || secret == null) {
            return false;
        }
        return MessageDigest.isEqual(
                botConfig.secret().getBytes(StandardCharsets.UTF_8),
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
}
