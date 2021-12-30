# Log4j Detector - v3.3.0
Fork of the mergebase/log4j scanner


This project contains a log4j detector with the aim to also detect shaded and fat jars.

# Quick Guide: Scan whole System, keep result locally

If possible, always run these scripts with Admin rights (Windows) / via sudo to have access to all files.

Otherwise, you will only get a partial result due to missing access.

# Use jar for specific folders
- Scan single path using Java: 
```
java -jar l4j-detector.jar $TARGET_FOLDER >> $OUTPUT_FILE
```

# Build
The jar can be built via mvn package in the main folder.
