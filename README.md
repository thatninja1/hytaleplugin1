# Hytale Economy Plugin

## Setup

1. Download "HytaleServer.jar" file.
2. Place jar file under "libraries/" directory.

## Features

- Persistent player balances stored in JSON.
- Configurable starting balance, currency name, symbol, and autosave interval.
- Commands: `/balance`, `/pay`, `/eco give|take|set`.

## Building locally

This project requires Java 25. Maven supports two build modes:

### With the real Hytale server jar

Place `HytaleServer.jar` under `libraries/` and run:

```
mvn -B clean package
```

### With stubbed APIs (no server jar required)

Use the CI property to compile against minimal stubs:

```
mvn -B -Dci=true clean package
```

The jar will be generated under `target/` (for example `EconomyPlugin-1.0-SNAPSHOT.jar`).

## CI build artifacts

GitHub Actions builds with `-Dci=true` and uploads the jar on every push and pull request. Download
the artifact from the workflow run to get the latest `target/*.jar`.
