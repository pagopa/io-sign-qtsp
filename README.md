# QTSP signer

Service to hash a PDF document, sign it via a QTSP and build the final PADES PDF.

## Api Documentation 📖

Spring application exposes API to manage the _SignPdf_.

See
the [OpenApi 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/io-microservice-sign-qtsp/feat-pades-pdf/openapi/openapi.yaml?v=2)

---

## Technology Stack
- Java 17
- Spring Boot
- Spring Web
- [DSS](https://github.com/esig/dss)
---

## Develop Locally 💻

### Install package

For the first time run

```sh
mvn clean install
```
### Run server locally

```sh
mvn spring-boot:run 
```

### Prerequisites
- git
- maven
- jdk-17