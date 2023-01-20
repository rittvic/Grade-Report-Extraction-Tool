package com.rittvic.extractiontool.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rittvic.extractiontool.data.Course;
import com.rittvic.extractiontool.data.GradeReport;
import com.rittvic.extractiontool.data.Subject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Parser {
    //Compile regex patterns for filtering rows
    private static Pattern startOfPage = Pattern.compile("TERM|University of Wisconsin - Madison|Section");
    private static Pattern endOfPage = Pattern.compile("Summary|Page|Dept. Total|College Total|Grand Total|University Total");
    private static Pattern allNumbers = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static Pattern allNumbersNotOptional = Pattern.compile("-?\\d+(\\.\\d+)");

    private static boolean startOfPage(String row) {
        return startOfPage.matcher(row).find() || !row.matches(".*\\d.*");
    }

    private static boolean endOfPage(String row) {
        return endOfPage.matcher(row).find();
    }

    private static boolean isValidGpa(String gpa) {
        //Check if it's parse-able to a double, otherwise it's not a valid number
        double formattedGpa;
        try {
            formattedGpa = Double.parseDouble(gpa);
        } catch (NumberFormatException e) {
            return false;
        }
        //If it doesn't have a decimal, then it's not a gpa
        if (!gpa.contains(".")) {
            return false;
        }
        //If it doesn't have three decimal places, then it's not a gpa
        String[] split = gpa.split("\\.");
        int decimalPlaces = split[1].length();
        if (decimalPlaces != 3) {
            return false;
        }
        //Last check to see if it's within the valid gpa range of 0.0 and 4.0
        return formattedGpa > 0.0 && formattedGpa <= 4.0;
    }

    private static Double extractGpa(String row, boolean containsCourseTotal) {
        int length = row.split(" ").length;
        int courseNumber;
        String strippedRow;
        //This is where we will be storing all the extracted numbers from the line
        List<String> allOfTheNumbers;
        //the index of the average gpa
        int gpaIndex = 1;
        if (!containsCourseTotal) {
            //Extract the course number
            //NOTE: Sometimes, the course number is not always in the same index; when this is the case, it seems like it's always just one index off
            try {
                courseNumber = Integer.parseInt(row.split(" ")[length - 20]);
            } catch (NumberFormatException e) {
                courseNumber = Integer.parseInt(row.split(" ")[length - 19]);
            }

            //Strip the line so that it starts with the course number
            //This is to ensure that courses with numbers in the name will not affect the extraction
            strippedRow = row.substring(row.indexOf(Integer.toString(courseNumber)));
            try {
                allOfTheNumbers = Stream.of(strippedRow.split(" ")).filter(allNumbers.asPredicate()).toList();
                gpaIndex = 3;
            } catch (NumberFormatException e) {
                //Sometimes, the course contains a letter in the number, like "00A"
                //Instead, we will just not account for that portion
                allOfTheNumbers = Stream.of(strippedRow.split(" ")).filter(allNumbersNotOptional.asPredicate()).toList();
                gpaIndex = 0;
            }
        } else {
            strippedRow = row.substring(row.indexOf("Course Total"));
            allOfTheNumbers = Stream.of(strippedRow.split(" ")).filter(allNumbers.asPredicate()).toList();
        }
        String averageGpa = allOfTheNumbers.get(gpaIndex);
        return allOfTheNumbers.size() >= 2 && isValidGpa(averageGpa) ? Double.parseDouble(averageGpa) : null;
    }

    private static String extractSemesterAndYear(String termCode) {
        if (termCode.length() != 4) {
            throw new IllegalArgumentException(termCode + " is not a valid term code!");
        }

        //"1082" -> [1,0,8,2]
        Integer[] digits = termCode.chars().map(c -> c - '0').boxed().toArray(Integer[]::new);

        //The term code is derived as follows:
        //Character 1 = Century (0 = 1900 and 1 = 2000)
        //Character 2 & 3 = Academic Year
        //Character 4 = Term ( 2=Fall, 4=Spring and 6=Summer)
        //Source: https://data.wisc.edu/infoaccess/available-data-views/uw-madison-student-administration/enrolled-student/stdnt-term-codes/
        String century = digits[0] == 0 ? "19" : "20";
        String academicYear = century + digits[1] + digits[2];
        String semester = null;

        if (digits[3] == 2) {
            semester = "Fall";
            String adjustedDigits = digits[1].toString() + digits[2].toString();
            int adjustedYear = (Integer.parseInt(adjustedDigits)) - 1;
            academicYear = century + String.format("%02d", adjustedYear);
        } else if (digits[3] == 4) {
            semester = "Spring";
        } else if (digits[3] == 6) {
            throw new IllegalArgumentException("Summer terms do not have a grade report!");
        }

        if (semester == null) {
            throw new IllegalArgumentException(termCode + " is not a valid term code!");
        }
        return semester + " " + academicYear;
    }

    public static GradeReport parseGradeReport(Path gradeReportPdf, JsonNode subjectCodes) throws IOException {
        //Load the PDF document
        PDDocument document = PDDocument.load(gradeReportPdf.toFile());
        int numPages = document.getNumberOfPages();

        //Strip the lines
        PDFTextStripper linesStripper = new PDFTextStripper();
        linesStripper.setSortByPosition(true);

        GradeReport gradeReport = new GradeReport();
        Subject subject = new Subject();
        String subjectAbbrev;
        String subjectCode;
        Integer courseNumber = null;

        PAGE:
        for (int i = 0; i <= numPages; i++) {
            linesStripper.setStartPage(i);
            linesStripper.setEndPage(i);

            List<String> rows = linesStripper.getText(document).lines().toList();

            ROW:
            for (String row : rows) {
                try {
                    //replace all multi-spaces with just a single space
                    row = row.replaceAll("\s+", " ");

                    //Extract the term code
                    if (gradeReport.getTerm() == null) {
                        if (row.contains("TERM")) {
                            int term = Integer.parseInt(row.replaceAll("[^0-9]", "").strip());
                            String semesterAndYear;
                            try {
                                semesterAndYear = extractSemesterAndYear(String.valueOf(term));
                            } catch (IllegalArgumentException e) {
                                System.err.println("An error has occurred: " + e.getMessage());
                                break PAGE;
                            }
                            System.out.println("Detected term: " + term + " (" + semesterAndYear + ")");
                            gradeReport.setTerm(term);
                        }
                    }
                    //Extract the subject code and abbreviation
                    if (row.contains("Grades GPA")) {
                        subjectCode = row.split(" ")[0];

                        //We're going to try to get the subject abbreviation from the JSON file using the parsed subject code
                        try {
                            subjectAbbrev = subjectCodes.get(subjectCode).get("Abbreviation").asText();
                        } catch (NullPointerException e) {
                            //If the subject code doesn't exist, then it is likely using a discontinued subject, and we will just use
                            //extracted subject abbreviation instead
                            subjectAbbrev = row.replace("Grades GPA", "").replaceAll("\\d", "").strip();
                        }
                        if (subject.getAbbreviation() == null) {
                            subject.setAbbreviation(subjectAbbrev);
                        } else if (!subject.getAbbreviation().equals(subjectAbbrev)) {
                            //Sometimes a subject is repeated, so we check if we already accounted for the subject
                            if (gradeReport.hasSubject(subject)) {
                                for (Course course : subject.getCourses()) {
                                    gradeReport.getSubjectsMap().get(subject.getAbbreviation()).addCourse(course);
                                }
                            } else {
                                gradeReport.addEntry(subject);
                            }
                            subject = new Subject();
                            subject.setAbbreviation(subjectAbbrev);
                        }

                        // if the line only contains numbers, then it is a course section.
                        // So we will just keep track of the course number
                    } else if (!row.matches(".*[a-zA-Z]+.*")) {
                        courseNumber = Integer.valueOf(row.split(" ")[0]);

                        //If the line contains "Course Total", then it contains the average gpa for
                        // that course. So let's extract the gpa
                    } else if (row.contains("Course Total")) {
                        Double averageGpa = !row.contains("***") ? extractGpa(row, true) : null;
                        subject.addCourse(new Course(courseNumber, averageGpa));
                    } else {
                        if (endOfPage(row)) {
                            //If we're on the last page, we have to add the remaining parsed courses before the method exits
                            if (i == numPages) {
                                gradeReport.addEntry(subject);
                            }
                            continue PAGE;
                        }
                        if (startOfPage(row)) {
                            continue ROW;
                        }
                        int length = row.split(" ").length;
                        //If the line contains stars, then readjust the length by 11 in order to properly extract the course number
                        //otherwise, re-adjust by 20
                        //NOTE: The numbers are found by trial and error
                        int fixedLength = !row.contains("***") ? length - 20 : length - 11;
                        try {
                            courseNumber = Integer.parseInt(row.split(" ")[fixedLength]);
                        } catch (NumberFormatException e) {
                            courseNumber = Integer.parseInt(row.split(" ")[length - 19]);
                        }
                        Double averageGpa = !row.contains("***") ? extractGpa(row, false) : null;
                        subject.addCourse(new Course(courseNumber, averageGpa));
                    }
                } catch (Exception e) {
                    System.err.println("A parsing error has occurred.");
                    System.err.println("Skipping the following line: \"" + row + "\" ...");
                }
            }
        }
        if (gradeReport.getNumberOfSubjects() == 0 && gradeReport.getNumberOfSubjects() == 0) {
            throw new IllegalStateException("Not a valid grade report!");
        }
        System.out.println("Total subjects parsed: " + gradeReport.getNumberOfSubjects());
        System.out.println("Total courses parsed: " + gradeReport.getNumberOfCourses());
        document.close();
        return gradeReport;
    }

}


