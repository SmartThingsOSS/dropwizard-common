# Dropwizard logging
## Logback appender filters
`smartthings.dw.logging.filter.DynamicThresholdLoggerFilter`:
Applies a  dynamic log level filtering to a set of user-specified loggers.
Logging events from other loggers are delegated to the remaining chain of appender filters.

Example:
```aidl
appenders:
- type: console
  filterFactories:
  - type: dynamic
    defaultThreshold: INFO
    onHigherOrEqual: ACCEPT
    onLower: DENY
    loggers:
      - foo
  - type: threshold
    level: INFO
```
The above configuration will create two filters for the console appender: DynamicThresholdLoggerFilter -> ThresholdFilter.
Logging events from logger `foo` and its child loggers with a log level higher than or equal to the level held by the MDC key `dynamicLogLevel` will be accepted by `DynamicThresholdLoggerFilter`, while those with a lower level will be denied. If the MDC value is not set at the time of the logging event, the threshold will default to `INFO`.
Logging events requested by other loggers will be decided by the next filter, which is a `ThresholdFilter` configured to have a static/constant `INFO` threshold.
