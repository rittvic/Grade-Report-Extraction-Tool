# Grade Report Extraction Tool

A tool to extract courses' average GPA from UW-Madison [course grade-distribution reports](https://registrar.wisc.edu/grade-reports/).

## Command Line Usage
```
Usage: Grade Report Extraction Tool [options]
  Options:
  * -i, -input
      Path to the directory with grades reports (e.g., -i path/to/grades)
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
