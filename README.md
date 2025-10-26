# GatlingGen
Gatling Code Generator

Take HTTP calls as input and generate Gatling Simulations

## Usage

Run the CLI using the provided shell script:

```bash
./ggen "http call"
Ex: ./ggen "GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100"
```

Or use Gradle directly:

```bash
./gradlew run --args="GET policies?$select=code&$filter=code%20eq%20'excludeLate'&$skip=0&$top=100"
```

## Roadmap

Support OData format https://www.odata.org/

For reference see: https://docs.gatling.io/reference/script/http/recorder/
