FROM openjdk:8-alpine

WORKDIR /service
ENV JAVA_OPTS ""
ENV SERVICE_PARAMS ""
ADD oauth-app/target/oauth-app.jar /service/
CMD java $JAVA_OPTS -jar oauth-app.jar $SERVICE_PARAMS