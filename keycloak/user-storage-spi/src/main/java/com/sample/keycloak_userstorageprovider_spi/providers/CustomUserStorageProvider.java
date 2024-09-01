package com.sample.keycloak_userstorageprovider_spi.providers;

import com.sample.keycloak_userstorageprovider_spi.dto.UserDto;
import com.sample.keycloak_userstorageprovider_spi.factories.CustomUserStorageProviderFactory;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.UserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.user.UserLookupProvider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CustomUserStorageProvider implements UserStorageProvider, CredentialInputValidator, UserLookupProvider {
    private KeycloakSession session;
    private ComponentModel model;
    private Connection connection;

    public CustomUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = session;
        this.model = model;

        final MultivaluedHashMap<String, String> config = model.getConfig();
        try {
            this.connection = DriverManager.getConnection(
                config.get(CustomUserStorageProviderFactory.CONFIG_KEY_USER_DB_URL).getFirst(),
                config.get(CustomUserStorageProviderFactory.CONFIG_KEY_USER_DB_USER_NAME).getFirst(),
                config.get(CustomUserStorageProviderFactory.CONFIG_KEY_USER_DB_USER_PASSWORD).getFirst()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            this.connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        try {
            final UserDto dto = fetchUserByUsername(user.getUsername());
            String password = dto.getUserPassword();
            return credentialType.equals(PasswordCredentialModel.TYPE) && password != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType())) return false;
        try {
            // here we can add a database treatment instead of a static hashmap
            final UserDto dto = fetchUserByUsername(user.getUsername());
            String userPassword = dto.getUserPassword();
            if (userPassword == null) return false;

            final String hashed = hashSha512(credentialInput.getChallengeResponse());
            return userPassword.equals(hashed);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private UserModel createAdapter(RealmModel realm, UserDto dto) {
        return new AbstractUserAdapter(this.session, realm, this.model) {
            @Override
            public String getUsername() {
                return dto.getUserCd();
            }

            @Override
            public String getFirstName() {
                return dto.getUserName(); // TODO split white space
            }

            @Override
            public String getLastName() {
                return dto.getUserName(); // TODO split white space
            }

            @Override
            public String getEmail() {
                return dto.getUserCd() + "@example.com";
            }

            @Override
            public Map<String, List<String>> getAttributes() {
                return Map.of(
                    "username", List.of(getUsername()),
                    "email", List.of(getEmail()),
                    "first_name", List.of(getFirstName()),
                    "last_name", List.of(getLastName()),
                    "custom_group", List.of(dto.getSupersetRoleName())
                );
            }
            
            @Override
            public Stream<String> getAttributeStream(String name) {
                return Stream.of("username", "email", "first_name", "last_name", "custom_group");
            }

            @Override
            public SubjectCredentialManager credentialManager() {
                return new UserCredentialManager(this.session, realm, this);
            }
        };
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realm, username);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        try {
            final UserDto dto = fetchUserByUsername(username);
            if (dto != null) {
                return createAdapter(realm, dto);
            }

            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realmModel, String s) {
        return null;
    }

    private UserDto fetchUserByUsername(String username) throws SQLException {
        Statement st = this.connection.createStatement();
        ResultSet rs = st.executeQuery(String.format("SELECT * FROM users where user_cd = '%s';", username));

        UserDto dto = null;
        while (rs.next()) {
            dto = new UserDto(rs);
        }
        rs.close();
        st.close();

        return dto;
    }

    private String hashSha512(String textToHashed) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] cipherBytes = md.digest(textToHashed.getBytes());

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < cipherBytes.length; i++) {
                sb.append(String.format("%02x", cipherBytes[i] & 0xff)); // to hex
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
