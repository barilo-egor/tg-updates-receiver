package tgb.cryptoexchange.tgupdatesreceiver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;
import tgb.cryptoexchange.tgupdatesreceiver.config.TelegramSpringConfig;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureRestTestClient
@Import({TelegramSpringConfig.class, TelegramUpdateRoutingIT.Configuration.class})
class TelegramUpdateRoutingIT {

    @Container
    static final KafkaContainer kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka"));

    @BeforeAll
    static void beforeAll() {
        kafkaContainer.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
    }

    @TestConfiguration
    static class Configuration {

        @Bean
        public Properties kafkaProperties() {
            Properties properties = new Properties();
            properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
            properties.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
            properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            return properties;
        }

        @Bean(initMethod = "start", destroyMethod = "stop")
        public WireMockServer withUpdatesWireMockServer() {
            return new WireMockServer(WireMockConfiguration.wireMockConfig()
                    .dynamicPort()
                    .usingFilesUnderClasspath("wiremock_with_updates")
            );
        }
    }

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private BotsConfiguration botsConfiguration;

    @Autowired
    private Properties kafkaProperties;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private ObjectMapper telegramObjectMapper;

    @ParameterizedTest
    @CsvFileSource(resources = "/tg_updates/updates.csv", delimiter = '~')
    void webhookBotsShouldSendUpdateToTopic(String updateJson) throws JsonProcessingException {
        for (BotConfig botConfig : getByMethod(UpdateDeliveryMethod.WEBHOOK)) {
            restTestClient.post().uri("/webhook/" + botConfig.username())
                    .header("X-Telegram-Bot-Api-Secret-Token", botConfig.secret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(updateJson)
                    .exchange()
                    .expectStatus().isOk();
            verify(botConfig, updateJson);
        }
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/tg_updates/updates.csv", delimiter = '~')
    void pollingBotsShouldSendUpdateToTopic(String updateJson) throws JsonProcessingException {
        for (BotConfig botConfig : getByMethod(UpdateDeliveryMethod.POLLING)) {
            StubMapping stubMapping = wireMockServer.stubFor(
                    post(urlMatching("/bot" + botConfig.token() + "/getupdates"))
                            .inScenario("receivedUpdate")
                            .whenScenarioStateIs(Scenario.STARTED)
                            .willSetStateTo("Used")
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withResponseBody(new Body("{\"ok\":true,\"result\":[" + updateJson + "]}")))
            );
            verify(botConfig, updateJson);
            wireMockServer.removeStub(stubMapping);
        }
    }

    private List<BotsConfiguration.BotConfig> getByMethod(UpdateDeliveryMethod method) {
        return botsConfiguration.configs().stream()
                .filter(bot -> method.equals(bot.updateDeliveryMethod()))
                .toList();
    }

    private void verify(BotConfig botConfig, String updateJson) throws JsonProcessingException {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(kafkaProperties)) {
            consumer.subscribe(Collections.singletonList(botConfig.topic()));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            assertEquals(1, records.count());
            ConsumerRecord<String, String> consumerRecord = records.iterator().next();
            assertEquals(botConfig.username(), consumerRecord.key());
            assertEquals(
                    telegramObjectMapper.readValue(updateJson, Update.class),
                    telegramObjectMapper.readValue(consumerRecord.value(), Update.class)
            );
        }
    }
}
