# GatlingGen
Gatling Code Generator

Take HTTP calls as input and generate Gatling Simulations

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

## Roadmap

Support OData format https://www.odata.org/

For reference see: https://docs.gatling.io/reference/script/http/recorder/
