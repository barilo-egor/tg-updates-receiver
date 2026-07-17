package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebhookBotRegistry {

    private final Map<String, WebhookBot> botMap = new ConcurrentHashMap<>();

    public void add(String username, WebhookBot bot) {
        if (Objects.isNull(username) || Objects.isNull(bot)) {
            throw new IllegalArgumentException("username и botId не могут быть null.");
        }
        WebhookBot webhookBot = botMap.putIfAbsent(username, bot);
        if (Objects.nonNull(webhookBot)) {
            log.warn("Бот с username={} уже был добавлен в реестр веб хук ботов, повторная попытка проигнорирована.", username);
        } else {
            log.info("Вебхук бот с username={} успешно добавлен в реестр.", username);
        }
    }

    public Optional<WebhookBot> get(String username) {
        if (Objects.isNull(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(botMap.get(username));
    }

    public Collection<WebhookBot> getAll() {
        return Collections.unmodifiableCollection(botMap.values());
    }
}
