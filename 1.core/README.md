# kw-centralized-core

The `core` module is the backbone of the **kw-centralized-spring-boot-starter**. It contains the fundamental logic, shared models, and utility classes that power the entire ecosystem.

## ⚙️ Role in the Project
While the parent module manages dependencies, **core** provides the actual implementation. It is intended to be a lightweight library that can be included in other modules without pulling in unnecessary auto-configuration overhead.

## ✨ Features
*   **Base Components**: Abstract classes and interfaces to standardize service implementation.
*   **Common Utilities**: Specialized helpers for internal logic (String, Date, Reflection).
*   **Exception Handling**: Base exception classes for consistent error propagation.
*   **Shared Models**: Common DTOs and Enums used across the `kw-bucket` project.

## 🚀 Getting Started

### Dependency
To use the core logic in a child module, add this to your `pom.xml`:

```xml
<dependency>
    <groupId>com.kw</groupId>
    <artifactId>kw-centralized-core</artifactId>
    <version>${project.version}</version>
</dependency>
