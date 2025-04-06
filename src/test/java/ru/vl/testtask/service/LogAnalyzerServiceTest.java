package ru.vl.testtask.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogAnalyzerServiceTest {

    private LogAnalyzerService logAnalyzerService;
    private ByteArrayOutputStream outputStream;
    private Path actualInputFile;
    private Path expectedOutputFile;

    @BeforeEach
    void setUp() {
        actualInputFile = Paths.get("src/test/resources/actual_stdin.log");
        expectedOutputFile = Paths.get("src/test/resources/expected_stdout.log");
        logAnalyzerService = new LogAnalyzerService();
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    void testAnalyze_withRefusalRequests() throws IOException {
        //GIVEN
        BufferedReader reader = Files.newBufferedReader(actualInputFile);
        String expectedOutput = new String(Files.readAllBytes(expectedOutputFile)).trim();

        //WHEN
        logAnalyzerService.analyze(reader, 45L, 99);

        //THEN
        String actualOutput = outputStream.toString().trim();
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    void analyze_LogsRuntimeException() {
        //GIVEN
        Logger logger = (Logger) LoggerFactory.getLogger(LogAnalyzerService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        try {
            String invalidLog = "192.168.1.1 - - INVALID_TIMESTAMP";
            BufferedReader reader = new BufferedReader(new StringReader(invalidLog));
            LogAnalyzerService service = new LogAnalyzerService();

            //WHEN
            service.analyze(reader, 100, 99.9);

            //THEN
            List<ILoggingEvent> logs = listAppender.list;
            assertFalse(logs.isEmpty());
            assertEquals(Level.ERROR, logs.getFirst().getLevel());
            assertNotNull(logs.getFirst().getFormattedMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            logger.detachAppender(listAppender);
        }
    }

}