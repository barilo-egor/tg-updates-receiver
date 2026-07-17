package tgb.cryptoexchange.tgupdatesreceiver.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;
import tgb.cryptoexchange.tgupdatesreceiver.bot.UpdateDeliveryMethod;

import java.net.InetAddress;
import java.util.List;

@ConfigurationProperties(prefix = "bots")
@Validated
public record BotsConfiguration(@NotNull @NotEmpty List<BotConfig> configs) {
    public record BotConfig(@NotNull @NotBlank String username, @DefaultValue("40") Integer maxConnections,
                            @DefaultValue List<String> allowedUpdates, @NotNull @NotBlank String token,
                            @NotNull @NotBlank String secret,
                            @NotNull UpdateDeliveryMethod updateDeliveryMethod, InetAddress ip,
                            @NotNull @NotBlank String topic, @DefaultValue("false") Boolean dropPendingUpdatesOnStart,
                            @DefaultValue("false") Boolean dropPendingUpdatesOnShutdown) implements tgb.cryptoexchange.tgupdatesreceiver.bot.BotConfig {
        @Override
        @org.jetbrains.annotations.NotNull
        public String toString() {
            return "BotConfig{" +
                    "username='" + username + '\'' +
                    ", maxConnections=" + maxConnections +
                    ", allowedUpdates=" + allowedUpdates +
                    ", updateDeliveryMethod=" + updateDeliveryMethod +
                    ", ip=" + ip +
                    ", topic='" + topic + '\'' +
                    ", dropPendingUpdatesOnStart=" + dropPendingUpdatesOnStart +
                    ", dropPendingUpdatesOnShutdown=" + dropPendingUpdatesOnShutdown +
                    '}';
        }
    }
}
