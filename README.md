# Hytale Economy Plugin

## Setup

1. Download "HytaleServer.jar" file.
2. Place jar file under "libraries/" directory.

## Features

- Persistent player balances stored in JSON.
- Configurable starting balance, currency name, symbol, and autosave interval.
- Commands: `/balance`, `/pay`, `/eco give|take|set`.

## Building locally

This project requires Java 25. Use the Gradle wrapper to build a plugin jar:

```
./gradlew clean build
```

The jar will be generated under `build/libs/` (for example `EconomyPlugin-1.0-SNAPSHOT.jar`).

## CI build artifacts

GitHub Actions builds and uploads the jar on every push and pull request. Download the artifact
from the workflow run to get the latest `build/libs/*.jar`.
