FROM eclipse-temurin:21 as spi-builder
COPY ./user-storage-spi/ /opt/user-storage-spi
WORKDIR /opt/user-storage-spi
RUN ./gradlew build

# ref: https://www.keycloak.org/server/containers
FROM quay.io/keycloak/keycloak:latest as keycloak-builder

# Enable health and metrics support
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true

# Configure a database vendor
ENV KC_DB=postgres

# install provider
COPY --from=spi-builder /opt/user-storage-spi/build/libs/keycloak_userStorageProvider_SPI-0.0.1-SNAPSHOT.jar /opt/keycloak/providers

# install theme
COPY ./mytheme/ /opt/keycloak/themes/mytheme

WORKDIR /opt/keycloak
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:latest
COPY --from=keycloak-builder /opt/keycloak/ /opt/keycloak/
ENV KC_DB=postgres

ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
