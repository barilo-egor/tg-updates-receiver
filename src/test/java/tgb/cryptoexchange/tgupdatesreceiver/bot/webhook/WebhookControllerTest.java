package tgb.cryptoexchange.tgupdatesreceiver.bot.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@ExtendWith(MockitoExtension.class)
class WebhookControllerTest {

    @MockitoBean
    private WebhookUpdateService webhookUpdateService;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @CsvSource({
            "MiZavezRN1L8cGrvj2prkB1ACz6Hw4j3,donaldbtc_bot," +
                    "{\"update_id\":123456789,\"message\":{\"message_id\":55,\"from\":{\"id\":987654321,\"is_bot\":false,\"first_name\":\"Иван\",\"username\":\"ivan_test\",\"language_code\":\"ru\"},\"chat\":{\"id\":987654321,\"first_name\":\"Иван\",\"username\":\"ivan_test\",\"type\":\"private\"},\"date\":1789141920,\"text\":\"Привет!\"}}\n",
            "f8cIQ2alFmoAmBFZYqWpy00GD8nG1zCW0nkOjm34NJtWwf1MujLQZONH3Z48w7DP,BTC24MONEY_BOT," +
                    "{\"update_id\":123456790,\"callback_query\":{\"id\":\"432109876543\",\"from\":{\"id\":987654321,\"is_bot\":false,\"first_name\":\"Иван\",\"username\":\"ivan_test\",\"language_code\":\"ru\"},\"message\":{\"message_id\":56,\"from\":{\"id\":11223344,\"is_bot\":true,\"first_name\":\"Мой Бот\",\"username\":\"my_cool_bot\"},\"chat\":{\"id\":987654321,\"first_name\":\"Иван\",\"username\":\"ivan_test\",\"type\":\"private\"},\"date\":1789141930,\"text\":\"Выберите опцию:\"},\"chat_instance\":\"-8877665544\",\"data\":\"btn_click_1\"}}\n"
    })
    @ParameterizedTest
    void updateReceived_shouldPassParametersToMethod(String secret, String username, String requestBody) throws Exception {
        Update update = mock(Update.class);
        when(objectMapper.readValue(requestBody, Update.class)).thenReturn(update);
        mockMvc.perform(post("/webhook/" + username)
                .header("X-Telegram-Bot-Api-Secret-Token", secret)
                .content(requestBody))
                .andExpect(status().isOk());
        verify(objectMapper).readValue(requestBody, Update.class);
        verify(webhookUpdateService).consume(username, secret, update);
    }

    @Test
    void updateReceived_shouldReturn200IfExceptionIsThrown() throws Exception {
        doThrow(RuntimeException.class).when(webhookUpdateService).consume(any(), any(), any());
        mockMvc.perform(post("/webhook/username")
                .content("{\"content\":\"content\"}"))
                .andExpect(status().isOk());
    }
}