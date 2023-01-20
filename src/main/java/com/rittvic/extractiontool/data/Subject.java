package com.rittvic.extractiontool.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@JsonIgnoreProperties({"abbreviation", "courses", "numberOfCourses"})
public class Subject {

    @JsonProperty("abbreviation")
    private String abbreviation;

    @JsonProperty("courses")
    private LinkedHashMap<Integer, Course> courses;

    public Subject() {
        this(null, new LinkedHashMap<>());
    }

    public Subject(LinkedHashMap<Integer, Course> courses) {
        this(null, courses);
    }

    public Subject(String abbreviation, LinkedHashMap<Integer, Course> courses) {
        this.abbreviation = abbreviation;
        this.courses = courses;
    }

    public String getAbbreviation() {
        return this.abbreviation;
    }

    public List<Course> getCourses() {
        return this.courses.values().stream().toList();
    }

    @JsonAnyGetter
    public HashMap<Integer, Course> getCoursesMap() {
        return this.courses;
    }

    @JsonProperty("numberOfCourses")
    public int getNumberOfCourses() {
        return this.getCourses().size();
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public void addCourse(Course course) {
        this.courses.put(course.getCourseNumber(), course);
    }

    public boolean hasCourse(Course course) {
        return this.courses.containsKey(course.getCourseNumber());
    }

    @Override
    public String toString() {
        return "Subject{" +
                "abbreviation='" + abbreviation + '\'' +
                ", courses=" + courses +
                '}';
    }
}
