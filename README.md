# Darts Matcher API

Backend API for Darts Matcher which manages X01 dart matches, scoring, statistics, Dart Bot opponents and real-time match updates.

## Features

- Create and manage X01 matches.
- Configure matches using sets or legs with optional clear-by-two rules.
- Support human players and Dart Bot opponents with configurable playing strength.
- Add, edit and delete turns with automatic match progression.
- Reset, reprocess, delete and create rematches.
- Calculate available X01 checkouts.
- Track player averages, first-nine averages, checkout statistics, score statistics and standings.
- Synchronize match updates between clients in real time using WebSockets.

## Technical Overview

- Spring Boot.
- Java 17.
- REST API.
- STOMP over WebSocket for real-time match communication.
- MongoDB persistence using Spring Data.
- MongoDB transactions and optimistic locking for concurrent match updates.
- Bean Validation with custom match and scoring validators.
- Globalized error handling for REST and WebSocket communication.
- English and Dutch localization.
- JUnit and Testcontainers integration tests using MongoDB.
- Structured application and WebSocket logging.
- Docker support using a multi-stage build.