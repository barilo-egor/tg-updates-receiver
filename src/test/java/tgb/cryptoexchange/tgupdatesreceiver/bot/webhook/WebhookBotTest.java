package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookBotTest {

    @Mock
    private BotsConfiguration.BotConfig botConfig;

    @BeforeEach
    void setUp() {
        when(botConfig.username()).thenReturn("username");
    }

    @ValueSource(booleans = {true, false})
    @ParameterizedTest
    void runDeleteWebhook_ShouldExecute(boolean dropPendingUpdatesOnShutdown) throws TelegramApiException {
        when(botConfig.dropPendingUpdatesOnShutdown()).thenReturn(dropPendingUpdatesOnShutdown);
        OkHttpTelegramClient okHttpClient = mock(OkHttpTelegramClient.class);
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, okHttpClient);
        webhookBot.runDeleteWebhook();
        ArgumentCaptor<DeleteWebhook> deleteWebhookArgumentCaptor = ArgumentCaptor.forClass(DeleteWebhook.class);
        verify(okHttpClient).execute(deleteWebhookArgumentCaptor.capture());
        assertEquals(dropPendingUpdatesOnShutdown, deleteWebhookArgumentCaptor.getValue().getDropPendingUpdates());
    }

    @Test
    void runDeleteWebhook_ShouldNotThrow() throws TelegramApiException {
        when(botConfig.dropPendingUpdatesOnShutdown()).thenReturn(false);
        OkHttpTelegramClient okHttpClient = mock(OkHttpTelegramClient.class);
        doThrow(RuntimeException.class).when(okHttpClient).execute(any(DeleteWebhook.class));
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, okHttpClient);
        assertDoesNotThrow(webhookBot::runDeleteWebhook);
    }

    @CsvSource(value = {
            "donaldbot,http://updates.rcetech.net,40,message:edited_channel_post:callback_query,11.11.11.111,true,9mmI2lz9mNtDN3jMbyCkWJ6ghBY3SpZZ",
            "bulbabot,http://updates.dev.rcetech.net,1,'',222.22.22.22,false,Z2BLKpI6MxFYi0Y8zeWGyCt3vunud7e7"
    })
    @ParameterizedTest
    void runSetWebhook_ShouldExecute(String username, String url, Integer maxConnections, String allowedUpdates, String ip,
                                     Boolean dropPendingUpdatesOnStart, String secret) throws UnknownHostException, TelegramApiException {
        when(botConfig.username()).thenReturn(username);
        when(botConfig.maxConnections()).thenReturn(maxConnections);
        when(botConfig.allowedUpdates()).thenReturn(Arrays.asList(allowedUpdates.split(":")));
        when(botConfig.ip()).thenReturn(InetAddress.getByName(ip));
        when(botConfig.dropPendingUpdatesOnStart()).thenReturn(dropPendingUpdatesOnStart);
        when(botConfig.secret()).thenReturn(secret);
        OkHttpTelegramClient okHttpClient = mock(OkHttpTelegramClient.class);
        WebhookBot webhookBot = new WebhookBot(url, botConfig, null, okHttpClient);
        webhookBot.runSetWebhook();
        ArgumentCaptor<SetWebhook> setWebhookArgumentCaptor = ArgumentCaptor.forClass(SetWebhook.class);
        verify(okHttpClient).execute(setWebhookArgumentCaptor.capture());
        SetWebhook actual = setWebhookArgumentCaptor.getValue();
        assertAll(
                () -> assertEquals(url + "/webhook/" + botConfig.username(), actual.getUrl()),
                () -> assertEquals(maxConnections, actual.getMaxConnections()),
                () -> assertEquals(allowedUpdates, String.join(":", actual.getAllowedUpdates())),
                () -> assertEquals(ip, actual.getIpAddress()),
                () -> assertEquals(dropPendingUpdatesOnStart, actual.getDropPendingUpdates()),
                () -> assertEquals(secret, actual.getSecretToken())
        );
    }

    @Test
    void runSetWebhook_ShouldNotThrow() throws TelegramApiException, UnknownHostException {
        when(botConfig.ip()).thenReturn(InetAddress.getLocalHost());
        OkHttpTelegramClient okHttpClient = mock(OkHttpTelegramClient.class);
        doThrow(RuntimeException.class).when(okHttpClient).execute(any(SetWebhook.class));
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, okHttpClient);
        assertDoesNotThrow(webhookBot::runSetWebhook);
    }

    @CsvSource(value = {
            "bulbabot,153523",
            "DonaldBTC_MONEY_24bot,153523"
    })
    @ParameterizedTest
    void consumeUpdate_shouldSendMessageToTopic(String username, Integer updateId) {
        Update update = new Update();
        update.setUpdateId(updateId);
        UpdateConsumer updateConsumer = mock(UpdateConsumer.class);
        when(botConfig.username()).thenReturn(username);
        WebhookBot webhookBot = new WebhookBot(null, botConfig, updateConsumer, null);
        BotApiMethod<?> actual = webhookBot.consumeUpdate(update);
        assertNull(actual);
        ArgumentCaptor<Update> updateArgumentCaptor = ArgumentCaptor.forClass(Update.class);
        verify(updateConsumer).consume(eq(botConfig), updateArgumentCaptor.capture());
        Update actualUpdate = updateArgumentCaptor.getValue();
        assertEquals(updateId, actualUpdate.getUpdateId());
    }

    @ValueSource(strings = {
            "donaldbot", "BTC247MONEY_BoT", "marioBOT"
    })
    @ParameterizedTest
    void getBotPath_shouldReturnBotPath(String username) {
        when(botConfig.username()).thenReturn(username);
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, null);
        assertEquals("/webhook/" + username, webhookBot.getBotPath());
    }

    @CsvSource(value = {
            "4czkl6TdZ2kqBUNlucVB4d25U7h1zA99,",
            ",4czkl6TdZ2kqBUNlucVB4d25U7h1zA99",
            ","
    })
    @ParameterizedTest
    void isValidSecret_shouldReturnFalseIfSomeValueIsNull(String botConfigSecret, String incomingSecret) {
        if (botConfigSecret != null) {
            when(botConfig.secret()).thenReturn(botConfigSecret);
        }
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, null);
        assertFalse(webhookBot.isValidSecret(incomingSecret));
    }

    @CsvSource(value = {
            "Gi8kTp1XEwwrqmYgcPEXIRJzfTXRbTCv,iBS9ISz3ycfDckyagJgeO5cbxjvLLsUe",
            "iBS9ISz3ycfDckyagJgeO5cbxjvLLsUe,Gi8kTp1XEwwrqmYgcPEXIRJzfTXRbTCv",
            "kSUGDN5e6hMgh93NpHjTANzn9ZPbz9mb,uw7HdXK77i6xWATItPpyt67mibUAH9xJ",
            "kSUGDN5e6hMgh93NpHjTANzn9ZPbz9mb,kSUGDN5e6hMgh93NpHjTANzn9ZPbz9m",
            "kSUGDN5e6hMgh93NpHjTANzn9ZPbz9mb,kSUGDN5e6hMgh93NpHjTANzn9ZPbz9ma",
            "СkSUGDN5e6hMgh93NpHjTANzn9ZPbz9mb,CkSUGDN5e6hMgh93NpHjTANzn9ZPbz9mb" // разные символы С
    })
    @ParameterizedTest
    void isValidSecret_shouldReturnFalseIfValuesNotEqual(String botConfigSecret, String incomingSecret) {
        when(botConfig.secret()).thenReturn(botConfigSecret);
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, null);
        assertFalse(webhookBot.isValidSecret(incomingSecret));
    }

    @CsvSource(value = {
            "FEgUnyKgnXLtAudzaQa0GIYPEti4pXh8,FEgUnyKgnXLtAudzaQa0GIYPEti4pXh8",
            "20ABy5Yn2XGNc9c6bnBNDgRVSQKlU4mg,20ABy5Yn2XGNc9c6bnBNDgRVSQKlU4mg",
            "CXQuXnsSF5PXxsHTRZUNh66VfR0Zc04TatmOjMOG2PjS9FWbldXXVUO6PCemHaRx," +
                    "CXQuXnsSF5PXxsHTRZUNh66VfR0Zc04TatmOjMOG2PjS9FWbldXXVUO6PCemHaRx"
    })
    @ParameterizedTest
    void isValidSecret_shouldReturnTrueIfSecretIsValid(String botConfigSecret, String incomingSecret) {
        when(botConfig.secret()).thenReturn(botConfigSecret);
        WebhookBot webhookBot = new WebhookBot(null, botConfig, null, null);
        assertTrue(webhookBot.isValidSecret(incomingSecret));
    }

    @Test
    @DisplayName("Должен вызвать метод MessageDigest.isEqual для константного времени выполнения.")
    void isValidSecret_shouldCallMessageDigestIsEqual() {
        try (MockedStatic<MessageDigest> mockedMessageDigest = Mockito.mockStatic(MessageDigest.class)) {
            mockedMessageDigest.when(() -> MessageDigest.isEqual(any(), any())).thenReturn(true);
            when(botConfig.secret()).thenReturn("secret");
            WebhookBot webhookBot = new WebhookBot(null, botConfig, null, null);
            webhookBot.isValidSecret("secret");
            mockedMessageDigest.verify(() -> MessageDigest.isEqual(any(), any()));
        }
    }
}