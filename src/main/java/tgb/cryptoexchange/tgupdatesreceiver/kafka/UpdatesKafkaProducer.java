package tgb.cryptoexchange.tgupdatesreceiver.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class UpdatesKafkaProducer {

    private final KafkaTemplate<String, Update> updateKafkaTemplate;

    public UpdatesKafkaProducer(KafkaTemplate<String, Update> updateKafkaTemplate) {
        this.updateKafkaTemplate = updateKafkaTemplate;
    }

    public void send(String topicName, String username, Update update) {
        try {
            updateKafkaTemplate.send(topicName, username, update);
        } catch (Exception e) {
            log.error("Ошибка отправки апдейта в топик: {}", e.getMessage(), e);
        }
    }
}
