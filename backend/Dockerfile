ARG JDK_IMAGE=azul/zulu-openjdk:25-latest
ARG JRE_IMAGE=azul/zulu-openjdk:25-jre-headless-latest

FROM ${JDK_IMAGE} AS deps
WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -q dependency:go-offline

FROM deps AS build
COPY lombok.config ./
COPY src/main/ src/main/
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B package -DskipTests -Dspotless.check.skip=true -Dpmd.skip=true -Dspotbugs.skip=true

FROM ${JDK_IMAGE} AS extract
WORKDIR /extract
COPY --from=build /workspace/target/snaptale-backend.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination layers

FROM ${JRE_IMAGE} AS runtime
WORKDIR /app
RUN groupadd --system --gid 1001 snaptale && useradd --system --uid 1001 --gid snaptale snaptale

COPY --from=extract --chown=snaptale:snaptale /extract/layers/dependencies/ ./
COPY --from=extract --chown=snaptale:snaptale /extract/layers/spring-boot-loader/ ./
COPY --from=extract --chown=snaptale:snaptale /extract/layers/snapshot-dependencies/ ./
COPY --from=extract --chown=snaptale:snaptale /extract/layers/application/ ./

RUN mkdir -p /var/app/media && chown snaptale:snaptale /var/app/media
USER snaptale
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD ["bash", "-c", "exec 3<>/dev/tcp/127.0.0.1/8080 && printf 'GET /actuator/health/liveness HTTP/1.0\\r\\n\\r\\n' >&3 && grep -q '\"UP\"' <&3"]

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
