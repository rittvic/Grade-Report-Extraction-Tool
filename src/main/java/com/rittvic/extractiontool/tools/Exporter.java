package com.rittvic.extractiontool.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.rittvic.extractiontool.data.GradeReport;
import com.rittvic.extractiontool.utils.enums.FileExtension;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Exporter {

    //For JSON serialization
    private static final ObjectWriter serializer = new ObjectMapper().writerWithDefaultPrettyPrinter();

    private static void createFile(String dirName, String fileName, String fileContent, FileExtension fileExtension) {
        String extension = fileExtension.extension;

        //Create path to the directory of specified name
        Path path = Path.of(dirName + File.separator + fileName + extension);

        //Create the directory with the given args as name
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            System.err.println("An error has occurred: Unable to create directory " + "(" + dirName + ").");
            System.err.println("Detailed message: " + e);
            return;
        }

        //Create the file with the given args as name and extension
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
            System.err.println("An error has occurred: The file " + "(" + fileName + extension + ")" + " already exists! " + path.normalize());
            System.err.println("Detailed message: " + e);
            return;
        } catch (IOException e) {
            System.err.println("An error has occurred: Unable to create file" + " (" + fileName + extension + "). " + path.normalize());
            System.err.println("Detailed message: " + e);
            return;
        }

        //Write to the created file with the given args as content
        try {
            Files.write(path, fileContent.getBytes());
        } catch (IOException e) {
            System.err.println("An error has occurred: Unable to write to file" + "(" + fileName + extension + "). " + path.normalize());
            System.err.println("Detailed message: " + e);
            return;
        }
        System.out.println(fileExtension + " file successfully created!" + " (" + path.normalize() + ")");
    }

    public static void toJson(String outputDir, GradeReport gradeReport) throws JsonProcessingException {
        String fileName = gradeReport.getTerm() + "-grades";
        createFile(outputDir, fileName, serializer.writeValueAsString(gradeReport), FileExtension.JSON);
    }

    public static void toJson(String outputDir, List<GradeReport> gradeReports) {
        JSONObject mergedGradesReports = Merger.mergeGradeReports(gradeReports);
        createFile(outputDir, "merged-grades", mergedGradesReports.toString(1), FileExtension.JSON);
    }
}
