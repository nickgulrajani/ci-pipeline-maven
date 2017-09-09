FROM openjdk:8-jre-alpine

ARG artifactName=ci-pipeline-maven
ARG artifactVersion=0.1.0

RUN apk add --update bash && rm -rf /var/cache/apk/*
RUN apk add --update tcpdump && rm -rf /var/cache/apk/*

ENV APP_DIRECTORY /app

RUN echo "Creating APP Directory $APP_DIRECTORY"
RUN mkdir -p $APP_DIRECTORY

RUN echo "Moving FatJar into $APP_DIRECTORY"
COPY ./target/${artifactName}-${artifactVersion}.jar $APP_DIRECTORY/${artifactName}-${artifactVersion}.jar
ENV ARTIFACT ${artifactName}-${artifactVersion}.jar
RUN chmod +x $APP_DIRECTORY/$ARTIFACT

RUN echo "Adding entrypoint.sh"
COPY ./docker/entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh", "$APP_DIRECTORY", "$ARTIFACT"]