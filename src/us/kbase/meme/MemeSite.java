
package us.kbase.meme;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.codehaus.jackson.annotate.JsonAnyGetter;
import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * <p>Original spec-file type: MemeSite</p>
 * <pre>
 * Represents a particular site from MEME motif description 
 * string source_sequence_id - ID of sequence where the site was found
 * int start - position of site start 
 * float pvalue - site P-value
 * string left_flank - sequence of left flank
 * string sequence - sequence of site
 * string right_flank - sequence of right flank
 * </pre>
 * 
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "source_sequence_id",
    "start",
    "pvalue",
    "left_flank",
    "right_flank",
    "sequence"
})
public class MemeSite {

    @JsonProperty("source_sequence_id")
    private String sourceSequenceId;
    @JsonProperty("start")
    private Integer start;
    @JsonProperty("pvalue")
    private Double pvalue;
    @JsonProperty("left_flank")
    private String leftFlank;
    @JsonProperty("right_flank")
    private String rightFlank;
    @JsonProperty("sequence")
    private String sequence;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("source_sequence_id")
    public String getSourceSequenceId() {
        return sourceSequenceId;
    }

    @JsonProperty("source_sequence_id")
    public void setSourceSequenceId(String sourceSequenceId) {
        this.sourceSequenceId = sourceSequenceId;
    }

    public MemeSite withSourceSequenceId(String sourceSequenceId) {
        this.sourceSequenceId = sourceSequenceId;
        return this;
    }

    @JsonProperty("start")
    public Integer getStart() {
        return start;
    }

    @JsonProperty("start")
    public void setStart(Integer start) {
        this.start = start;
    }

    public MemeSite withStart(Integer start) {
        this.start = start;
        return this;
    }

    @JsonProperty("pvalue")
    public Double getPvalue() {
        return pvalue;
    }

    @JsonProperty("pvalue")
    public void setPvalue(Double pvalue) {
        this.pvalue = pvalue;
    }

    public MemeSite withPvalue(Double pvalue) {
        this.pvalue = pvalue;
        return this;
    }

    @JsonProperty("left_flank")
    public String getLeftFlank() {
        return leftFlank;
    }

    @JsonProperty("left_flank")
    public void setLeftFlank(String leftFlank) {
        this.leftFlank = leftFlank;
    }

    public MemeSite withLeftFlank(String leftFlank) {
        this.leftFlank = leftFlank;
        return this;
    }

    @JsonProperty("right_flank")
    public String getRightFlank() {
        return rightFlank;
    }

    @JsonProperty("right_flank")
    public void setRightFlank(String rightFlank) {
        this.rightFlank = rightFlank;
    }

    public MemeSite withRightFlank(String rightFlank) {
        this.rightFlank = rightFlank;
        return this;
    }

    @JsonProperty("sequence")
    public String getSequence() {
        return sequence;
    }

    @JsonProperty("sequence")
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public MemeSite withSequence(String sequence) {
        this.sequence = sequence;
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
