package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
@Slf4j
public class WebhookUpdateService {

    private final WebhookBotRegistry webhookBotRegistry;

    public WebhookUpdateService(WebhookBotRegistry webhookBotRegistry) {
        this.webhookBotRegistry = webhookBotRegistry;
    }

    @Async
    public void consume(String username, String secret, Update update) {
        webhookBotRegistry.get(username)
                .ifPresentOrElse(bot -> {
                            if (bot.isValidSecret(secret)) {
                                bot.consumeUpdate(update);
                            } else {
                                log.warn("Поступил запрос по вебхуку с невалидным секретом. Бот {}, поступивший секрет {}, {}",
                                        username, secret, update.toString());
                            }
                        },
                        () -> log.error("В метод вебхука поступил апдейт для бота, отсутствующего в реестре. " +
                                "username={}, update={}", username, update.toString()));
    }
}
