package smartthings.dw.oauth;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            other.put(name, value);
        }
    }
}
