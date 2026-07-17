package tgb.cryptoexchange.tgupdatesreceiver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotsInitializer;
import tgb.cryptoexchange.tgupdatesreceiver.bot.webhook.WebhookBotRegistry;
import tgb.cryptoexchange.tgupdatesreceiver.config.TelegramSpringConfig;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TelegramSpringConfig.class)
class InitializeBotsIT {

    @Autowired
    private WebhookBotRegistry webhookBotRegistry;

    @Autowired
    private WireMockServer wireMockServer;

    @Autowired
    private BotsInitializer botsInitializer;

    @Test
    @DisplayName("WEBHOOK боты должны отправить setWebhook, POLLING боты должны начать отправлять getupdates.")
    void shouldStartBots() {
        assertEquals(3, webhookBotRegistry.getAll().size());
        assertAll(
                () -> assertTrue(Objects.nonNull(webhookBotRegistry.get("testtgbupdatereceivebot"))),
                () -> assertTrue(Objects.nonNull(webhookBotRegistry.get("webhooktest1bot"))),
                () -> assertTrue(Objects.nonNull(webhookBotRegistry.get("WebHOOK_TEST_BOT")))
        );
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot9876543210:ZYXwvuTsrQPonMlKjIHgFedCBA987654321/setWebhook")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot123456789:AAHdfg_89DfjklSDF9034_kljDFG89Acls/setWebhook")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot5566778899:XYZ-abc123_DEF456_GHI789_jklMNOpqr/setWebhook")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot987654321:ABCdefGHIjklMNOpqrSTUvwxYZ123456789/getupdates")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot2048591733:AAFlk39_dKjLMs0923_lkFDMK3k239KDFms/getupdates")));
                });
    }

    @Test
    @DisplayName("WEBHOOK боты должны отправит deleteWebhook, POLLING боты должны перестать слать getupdates.")
    void shouldDeleteWebhooksOnShutdown() {
        botsInitializer.stop();
        await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot9876543210:ZYXwvuTsrQPonMlKjIHgFedCBA987654321/deleteWebhook")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot123456789:AAHdfg_89DfjklSDF9034_kljDFG89Acls/deleteWebhook")));
                    wireMockServer.verify(postRequestedFor(urlMatching("/bot5566778899:XYZ-abc123_DEF456_GHI789_jklMNOpqr/deleteWebhook")));
                });
        int initialCount = wireMockServer.findAll(postRequestedFor(urlMatching("/bot.*/getupdates"))).size();
        await()
                .atMost(1, TimeUnit.SECONDS)
                .during(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> wireMockServer.verify(WireMock.exactly(initialCount),
                        postRequestedFor(urlMatching("/bot.*/getupdates"))));
    }
}
