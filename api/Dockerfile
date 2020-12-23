FROM maven:3.6-jdk-11-slim AS builder
COPY . /usr/src/chemical-search-service/api
WORKDIR /usr/src/chemical-search-service/api
RUN mvn -Pdocker clean package

FROM openjdk:16-jdk-slim
RUN mkdir /temps
ENV MAVEN_APP chemical-search-api-0.1.jar
ENV MAVEN_APP_JAVA_OPTS -XX:NativeMemoryTracking=summary
WORKDIR /app
EXPOSE 8080
RUN apt-get update && apt-get install -y curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > /app/wait-for-it.sh && chmod 777 /app/wait-for-it.sh
COPY --from=builder /usr/src/chemical-search-service/api/target/$MAVEN_APP ./
ENTRYPOINT java $MAVEN_APP_JAVA_OPTS -jar $MAVEN_APP