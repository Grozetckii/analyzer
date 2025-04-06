package ru.vl.testtask.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CommandLineServiceTest {

    private InputStream originalSystemIn;

    @BeforeEach
    void setUp() {
        originalSystemIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    void testCall_successfulAnalysis() throws Exception {
        //GIVEN
        String input = "test log line\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<LogAnalyzerService> mocked = mockConstruction(
                LogAnalyzerService.class,
                (mock, context) -> {
                    doNothing().when(mock)
                            .analyze(any(BufferedReader.class), anyLong(), anyDouble());
                })) {

            //WHEN
            CommandLineService service = new CommandLineService();
            Integer result = service.call();

            //THEN
            assertEquals(0, result);
            assertEquals(1, mocked.constructed().size());
            LogAnalyzerService mockInstance = mocked.constructed().getFirst();
            verify(mockInstance, times(1))
                    .analyze(any(BufferedReader.class), anyLong(), anyDouble());
        }
    }

    @Test
    void testCall_withException() throws Exception {
        //GIVEN
        String input = "test log line\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        try (MockedConstruction<LogAnalyzerService> mocked = mockConstruction(
                LogAnalyzerService.class,
                (mock, context) -> {
                    doThrow(new RuntimeException("Test error"))
                            .when(mock)
                            .analyze(any(BufferedReader.class), anyLong(), anyDouble());
                })) {

            //WHEN
            CommandLineService service = new CommandLineService();
            Integer result = service.call();

            //THEN
            assertEquals(0, result);
            assertEquals(1, mocked.constructed().size());
            LogAnalyzerService mockInstance = mocked.constructed().getFirst();
            verify(mockInstance, times(1))
                    .analyze(any(BufferedReader.class), anyLong(), anyDouble());
        }
    }
}