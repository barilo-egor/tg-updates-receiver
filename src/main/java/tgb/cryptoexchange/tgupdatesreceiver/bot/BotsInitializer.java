package tgb.cryptoexchange.tgupdatesreceiver.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import tgb.cryptoexchange.tgupdatesreceiver.bot.delivery.DeliveryService;
import tgb.cryptoexchange.tgupdatesreceiver.config.BotsConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BotsInitializer implements SmartLifecycle {

    private final Map<UpdateDeliveryMethod, DeliveryService> deliveryServiceMap;

    private final BotsConfiguration botsConfiguration;

    private volatile boolean running = false;

    public BotsInitializer(List<DeliveryService> deliveryServiceList, BotsConfiguration botsConfiguration) {
        deliveryServiceMap = deliveryServiceList.stream()
                .collect(Collectors.toMap(
                        service -> Objects.requireNonNull(service.getMethod(), "Метод DeliveryService не может быть null."),
                        deliveryService -> deliveryService
                ));
        this.botsConfiguration = botsConfiguration;
    }

    @Override
    public void start() {
        botsConfiguration.configs()
                .stream()
                .collect(Collectors.groupingBy(BotConfig::updateDeliveryMethod))
                .forEach((method, configs) -> {
                    DeliveryService deliveryService = deliveryServiceMap.get(method);
                    if (Objects.isNull(deliveryService)) {
                        log.error("Присутствуют конфигурации ботов с методом получения апдейтов, для которого отсутствует " +
                                "реализация. Боты будут проигнорированы. method={}, configs={}", method, configs);
                        return;
                    }
                    try {
                        deliveryService.start(configs);
                    } catch (Exception e) {
                        log.error("Ошибка при попытке старта {} ботов: {}", method.name(), e.getMessage(), e);
                    }
                });
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        deliveryServiceMap.values().forEach(deliveryService -> {
            try {
                deliveryService.shutdown();
            } catch (Exception e) {
                log.error("Ошибка при попытке остановки ботов с методом получения {}: {}",
                        deliveryService.getMethod().name(), e.getMessage(), e);
            }
        });
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
