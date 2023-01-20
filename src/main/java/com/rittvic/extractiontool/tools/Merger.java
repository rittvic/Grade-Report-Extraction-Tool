package com.rittvic.extractiontool.tools;

import com.rittvic.extractiontool.data.Course;
import com.rittvic.extractiontool.data.GradeReport;
import com.rittvic.extractiontool.data.Subject;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Merger {

    //Declare and initialize JSON objects for merging process
    private static JSONObject mergedGradeReports = new JSONObject();
    private static JSONObject termObject = new JSONObject();
    private static JSONObject subjectObject = new JSONObject();
    private static JSONObject courseObject = new JSONObject();

    public static JSONObject mergeGradeReports(List<GradeReport> gradeReports) {
        // This is pretty disgusting, but I haven't found a cleaner way for merging objects
        String term;
        for (GradeReport gradeReport : gradeReports) {
            term = String.valueOf(gradeReport.getTerm());
            for (Subject subject : gradeReport.getSubjects()) {
                String subjectAbbreviation = subject.getAbbreviation();
                if (!mergedGradeReports.has(subjectAbbreviation)) {
                    mergedGradeReports.put(subjectAbbreviation, new JSONObject());
                }
                for (Course course : subject.getCourses()) {
                    String courseNumber = String.valueOf(course.getCourseNumber());
                    Double courseAverageGpa = course.getAverageGpa();
                    subjectObject = mergedGradeReports.getJSONObject(subjectAbbreviation);
                    if (!subjectObject.has(courseNumber)) {
                        subjectObject.put(courseNumber, new JSONObject());
                    }
                    courseObject = subjectObject.getJSONObject(courseNumber);
                    if (!courseObject.has("cumulativeGpa")) {
                        courseObject.put("cumulativeGpa", JSONObject.NULL);
                    }
                    if (!courseObject.has("terms")) {
                        courseObject.put("terms", new JSONObject());
                    }
                    termObject = courseObject.getJSONObject("terms");
                    termObject.put(term, new JSONObject());
                    termObject.getJSONObject(term).put("averageGpa", Objects.requireNonNullElse(courseAverageGpa, JSONObject.NULL));
                    int numTerms = 0;
                    double massGpa = 0.0;
                    for (Map.Entry<String, Object> termEntry : termObject.toMap().entrySet()) {
                        String currentTerm = termEntry.getKey();
                        if (!termObject.getJSONObject(currentTerm).isNull("averageGpa")) {
                            double averageGpa = termObject.getJSONObject(currentTerm).getDouble("averageGpa");
                            massGpa += averageGpa;
                            numTerms++;
                        }
                    }
                    if (numTerms != 0) {
                        double cumulativeGpa = Math.floor((massGpa / numTerms) * 100) / 100;
                        courseObject.put("cumulativeGpa", cumulativeGpa);
                    }
                }
            }
        }
        return mergedGradeReports;
    }
}
