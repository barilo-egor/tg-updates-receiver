package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequestMapping(WebhookController.PATH)
@Slf4j
public class WebhookController {

    public static final String PATH = "/webhook";

    private final WebhookUpdateService webhookUpdateService;

    private final ObjectMapper telegramObjectMapper;

    public WebhookController(WebhookUpdateService webhookUpdateService, ObjectMapper telegramObjectMapper) {
        this.webhookUpdateService = webhookUpdateService;
        this.telegramObjectMapper = telegramObjectMapper;
    }

    @PostMapping("/{username}")
    @ResponseStatus(HttpStatus.OK)
    public void updateReceived(@RequestHeader(name = "X-Telegram-Bot-Api-Secret-Token", required = false) String secret,
                               @PathVariable String username,
                               @RequestBody String updateJson) {
        try {
            log.debug("Поступил апдейт secret={}, botId={}, update={}", secret, username, updateJson);
            webhookUpdateService.consume(username, secret, telegramObjectMapper.readValue(updateJson, Update.class));
        } catch (Exception e) {
            log.error("Ошибка обработки запроса вебхука бота {}, тело запроса: {}", username, e.getMessage());
        }
    }
}
