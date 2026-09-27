# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-21 AS build
ARG GITHUB_OWNER=OWNER
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN --mount=type=secret,id=maven_settings,target=/root/.m2/settings.xml \
	--mount=type=secret,id=github_actor,env=GITHUB_ACTOR \
	--mount=type=secret,id=github_token,env=GITHUB_TOKEN \
	mvn -B -Dgithub.owner=${GITHUB_OWNER} -Dgithub.packages=true -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/checkout-service-0.1.0-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]