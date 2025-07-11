# EVDiscreteScale

This project seeks to perform a scalability test on the EVLib library. The overall objective is to determine how useful the objects and tools provided by EVLib are when subjected to large-scale simulations, particularly of multiple charging stations within a single city.

## Required Libraries

First of all, this project requires [maven](https://maven.apache.org/) to compile and link. It also requires [Jackson](https://github.com/FasterXML/jackson), a JSON file reader. The latter two must be installed on the host computer in order to be recognized by this project's maven file.

* Maven - project management tool, handles dependency information
* GraalVM - to read and execute javascript equations
* Jackson - to read from JSON files