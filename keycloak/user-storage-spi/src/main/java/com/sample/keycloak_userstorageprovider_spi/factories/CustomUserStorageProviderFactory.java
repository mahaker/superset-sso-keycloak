package com.sample.keycloak_userstorageprovider_spi.factories;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.storage.UserStorageProviderFactory;

import com.sample.keycloak_userstorageprovider_spi.providers.CustomUserStorageProvider;


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

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        UserStorageProviderFactory.super.validateConfiguration(session, realm, config);
    }
}
