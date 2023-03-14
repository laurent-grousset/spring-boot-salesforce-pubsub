FROM maven:3.8-eclipse-temurin-17 as builder
RUN mkdir /build
COPY . /build/
WORKDIR /build
RUN mvn --no-transfer-progress -Dmaven.test.skip=true clean package

FROM eclipse-temurin:17-alpine as runner
RUN mkdir /opt/app
COPY --from=builder /build/target/app.jar /opt/app/
EXPOSE 8080/tcp
ENTRYPOINT ["java", "-jar", "/opt/app/app.jar"]
