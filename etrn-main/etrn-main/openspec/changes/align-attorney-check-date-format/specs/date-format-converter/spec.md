## ADDED Requirements

### Requirement: System SHALL serialize LocalDate in API responses using ISO 8601 format yyyy-MM-dd

The system SHALL use a custom Jackson `@JsonSerialize` converter for `LocalDate` fields in attorney check API responses, producing strings in the format `yyyy-MM-dd` (e.g., `"2025-01-15"`).

#### Scenario: AttorneyCheckResponseDto serialization

- **WHEN** the `attorneyCheck` endpoint returns `AttorneyCheckResponseDto`
- **THEN** the `issueDate` and `expiryDate` fields are serialized as ISO strings (`"yyyy-MM-dd"`), not timestamps

#### Scenario: AttorneyCheckResponseDto deserialization

- **WHEN** a client sends a request containing `LocalDate` fields in `yyyy-MM-dd` format
- **THEN** the system successfully deserializes the dates

### Requirement: LocalDateSerializer shall format LocalDate as yyyy-MM-dd string

The `LocalDateSerializer` in package `ru.sber.transport.etrn.config.converters` SHALL serialize `LocalDate` values to JSON strings using `DateTimeFormatter.ISO_LOCAL_DATE`.

#### Scenario: Serializer outputs correct format

- **WHEN** `LocalDateSerializer.serialize()` is called with `LocalDate.of(2025, 1, 15)`
- **THEN** the JSON output is the string `"2025-01-15"`

### Requirement: LocalDateDeserializer shall parse yyyy-MM-dd strings to LocalDate

The `LocalDateDeserializer` in package `ru.sber.transport.etrn.config.converters` SHALL deserialize JSON strings to `LocalDate` using `DateTimeFormatter.ISO_LOCAL_DATE`.

#### Scenario: Deserializer parses valid ISO date

- **WHEN** `LocalDateDeserializer.deserialize()` is called with the string `"2025-01-15"`
- **THEN** the result is `LocalDate.of(2025, 1, 15)`

#### Scenario: Deserializer rejects invalid date format

- **WHEN** `LocalDateDeserializer.deserialize()` is called with a string in `dd.MM.yyyy` format (e.g., `"15.01.2025"`)
- **THEN** the system throws a `JsonParseException`
