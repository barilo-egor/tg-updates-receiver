package tgb.cryptoexchange.tgupdatesreceiver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfiguration (String url) {
}
