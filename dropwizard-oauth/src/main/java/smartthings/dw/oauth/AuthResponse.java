package smartthings.dw.oauth;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.*;

@JsonIgnoreProperties(ignoreUnknown = false)
public class AuthResponse {
    private String userName;
    private List<String> scopes;
    private List<String> authorities;
    private String clientId;
    private String uuid;
    private String email;
    private String fullName;
    private Map<String, Object> other = new HashMap<String, Object>();

    public AuthResponse() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getScopes() {
        if (scopes == null) {
            return new ArrayList<>();
        }
        return scopes;
    }

    public void setScopes(List<String> scope) {
        this.scopes = scope;
    }

    public List<String> getAuthorities() {
        if (authorities == null) {
            return new ArrayList<>();
        }
        return authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String, Object> getAdditionalFields() {
        return other;
    }

    @JsonAnySetter
    public void set(String name, Object value) {
        if (name.equals("client_id")) {
            setClientId((String) value);
        } else if (name.equals("uuid")) {
            setUuid((String) value);
        } else if (name.equals("fullName")) {
            setFullName((String) value);
        } else if (name.equals("email")) {
            setEmail((String) value);
        } else if (name.equals("user_name")) {
            setUserName((String) value);
        } else if (name.equals("scope")) {
            setScopes((List<String>) value);
        } else {
            if (value == null) {
                return;
            }
            other.put(name, value);
        }
    }

    @JsonIgnore
    public OAuthToken toOAuthToken(String token) {
        if (this.getClientId() != null && !this.getClientId().isEmpty()) {
            Optional<User> user = Optional.empty();
            if (this.getUserName() != null && !this.getUserName().isEmpty()) {
                // User will be absent in the case of client only tokens
                user = Optional.of(
                    new User(this.getUuid(),
                        this.getUserName(),
                        this.getEmail(),
                        this.getFullName(),
                        this.getAuthorities()));
            }
            return new OAuthToken(user, this.getScopes(), this.getClientId(), token, this.getAdditionalFields());
        } else {
            return null;
        }
    }
}
