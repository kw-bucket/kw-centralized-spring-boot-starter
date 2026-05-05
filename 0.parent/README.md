# kw-centralized-parent

This module serves as the **Parent POM** for the `kw-bucket` ecosystem. It centralizes dependency management, plugin configurations, and property versions to ensure consistency across all child modules and microservices.

## 🎯 Purpose
The Parent POM is designed to:
*   **Uniform Versions**: Define the versions for Spring Boot, Cloud, and third-party libraries in one place.
*   **Plugin Management**: Standardize build settings (Compiler, Source, Resources) and Checkstyle/Lombok configurations.
*   **Simplify Maintenance**: Update a version here to propagate changes across all dependent projects.

## 🛠️ Technical Stack
*   **Base Framework**: Spring Boot 3.x
*   **Java Version**: 17+
*   **Build Tool**: Maven

## 📦 Usage

To use this parent in your Spring Boot project, add the following to your `pom.xml`:

```xml
<parent>
    <groupId>com.kw-bucket</groupId>
    <artifactId>kw-centralized-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <relativePath>../0.parent/pom.xml</relativePath> <!-- Adjust if used as a remote parent -->
</parent>
