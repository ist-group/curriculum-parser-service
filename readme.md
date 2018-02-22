# Open Data Curriculum Service  

This is a tool for parsing and displaying the public open data from [opendata.skolverket.se](http://opendata.skolverket.se/)

The service both contains HTML views for the data as well as an API for delivering it in JSON format.
The api is reachable from the following urls: 
- `<host>/api/subject/{subjectName}` - Json structure for the subject level content
- `<host>/api//subject/{subjectName}/courses`- Json structure for all the courses
- `<host>/api//subject/{subjectName}/course/{courseCode}`- Json structure for a specific course

## Platform and dependencies
This project is based on [Kotlin](https://kotlinlang.org/) so it needs to be downloaded and installed. 

### Build dependencies
Gradle is used for dependency management.  
You can download released versions and nightly build artifacts from: https://gradle.org/downloads


## Building and running the application
To be able to build this project you are first required to install a version of curriculum-parser into your local maven.
```
git clone https://github.com/stefan-jonasson/curriculum-parser-service.git
cd curriculum-parser-service
gradle publishToMavenLocal
```

To build an executable jar type: `gradlew jar`

To run directly from source type: `gradlew bootRun` 

To use the service open [http://localhost:8088](http://localhost:8088) 