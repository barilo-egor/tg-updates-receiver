package tgb.cryptoexchange.tgupdatesreceiver.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateConsumer {

    void consume(BotConfig botConfig, Update update);
}
