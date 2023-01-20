# Grade Report Extraction Tool

A tool to extract courses' average GPA from UW-Madison [course grade-distribution reports](https://registrar.wisc.edu/grade-reports/).

## Command Line Usage
```
Usage: Grade Report Extraction Tool [options]
  Options:
  * -i, -input
      Path to the directory with grade reports (e.g., -i path/to/grades)
    -o, -output
      Path to the output directory for exported files (e.g., -o path/to/data)
      Default: ./output
    -e, -export
      Set export mode
      all = export every individual terms, and merged terms
      merged-only = export only the merged terms
      Default: all
    --help
      Show all usages
```
## Output Examples
Currently, only `.json` format is supported.

### Individual term
Here's an example for some COMP SCI courses from term 1204 (Spring 2020):
```json
  "COMP SCI" : {
    "200" : {
      "gpa" : 3.555
    },
    "220" : {
      "gpa" : 3.696
    },
    "240" : {
      "gpa" : 3.476
    },
    "252" : {
      "gpa" : 3.725
    },
    "298" : {
      "gpa" : null
    },
    "300" : {
      "gpa" : 3.458
    },
    "304" : {
      "gpa" : 4.0
    },
    "310" : {
      "gpa" : 3.318
    },
    "320" : {
      "gpa" : 3.745
    },
    "352" : {
      "gpa" : 3.889
    },
    "354" : {
      "gpa" : 3.558
    },
    "368" : {
      "gpa" : null
    },
    "400" : {
      "gpa" : 3.632
    },
    "402" : {
      "gpa" : 4.0
    },
    "407" : {
      "gpa" : 3.64
    },
```

### Merged terms
Here's an example for COMP SCI 352 average GPA from Spring 2007 to Fall 2022. Also includes cumulative GPA.
```json
"COMP SCI": {
  "352": {
   "terms": {
    "1164": {"averageGpa": 2.799},
    "1142": {"averageGpa": 3.088},
    "1162": {"averageGpa": 2.78},
    "1184": {"averageGpa": 3},
    "1084": {"averageGpa": 3.139},
    "1182": {"averageGpa": 3.141},
    "1082": {"averageGpa": 2.781},
    "1114": {"averageGpa": 3.222},
    "1212": {"averageGpa": 3.063},
    "1134": {"averageGpa": 2.93},
    "1112": {"averageGpa": 2.97},
    "1232": {"averageGpa": null},
    "1154": {"averageGpa": 2.875},
    "1132": {"averageGpa": 3.096},
    "1152": {"averageGpa": 2.602},
    "1174": {"averageGpa": 2.868},
    "1074": {"averageGpa": 2.975},
    "1172": {"averageGpa": 2.98},
    "1194": {"averageGpa": 3.147},
    "1094": {"averageGpa": 2.708},
    "1192": {"averageGpa": 2.708},
    "1092": {"averageGpa": 3.125},
    "1204": {"averageGpa": 3.889},
    "1104": {"averageGpa": 2.817},
    "1224": {"averageGpa": 3.5},
    "1202": {"averageGpa": 3.024},
    "1124": {"averageGpa": 2.801},
    "1102": {"averageGpa": 2.731},
    "1222": {"averageGpa": 2.938},
    "1144": {"averageGpa": 2.935},
    "1122": {"averageGpa": 2.973}
   },
   "cumulativeGpa": 2.98
  },
}
```

## How To Use
* Install the latest version of [Java](https://www.oracle.com/java/technologies/downloads/)
* Clone the repository
```
git clone https://github.com/rittvic/Grade-Report-Extraction-Tool.git
```
* Build a .JAR file by running `mvn package` in your IDE
* Run the .JAR file
```
java -jar <jar-name> -i path/to/input/dir
```

# Docker
Alternatively, you can use Docker to run the tool (recommended).
* Pull the Docker image
```
docker pull ghcr.io/rittvic/grade-report-extraction-tool:latest
```
* Bind the input directory from your local machine to the Docker container filesystem (with `-v` flag), and then run the image
```
docker run --name extraction-tool -v /path/to/input/dir:/app/grades ghcr.io/rittvic/grade-report-extraction-tool -i grades
```

* Copy the output directory to your local machine. Use `docker container ps -a` to find the corresponding container id.
```
docker cp <container id>:/app/output /host/path/to/target
```

To access the logs, you can use `docker logs extraction-tool`.

<b>Note: You must leave /app in the Docker container path as that is where the .JAR file is located.</b>

<hr>

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for  details

<b><i>Note: This project is not affiliated with UW-Madison. </b></i>
