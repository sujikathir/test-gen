FROM openjdk:17-slim as build

WORKDIR /app

COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

FROM openjdk:17-slim

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]