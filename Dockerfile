FROM navikt/java:16
COPY target/*.jar "/app/app.jar"
ENV JAVA_OPTS --enable-preview --add-opens java.base/java.time=ALL-UNNAMED
ENV APPD_ENABLED=true
ENV APP_NAME=foreldrepengesoknad-api
ENV APPDYNAMICS_CONTROLLER_HOST_NAME=appdynamics.adeo.no
ENV APPDYNAMICS_CONTROLLER_PORT=443
ENV APPDYNAMICS_CONTROLLER_SSL_ENABLED=true
