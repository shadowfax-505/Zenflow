# ZenFlow

Desktop focus timer and screen-time tracker built with JavaFX. See also the
[Android version](https://github.com/shadowfax-505/ZenFlow_Android).

## Features

- Focus timer with a session history
- Reminders
- Active-window tracking (macOS) that flags overused apps against
  per-category daily limits
- Dashboard, settings, and an admin view

## Stack

Java 24 · JavaFX 21 · SQLite (`sqlite-jdbc`) · Gradle

## Run

```bash
./gradlew run
```

Data is stored in `~/.zenflow/zenflow.db`, created on first launch.
Active-window tracking uses AppleScript, so it only works on macOS.
