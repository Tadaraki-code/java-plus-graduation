package ru.yandex.practicum.stat.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {
    final AggregationStarter aggregationStarter;

    @Override
    public void run(String... args) throws Exception {

        aggregationStarter.start();
    }
}
