# kw-centralized-spring-boot-starter

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A centralized ecosystem for Spring Boot microservices within the **kw-bucket** organization. This repository provides a unified foundation for configuration, core logic, and auto-configuration starters to ensure consistency and speed up development.

## 🏗️ Project Structure

This is a multi-module Maven project organized as follows:

| Module | Purpose |
| :--- | :--- |
| [**0.parent**](./0.parent) | **The Foundation**: Manages dependency versions (BOM), plugin configs, and global properties. |
| [**1.core**](./1.core) | **The Engine**: Contains shared business logic, base classes, and internal utilities. |
| **Starter (Root)** | The auto-configuration layer that ties everything together for end-users. |

---

## 🚀 Quick Start

### 1. Build the Entire Project
To build all modules in the correct order, run this command from the root directory:

```bash
mvn clean install
