package ru.vl.testtask;

import picocli.CommandLine;
import ru.vl.testtask.service.CommandLineService;

public class AnalyzeApp {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new CommandLineService()).execute(args);
        System.exit(exitCode);
    }
}
