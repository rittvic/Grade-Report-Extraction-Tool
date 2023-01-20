package com.rittvic.extractiontool;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rittvic.extractiontool.data.GradeReport;
import com.rittvic.extractiontool.utils.cmdline.CommandLineArgs;
import com.rittvic.extractiontool.tools.Exporter;
import com.rittvic.extractiontool.tools.Parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandLineApp {

    private static Set<Path> loadGradeReportPdfs(Path parentDir) {
        Set<Path> gradeReports;
        try (Stream<Path> stream = Files.list(parentDir)) {
            gradeReports = stream.filter(file -> !Files.isDirectory(file)).collect(Collectors.toSet());
            return gradeReports;
        } catch (NoSuchFileException e) {
            System.err.println("An error has occurred: Unable to find the directory \"" + parentDir + "\"");
        } catch (NotDirectoryException e) {
            System.err.println("An error has occurred: \"" + parentDir + "\" is not a directory!");
        } catch (IOException e) {
            System.err.println("An unknown IO error has occurred: " + e);
        }
        System.err.println("Exiting ...");
        System.exit(-1);
        return null;
    }

    public static void main(String[] args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        JCommander commandLine = JCommander.newBuilder().addObject(commandLineArgs).programName("Grade Report Extraction Tool").build();

        try {
            commandLine.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            e.usage();
            return;
        }

        if (commandLineArgs.help) {
            commandLine.usage();
            return;
        }

        Set<Path> gradeReportPdfs = loadGradeReportPdfs(Path.of(commandLineArgs.gradeDir));

        if (gradeReportPdfs == null) {
            System.err.println("Unable to find any PDF files.");
            System.err.println("Exiting ...");
            return;
        }

        System.out.println("Found " + gradeReportPdfs.size() + " files.");

        long startTime = System.nanoTime();

        JsonNode subjectCodes;
        try (InputStream stream = CommandLineApp.class.getResourceAsStream("/SubjectCodes.json")) {
            subjectCodes = new ObjectMapper().readTree(stream);
        } catch (IOException e) {
            System.err.println("An unknown IO error has occurred: " + e);
            System.err.println("Exiting ...");
            return;
        }

        List<GradeReport> gradeReports = new ArrayList<>();
        for (Path gradeReportDir : gradeReportPdfs) {
            try {
                System.out.println("\nParsing " + gradeReportDir.getFileName() + " ...");
                GradeReport gradeReport = Parser.parseGradeReport(gradeReportDir, subjectCodes);
                gradeReports.add(gradeReport);
                if (commandLineArgs.exportOption.equals("all")) {
                    Exporter.toJson(commandLineArgs.outputDir, gradeReport);
                }
            } catch (IllegalStateException e) {
                System.err.println("The file " + "(" + gradeReportDir.getFileName() + ")" + " is not a grade report. " + "Path: " + gradeReportDir.normalize());
                System.err.println("Detailed message: " + e);
                System.err.println("Exiting ...");
                return;
            } catch (IOException e) {
                System.err.println("The file " + "(" + gradeReportDir.getFileName() + ")" + " is not a PDF. " + "Path: " + gradeReportDir.normalize());
                System.err.println("Detailed message: " + e);
                System.err.println("Exiting ...");
                return;
            }
        }

        System.out.println("\nMerging all " + gradeReports.size() + " grade reports ...");
        Exporter.toJson(commandLineArgs.outputDir, gradeReports);

        long endTime = System.nanoTime();
        long milliseconds = (endTime - startTime) / 1000000;
        long seconds = (endTime - startTime) / 1000000000;

        System.out.println("\nThis took " + seconds + " secs " + "(" + milliseconds + " ms)" + " to execute.");
    }
}
