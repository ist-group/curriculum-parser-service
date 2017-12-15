# Open Data Curriculum Parser (ODCP) 

This is a tool for parsing the public open data from [opendata.skolverket.se](http://opendata.skolverket.se/)
 
ODCP works by parsing the HTML contained in the XML files from skolverket. It matches the HTML structure into usable types that can be imported into other applications. 

The service both contains HTML views for the data as well as an API for delivering it in JSON format.

## Platform and dependencies
This project is based on [Kotlin](https://kotlinlang.org/) so it needs to be downloaded and installed. 

### Build dependencies
Gradle is used for dependency management.  
You can download released versions and nightly build artifacts from: https://gradle.org/downloads

