FROM docker-base.artifactory.ist.local/java:jre-8u121


ADD application.yaml ${APP_FOLDER}/
ADD ./build/libs/curriculum-parser-service-0.0.4.jar ${APP_FOLDER}/app.jar
ENV SERVICE_8080_NAME=curriculum-ui
ENV SERVICE_TAGS=INCLUDE_SERVICE_NAME

EXPOSE 8080