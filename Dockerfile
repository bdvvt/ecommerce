FROM amazoncorretto:21-alpine
RUN apk update && apk upgrade --no-cache && apk add tzdata --no-cache && ln -s /usr/share/zoneinfo/Asia/Ho_Chi_Minh /etc/localtime && \
    addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --chown=appuser:appgroup identity-service/build/libs/identity-service-0.0.1-SNAPSHOT.jar notification-service/build/libs/notification-service-0.0.1-SNAPSHOT.jar ./

USER appuser

ENTRYPOINT ["/usr/bin/java", "-jar"]
CMD ["identity-service-0.0.1-SNAPSHOT.jar"]
