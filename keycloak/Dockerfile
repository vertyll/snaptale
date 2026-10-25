# The Keycloak theme of this application, as an image that a Keycloak shared by several applications copies the
# JAR from into its providers folder.
FROM docker.io/library/alpine:3.24.3 AS build
RUN apk add --no-cache bash zip
WORKDIR /keycloak
COPY build-theme.sh ./
COPY themes themes
RUN ./build-theme.sh

FROM docker.io/library/busybox:1.38.0
COPY --from=build /keycloak/target/snaptale-keycloak-theme.jar /themes/snaptale-keycloak-theme.jar
