package au.com.gaiaresources.bdrs.model.expert;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.record.Record;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "EXPERT_REVIEW_REQUEST")
@AttributeOverride(name = "id", column = @Column(name = "EXPERT_REVIEW_REQUEST_ID"))
public class ReviewRequest extends PortalPersistentImpl {
    private Expert expert;
    private Record record;
    private String reasonForRequest;

    /**
     * Get the expert that has been assigned to this request.
     *
     * @return {@link Expert}
     */
    @ManyToOne
    @JoinColumn(name = "EXPERT_ID", nullable = false)
    @ForeignKey(name = "EXPERT_REQUEST_TO_EXPERT")
    public Expert getExpert() {
        return expert;
    }

    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    /**
     * Get the record that is to be reviewed.
     *
     * @return {@link Record}
     */
    @ManyToOne
    @JoinColumn(name = "RECORD_ID", nullable = false)
    @ForeignKey(name = "EXPERT_REQUEST_TO_RECORD")
    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    /**
     * Get the reason that this record was refered to the expert.
     *
     * @return {@link String}
     */
    @Column(name = "REASON_FOR_REQUEST", nullable = false)
    @Type(type = "text")
    public String getReasonForRequest() {
        return reasonForRequest;
    }

    public void setReasonForRequest(String reasonForRequest) {
        this.reasonForRequest = reasonForRequest;
    }
}
