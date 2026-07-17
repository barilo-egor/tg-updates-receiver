package tgb.cryptoexchange.tgupdatesreceiver.kafka;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;

@Service
public class KafkaUpdateConsumer implements UpdateConsumer {

    private final UpdatesKafkaProducer updatesKafkaProducer;

    public KafkaUpdateConsumer(UpdatesKafkaProducer updatesKafkaProducer) {
        this.updatesKafkaProducer = updatesKafkaProducer;
    }

    @Override
    public void consume(BotConfig botConfig, Update update) {
        updatesKafkaProducer.send(botConfig.topic(), botConfig.username(), update);
    }
}
