package com.sample.keycloak_userstorageprovider_spi.dto;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDto {
    private final String userCd;
    private final String userName;
    private final String userPassword;
    private final String supersetRoleName;

    public UserDto(ResultSet rs) throws SQLException {
        this.userCd = rs.getString("user_cd");
        this.userName = rs.getString("user_name");
        this.userPassword = rs.getString("user_password");
        this.supersetRoleName = rs.getString("superset_role_name");
    }

    public String getUserCd() {
        return userCd;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public String getSupersetRoleName() {
        return supersetRoleName;
    }
}
