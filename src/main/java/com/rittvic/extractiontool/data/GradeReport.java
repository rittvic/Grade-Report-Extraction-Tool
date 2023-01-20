package com.rittvic.extractiontool.data;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@JsonIgnoreProperties({"subjects", "numberOfCourses", "numberOfSubjects"})
public class GradeReport {

    @JsonProperty("term")
    private Integer term;

    @JsonProperty("subjects")
    private LinkedHashMap<String, Subject> subjects;

    public GradeReport() {
        this(null, new LinkedHashMap<>());
    }

    public GradeReport(LinkedHashMap<String, Subject> subjects) {
        this(null, subjects);
    }

    public GradeReport(Integer term, LinkedHashMap<String, Subject> subjects) {
        this.term = term;
        this.subjects = subjects;
    }

    public Integer getTerm() {
        return this.term;
    }

    public List<Subject> getSubjects() {
        return this.subjects.values().stream().toList();
    }

    @JsonAnyGetter
    public HashMap<String, Subject> getSubjectsMap() {
        return this.subjects;
    }

    @JsonProperty("numberOfSubjects")
    public int getNumberOfSubjects() {
        return this.getSubjects().size();
    }

    @JsonProperty("numberOfCourses")
    public int getNumberOfCourses() {
        int numCourses = 0;
        for (Subject subject : this.getSubjects()) {
            numCourses += subject.getNumberOfCourses();
        }
        return numCourses;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void addEntry(Subject subject) {
        this.subjects.put(subject.getAbbreviation(), subject);
    }

    public boolean hasSubject(Subject subject) {
        return this.subjects.containsKey(subject.getAbbreviation());
    }

    @Override
    public String toString() {
        return "GradeReport{" +
                "term=" + term +
                ", subjects=" + subjects +
                '}';
    }
}

