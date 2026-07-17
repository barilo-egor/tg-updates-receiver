package tgb.cryptoexchange.tgupdatesreceiver.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.support.ProducerListener;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
public class UpdatesProducerListener implements ProducerListener<String, Update> {
    @Override
    public void onSuccess(ProducerRecord<String, Update> producerRecord, @NonNull RecordMetadata recordMetadata) {
        log.debug("Успешно отправлен апдейт с username={}.", producerRecord.key());
    }

    @Override
    public void onError(ProducerRecord<String, Update> producerRecord,
                        @Nullable RecordMetadata recordMetadata, @NonNull Exception exception) {
        log.error("Ошибка отправки апдейта с username={}, update={}: {}", producerRecord.key(),
                producerRecord.value().toString(), exception.getMessage(), exception);
    }
}
