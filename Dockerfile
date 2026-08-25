FROM eclipse-temurin:17-jre-alpine AS runtime

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY target/order-history-service-*.jar /app/order-history-service.jar

RUN chown -R appuser:appgroup /app
USER appuser

EXPOSE 9095

HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:9095/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "order-history-service.jar"]
