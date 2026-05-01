# CSC2620_Final_Project_Hajj_Higgins_Palmer

**Object Oriented Design Final Project**
Group Members: Peter Hajj, Jackson Higgins, and Matthew Palmer

---

## Prerequisites

### Java 17

This project requires Java 17 or later. Verify your installation:

```
java -version
```

### Maven Wrapper

This project includes the **Maven Wrapper**, Maven does not need to be installed on your machine to run. The wrapper scripts (`mvnw.cmd` on Windows, `mvnw` on Mac/Linux) will automatically download the Maven Wrapper the first time you build.

---

## Building the Project

Clone the repository, then run from the project root.

**Windows:**
```
.\mvnw.cmd package
```

**Mac / Linux:**
```
chmod +x mvnw
./mvnw package
```

This compiles the source and produces a fat JAR (all dependencies bundled) at:

```
target/final-project-1.0-SNAPSHOT-jar-with-dependencies.jar
```

To clean previously compiled output before rebuilding:

**Windows:**
```
.\mvnw.cmd clean package
```

**Mac / Linux:**
```
./mvnw clean package
```

---

## Setting Up the Vosk Speech Recognition Model

Vosk requires a local model directory to perform speech recognition. The **lightweight English model** is recommended — it is fast, small (~40 MB), and accurate enough for most use cases.

### Recommended model: `vosk-model-small-en-us-0.15`

1. Download the model from the Vosk model page:
   https://alphacephei.com/vosk/models

   Direct download link for the recommended lightweight model:
   `vosk-model-small-en-us-0.15.zip`

2. Extract the zip file. You should get a folder named `vosk-model-small-en-us-0.15`.

3. Place the extracted folder in the project root (alongside `pom.xml`), or note the full path to it — you will need to supply this path when running the application.

> **Note:** Larger models (e.g., `vosk-model-en-us-0.22`) are available if higher accuracy is needed, but they are significantly larger (~1.8 GB) and slower. For development and testing, stick with the small model.
