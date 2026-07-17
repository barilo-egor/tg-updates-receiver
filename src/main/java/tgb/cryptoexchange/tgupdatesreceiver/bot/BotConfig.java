package tgb.cryptoexchange.tgupdatesreceiver.bot;

import java.net.InetAddress;
import java.util.List;

public interface BotConfig {
    String username();
    Integer maxConnections();
    List<String> allowedUpdates();
    String token();
    String secret();
    UpdateDeliveryMethod updateDeliveryMethod();
    InetAddress ip();
    String topic();
    Boolean dropPendingUpdatesOnStart();
    Boolean dropPendingUpdatesOnShutdown();
}
