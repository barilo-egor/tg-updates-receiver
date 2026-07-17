package tgb.cryptoexchange.tgupdatesreceiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.polling")
public record PollingConfiguration(@DefaultValue Queue queue) {

    public record Queue(@DefaultValue("1000") Integer capacity) {}
}
