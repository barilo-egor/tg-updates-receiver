package tgb.cryptoexchange.tgupdatesreceiver.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.api.objects.Update;
import tgb.cryptoexchange.tgupdatesreceiver.kafka.UpdatesProducerListener;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationPropertiesScan("tgb.cryptoexchange.tgupdatesreceiver")
public class SpringConfiguration {

    private final KafkaProperties kafkaProperties;

    public SpringConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, Update> userProducerFactory() {
        Map<String, Object> configProps = kafkaProperties.buildProducerProperties();
        return new DefaultKafkaProducerFactory<>(
                configProps,
                new StringSerializer(),
                new JacksonJsonSerializer<>()
        );
    }

    @Bean
    public KafkaTemplate<String, Update> userKafkaTemplate() {
        KafkaTemplate<String, Update> template = new KafkaTemplate<>(userProducerFactory());
        template.setProducerListener(new UpdatesProducerListener());
        return template;
    }

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService commonVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public TelegramBotsLongPollingApplication telegramBotsLongPollingApplication() {
        return new TelegramBotsLongPollingApplication();
    }

    @Bean(name = "telegramObjectMapper")
    public ObjectMapper telegramObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient(new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS));
    }

    @SuppressWarnings("SameReturnValue")
    @Bean
    @Profile("!test")
    public TelegramUrl telegramUrl() {
        return TelegramUrl.DEFAULT_URL;
    }
}
