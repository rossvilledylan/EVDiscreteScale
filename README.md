# EVLib Scalability Test

This project seeks to perform a scalability test on the EVLib library. The overall objective is to determine how useful the objects and tools provided by EVLib are when subjected to large-scale simulations, particularly of multiple charging stations within a single city.

## Required Libraries

First of all, this project requires [maven](https://maven.apache.org/) to compile and link. It also requires [Jackson](https://github.com/FasterXML/jackson), a JSON file reader, as well as the subject of the test, [EVLib](https://github.com/skarapost/EVLib). The latter two must be installed on the host computer in order to be recognized by this project's maven file. Furthermore, it is recommended to install maven prior to EVLib, as maven is also a prerequisite for that library.

* Maven - project management tool, handles dependency information
* GraalVM - to read and execute javascript equations
* Jackson - to read from JSON files
* EVLib - the subject