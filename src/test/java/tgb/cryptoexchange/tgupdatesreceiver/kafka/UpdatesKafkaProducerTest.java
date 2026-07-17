package tgb.cryptoexchange.tgupdatesreceiver.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatesKafkaProducerTest {

    @Mock
    private KafkaTemplate<String, Update> updateKafkaTemplate;

    @InjectMocks
    private UpdatesKafkaProducer updatesKafkaProducer;

    @CsvSource({
            "donald-update-receive-v1,donald_bot",
            "money-update-receive-v2,BTC24MONEY_BOT"
    })
    @ParameterizedTest
    void send_shouldSendToTopic(String topicName, String username) {
        Update update = mock(Update.class);
        updatesKafkaProducer.send(topicName, username, update);
        verify(updateKafkaTemplate).send(topicName, username, update);
    }

    @Test
    void send_shouldNotThrowException() {
        doThrow(RuntimeException.class).when(updateKafkaTemplate).send(anyString(), anyString(), any());
        assertDoesNotThrow(() -> updatesKafkaProducer.send("topic", "username", new Update()));
    }
}