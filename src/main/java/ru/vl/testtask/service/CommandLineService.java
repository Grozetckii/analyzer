package ru.vl.testtask.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;

@Command(name = "analyze", mixinStandardHelpOptions = true,
        version = "analyze 1.0",
        description = "description")
public class CommandLineService implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineService.class);

    @Option(names = {"-t", "--time"}, description = "Minimum acceptable response time")
    private long minAcceptableResponseTime;
    @Option(names = {"-p", "--percentage"}, description = "Minimum acceptable level of accessibility in percent")
    private double minAcceptableLevelOfAccessibility;

    @Override
    public Integer call() throws Exception {
        LOGGER.info("Start analyze");
        long startTime = System.nanoTime();
        LogAnalyzerService logAnalyzerService = new LogAnalyzerService();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            logAnalyzerService.analyze(reader, minAcceptableResponseTime, minAcceptableLevelOfAccessibility);
        } catch (RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
        }

        long endTime = System.nanoTime();
        long durationInMillis = (endTime - startTime) / 1_000_000;

        LOGGER.info("Analysis is completed over time: {} ms", durationInMillis);
        return 0;
    }
}
