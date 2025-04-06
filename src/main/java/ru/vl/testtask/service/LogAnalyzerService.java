package ru.vl.testtask.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class LogAnalyzerService {
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy:HH:mm:ss Z");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final Logger LOGGER = LoggerFactory.getLogger(LogAnalyzerService.class);

    /**
     * Общее количество логов с найденными отказами
     * */
    private BigDecimal totalNumberOfLogs;
    /**
     * Количество отказных запросов
     * */
    private BigDecimal numberOfFailures;
    /**
     * Дата первого лога с отказным запросом
     **/
    private ZonedDateTime startDateOfErrorsFixation;
    /**
     * Дата последнего лога с отказным запросом
     * */
    private ZonedDateTime endDateOfErrorsFixation;
    /**
     * Флаг существования ошибки ранее
     * */
    private boolean isErrorOccurredEarlier;

    public LogAnalyzerService() {
        totalNumberOfLogs = BigDecimal.ZERO;
        numberOfFailures = BigDecimal.ZERO;
        startDateOfErrorsFixation = null;
        endDateOfErrorsFixation = null;
        isErrorOccurredEarlier = false;
    }

    public void analyze(BufferedReader reader,
                        long minAcceptableResponseTime,
                        double minAcceptableLevelOfAccessibility) throws IOException {
        String log;
        while ((log = reader.readLine()) != null) {
            try {
                boolean isFailure = processLogLine(log, minAcceptableResponseTime);

                totalNumberOfLogs = totalNumberOfLogs.add(BigDecimal.ONE);

                if (isFailure && isErrorOccurredEarlier) {
                    endDateOfErrorsFixation = extractZonedDateTimeFromLog(log);
                    numberOfFailures = numberOfFailures.add(BigDecimal.ONE);
                }

                if (isFailure && !isErrorOccurredEarlier) {
                    startDateOfErrorsFixation = endDateOfErrorsFixation = extractZonedDateTimeFromLog(log);
                    numberOfFailures = numberOfFailures.add(BigDecimal.ONE);
                    isErrorOccurredEarlier = true;
                }

                if (!isFailure && isErrorOccurredEarlier) {
                    printIfItSatisfiesConditions(BigDecimal.valueOf(minAcceptableLevelOfAccessibility));
                }
            } catch (RuntimeException e) {
                isErrorOccurredEarlier = false;
                LOGGER.error(e.getMessage(), e);
            }
        }
        
        if (isErrorOccurredEarlier) {
            totalNumberOfLogs = totalNumberOfLogs.add(BigDecimal.ONE);
            printIfItSatisfiesConditions(BigDecimal.valueOf(minAcceptableLevelOfAccessibility));
        }
    }

    private void printIfItSatisfiesConditions(BigDecimal minAcceptableLevelOfAccessibility) {
        BigDecimal accessibilityLevel = BigDecimal.valueOf(100)
                .subtract(
                        numberOfFailures.divide(totalNumberOfLogs.subtract(BigDecimal.ONE), 5, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)));

        if (accessibilityLevel.compareTo(minAcceptableLevelOfAccessibility) < 0) {
            totalNumberOfLogs = BigDecimal.ONE;
            numberOfFailures = BigDecimal.ZERO;
            isErrorOccurredEarlier = false;

            System.out.printf("%s %s %.1f%n",
                    startDateOfErrorsFixation.format(TIME_FORMATTER),
                    endDateOfErrorsFixation.format(TIME_FORMATTER),
                    accessibilityLevel);
        }
    }

    private boolean processLogLine(String line, double minAcceptableResponseTime) {
        try {
            String[] parts = line.split(" ");

            if (parts.length < 11) {
                throw new IllegalArgumentException("Строка лога содержит менее 11 полей: " + line);
            }

            String statusCode = parts[8];
            double responseTime = Double.parseDouble(parts[10]);

            return statusCode.startsWith("5") || responseTime > minAcceptableResponseTime;
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Не удалось проанализировать строку лога: " + line, e);
        }
    }

    private ZonedDateTime extractZonedDateTimeFromLog(String log) {
        try {
            String timestamp = log.split("\\[")[1].split("]")[0];
            return ZonedDateTime.parse(timestamp, LOG_DATE_FORMATTER);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Не удалось извлечь дату и время из строки лога: " + log, e);
        }
    }

}
