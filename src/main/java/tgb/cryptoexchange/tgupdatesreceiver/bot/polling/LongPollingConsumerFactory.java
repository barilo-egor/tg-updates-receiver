package tgb.cryptoexchange.tgupdatesreceiver.bot.polling;

import org.springframework.stereotype.Component;
import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateConsumer;
import tgb.cryptoexchange.tgupdatesreceiver.config.PollingConfiguration;

import java.util.concurrent.ExecutorService;

@Component
public class LongPollingConsumerFactory {

    private final ExecutorService commonVirtualThreadExecutor;

    private final UpdateConsumer updateConsumer;

    private final PollingConfiguration pollingConfiguration;

    public LongPollingConsumerFactory(ExecutorService commonVirtualThreadExecutor,
                                      UpdateConsumer updateConsumer,
                                      PollingConfiguration pollingConfiguration) {
        this.commonVirtualThreadExecutor = commonVirtualThreadExecutor;
        this.updateConsumer = updateConsumer;
        this.pollingConfiguration = pollingConfiguration;
    }

    public LongPollingBotUpdateConsumer create(BotConfig botConfig) {
        var consumer = new LongPollingBotUpdateConsumer(botConfig.username(), pollingConfiguration.queue().capacity());
        commonVirtualThreadExecutor.submit(new UpdateSendWorker(updateConsumer, botConfig, consumer.getQueue()));
        return consumer;
    }
}
