package ru.yandex.practicum.stat.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.stat.analyzer.service.processor.EventSimilarityProcessor;
import ru.yandex.practicum.stat.analyzer.service.processor.UserActionProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final EventSimilarityProcessor eventSimilarityProcessor;
    final UserActionProcessor userActionProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread hubEventsTread = new Thread(userActionProcessor);
        hubEventsTread.setName("HubEventHandlerThread");
        hubEventsTread.start();

        eventSimilarityProcessor.start();
    }

}
