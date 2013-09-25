
package us.kbase.workspaceservice;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * Input parameters for the "delete_object_permanently" function.
 * 
 *         object_type type - type of the object to be permanently deleted (an essential argument)
 *         workspace_id workspace - ID of the workspace where the object is to be permanently deleted (an essential argument)
 *         object_id id - ID of the object to be permanently deleted (an essential argument)
 *         string auth - the authentication token of the KBase account to associate with this permanent deletion command (an optional argument; user is "public" if auth is not provided)
 *         bool2 asHash - a bool2ean indicating if metadata should be returned as a hash
 * 
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "id",
    "type",
    "workspace",
    "auth",
    "asHash"
})
public class DeleteObjectPermanentlyParams {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("workspace")
    private String workspace;
    @JsonProperty("auth")
    private String auth;
    @JsonProperty("asHash")
    private Integer asHash;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    public DeleteObjectPermanentlyParams withId(String id) {
        this.id = id;
        return this;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    public DeleteObjectPermanentlyParams withType(String type) {
        this.type = type;
        return this;
    }

    @JsonProperty("workspace")
    public String getWorkspace() {
        return workspace;
    }

    @JsonProperty("workspace")
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public DeleteObjectPermanentlyParams withWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    @JsonProperty("auth")
    public String getAuth() {
        return auth;
    }

    @JsonProperty("auth")
    public void setAuth(String auth) {
        this.auth = auth;
    }

    public DeleteObjectPermanentlyParams withAuth(String auth) {
        this.auth = auth;
        return this;
    }

    @JsonProperty("asHash")
    public Integer getAsHash() {
        return asHash;
    }

    @JsonProperty("asHash")
    public void setAsHash(Integer asHash) {
        this.asHash = asHash;
    }

    public DeleteObjectPermanentlyParams withAsHash(Integer asHash) {
        this.asHash = asHash;
        return this;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}