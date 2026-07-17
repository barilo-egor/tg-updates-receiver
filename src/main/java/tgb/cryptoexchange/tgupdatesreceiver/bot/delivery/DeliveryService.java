package tgb.cryptoexchange.tgupdatesreceiver.bot.delivery;

import tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;

import java.util.List;

public interface DeliveryService {

    UpdateDeliveryMethod getMethod();

    void start(List<? extends BotConfig> botConfigs);

    void shutdown();
}
