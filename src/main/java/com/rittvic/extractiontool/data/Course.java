package com.rittvic.extractiontool.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"number"})
public class Course {

    @JsonProperty("number")
    private Integer courseNumber;

    @JsonProperty("gpa")
    private Double averageGpa;

    public Course() {
        this(null, null);
    }

    public Course(Double averageGpa) {
        this(null, averageGpa);
    }

    public Course(Integer courseNumber, Double averageGpa) {
        this.courseNumber = courseNumber;
        this.averageGpa = averageGpa;
    }

    public int getCourseNumber() {
        return this.courseNumber;
    }

    public Double getAverageGpa() {
        return this.averageGpa;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public void setAverageGpa(Double averageGpa) {
        this.averageGpa = averageGpa;
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseNumber=" + courseNumber +
                ", averageGpa=" + averageGpa +
                '}';
    }
}
