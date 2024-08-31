package com.sample.keycloak_userstorageprovider_spi.factories;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import com.sample.keycloak_userstorageprovider_spi.providers.CustomUserStorageProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {
    @Override
    public CustomUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new CustomUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return "custom_user_storage_provider";
    }

    @Override
    public String getHelpText() {
        return UserStorageProviderFactory.super.getHelpText();
    }

    public static final String CONFIG_KEY_USER_DB_URL = "user-db-url";
    public static final String CONFIG_KEY_USER_DB_USER_NAME = "user-db-user-name";
    public static final String CONFIG_KEY_USER_DB_USER_PASSWORD = "user-db-user-password";

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        UserStorageProviderFactory.super.validateConfiguration(session, realm, config);

        // User database test connection
        try {
            final Connection conn = DriverManager.getConnection(
                config.get(CONFIG_KEY_USER_DB_URL),
                config.get(CONFIG_KEY_USER_DB_USER_NAME),
                config.get(CONFIG_KEY_USER_DB_USER_PASSWORD)
            );
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ComponentValidationException("User database connection refused.");
        }
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return ProviderConfigurationBuilder.create()
            .property().name(CONFIG_KEY_USER_DB_URL)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("User Database URL.")
            .defaultValue("jdbc:postgresql://<HOST>:<PORT>/<DATABASE>")
            .required(true)
            .add()
            .property().name(CONFIG_KEY_USER_DB_USER_NAME)
            .type(ProviderConfigProperty.STRING_TYPE)
            .label("User Database User Name.")
            .required(true)
            .add()
            .property().name(CONFIG_KEY_USER_DB_USER_PASSWORD)
            .type(ProviderConfigProperty.PASSWORD)
            .label("User Database User Password.")
            .required(true)
            .add()
            .build();
    }
}
