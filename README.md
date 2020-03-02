# tool-openapi-matcher

## Usage
### Binary files
Binary files placed in [bin-repo](https://github.com/Jepria/bin-repo).
Current versions: 
  [matcher-cli-1.0.jar](https://github.com/Jepria/bin-repo/blob/master/build/org/jepria/tools/openapi/matcher-cli/1.0/matcher-cli-1.0.jar)
  [matcher-1.0.jar](https://github.com/Jepria/bin-repo/blob/master/build/org/jepria/tools/openapi/matcher/1.0/matcher-1.0.jar)
### Command line options
```
-spec      - input specification file
-java      - input java jaxrs adapter file
```
#### Example use of matcher-cli.jar from bin repository.
```
java -jar %BIN_HOME%/build/org/jepria/tools/openapi/matcher-cli/1.0/matcher-cli-1.0.jar -spec modules/tool-openapi-matcher-cli/src/test/resources/swagger.json -java modules/tool-openapi-matcher-cli/src/test/resources/FeatureJaxrsAdapter.java
```
### Build projects
To build from source, you need the following installed and available in your $PATH:
* [Java 8](https://www.oracle.com/technetwork/java/index.html)
* [Apache Maven ](https://maven.apache.org/)

After cloning the project, you can build it from source with this command:
```sh
mvn clean install
```
