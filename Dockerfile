FROM maven:3.8-openjdk-18 as build

WORKDIR /app

ADD pom.xml .

RUN mvn verify clean --fail-never

ADD . .

RUN mvn package

FROM openjdk:18.0.2-jdk

WORKDIR /app

COPY --from=build /app/target/GradesReportDataExtraction-*.jar /app/app.jar


ENTRYPOINT ["java","-jar","app.jar"]
