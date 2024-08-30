from flask_appbuilder.security.manager import AUTH_OID, AUTH_REMOTE_USER, AUTH_DB, AUTH_LDAP, AUTH_OAUTH
from superset.security import SupersetSecurityManager
from flask_oidc import OpenIDConnect
from flask_appbuilder.security.views import AuthOIDView, AuthOAuthView
from flask_login import login_user
from urllib.parse import quote
from flask_appbuilder.views import ModelView, SimpleFormView, expose
from flask import (
    redirect,
    request
)
import logging

class AuthOIDCView(AuthOAuthView):
    @expose('/logout/', methods=['GET', 'POST'])
    def logout(self):
        # keycloak_logout_url = "http://<keycloak host>/realms/<realm>/protocol/openid-connect/logout"
        keycloak_logout_url = "http://localhost:8080/realms/superset/protocol/openid-connect/logout"
        # logout_redirect_url = 'http://<superset host>/oauth-authorized/<provider name>'
        logout_redirect_url = 'http://localhost:8088/oauth-authorized/keycloak'
        # client_id = keycloak's client id
        client_id = 'Superset'
        self.appbuilder.app.config["LOGOUT_REDIRECT_URL"] = ("{0}?client_id={1}&post_logout_redirect_uri={2}".format(
            keycloak_logout_url,
            client_id,
            logout_redirect_url,
        ))
        return super().logout()


class OIDCSecurityManager(SupersetSecurityManager):
    def __init__(self, appbuilder):
        super(OIDCSecurityManager, self).__init__(appbuilder)
        self.authoauthview = AuthOIDCView
        # app = self.appbuilder.get_app
        # app.config.setdefault("AUTH_ROLES_MAPPING", {})
        # app.config.setdefault("AUTH_TYPE", AUTH_OAUTH)

    # override Flask Appbuilder
    # https://flask-appbuilder.readthedocs.io/en/latest/_modules/flask_appbuilder/security/manager.html
    def auth_user_oauth(self, userinfo):
        log = logging.getLogger("TEST")
        log.debug("auth_user_oauth userinfo.username: %s", userinfo["username"])
        log.debug("auth_user_oauth userinfo.email: %s", userinfo["email"])

        # if "email" not duplicated, insert ab_user.
        # Preventing inserts to ab_user even after the second login.
        user = self.find_user(email=userinfo["email"])
        if (not user):
            user_role_objects=set()

            user_role_keys = userinfo.get("role_keys", [])
            user_role_objects.update(self.get_roles_from_keys(user_role_keys))

            user = self.add_user(
                username=userinfo["username"],
                first_name=userinfo.get("first_name", ""),
                last_name=userinfo.get("last_name", ""),
                email=userinfo.get("email", ""),
                role=list(user_role_objects),
            )
        if user:
            self.update_user_auth_stat(user)
            return user

    # override Flask Appbuilder
    # https://flask-appbuilder.readthedocs.io/en/latest/_modules/flask_appbuilder/security/manager.html
    def oauth_user_info(self, provider, resp=None):
        log = logging.getLogger("TEST")
        if provider == "keycloak":
            # log.debug("Keycloak response received : {0}".format(resp))
            # log.debug("ID Token: %s", resp["id_token"])
            me = self.appbuilder.sm.oauth_remotes[provider].get(
                f'http://localhost:8080/realms/superset/protocol/openid-connect/userinfo'
            )
            me.raise_for_status()
            data = me.json()
            log.debug("User info from Keycloak: %s", data)
            return {
                "name": data["preferred_username"],
                "email": data["preferred_username"] + '@example.com',
                "first_name": data["preferred_username"],
                "last_name": data["preferred_username"],
                "id": data["sub"],
                "username": data["preferred_username"],
                "role_keys": ["user"]
            }

# class OIDCSecurityManager(SupersetSecurityManager):

#     def __init__(self, appbuilder):
#         super(OIDCSecurityManager, self).__init__(appbuilder)
#         if self.auth_type == AUTH_OID:
#             self.oid = OpenIDConnect(self.appbuilder.get_app)
#         self.authoidview = AuthOIDCView

# class AuthOIDCView(AuthOIDView):

#     @expose('/login/', methods=['GET', 'POST'])
#     def login(self, flag=True):
#         sm = self.appbuilder.sm
#         oidc = sm.oid

#         superset_roles = ["Admin", "Alpha", "Gamma", "Public", "granter", "sql_lab"]
#         default_role = "Admin"

#         @self.appbuilder.sm.oid.require_login
#         def handle_login():
#             user = sm.auth_user_oid(oidc.user_getfield('preferred_username')+'@example.com')

#             if user is None:
#                 info = oidc.user_getinfo(['sub', 'family_name', 'given_name', 'first_name', 'last_name', 'roles'])
#                 roles = [role for role in superset_roles if role in info.get('roles', [])]
#                 roles += [default_role, ] if not roles else []
                
#                 user = sm.add_user(info.get('sub'), info.get('preferred_username'), info.get('preferred_username'),
#                                    info.get('preferred_username')+'@example.com', [sm.find_role(role) for role in roles])
#                 # info = oidc.user_getinfo(['preferred_username', 'given_name', 'family_name', 'email'])
#                 # user = sm.add_user(info.get('preferred_username'), info.get('given_name'), info.get('family_name'),
#                 #                    info.get('email'), sm.find_role('Gamma'))

#             login_user(user, remember=False)
#             return redirect(self.appbuilder.get_url_for_index)

#         return handle_login()

#     @expose('/logout/', methods=['GET', 'POST'])
#     def logout(self):
#         oidc = self.appbuilder.sm.oid

#         oidc.logout()
#         super(AuthOIDCView, self).logout()
#         redirect_url = request.url_root.strip('/') + self.appbuilder.get_url_for_login

#         return redirect(
#             oidc.client_secrets.get('issuer') + '/protocol/openid-connect/logout?post_logout_redirect_uri=' + quote(redirect_url))
