# GatlingGen: Gatling Code Generator for API testing.

Gatling can be used both for load testing and for functional API testing.
An popular approach to creating these tests is to observe HTTP traffic 
via the Chrome Developer Tools or a proxy server. 
This tool will take HTTP traces as input and generate Gatling simulations.

## Usage

Run the CLI using the provided shell script:

```bash
./ggen <filename>.http
Ex: ./ggen get_abc.http
```

Or use Gradle directly:

```bash
./gradlew run --args=get_abc.http
```

## Set target directory

export the `GGEN_TARGET_DIR` environment variable to set the output directory.

## Roadmap

Support OData format https://www.odata.org/

For reference see: https://docs.gatling.io/reference/script/http/recorder/

## Generating code with AI

One of the main purposes of this project is to explore using various AI code generators such as
CoPilot, Devin, Augment, OpenCode, etc.
