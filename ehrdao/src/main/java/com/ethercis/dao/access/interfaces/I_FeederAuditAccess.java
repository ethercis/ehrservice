package com.ethercis.dao.access.interfaces;

import com.ethercis.dao.access.jooq.FeederAuditAccess;
import org.jooq.Result;
import org.openehr.rm.common.archetyped.FeederAudit;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public interface I_FeederAuditAccess {

    static I_FeederAuditAccess getInstance(I_DomainAccess domain, FeederAudit feederAudit) throws Exception {
        return new FeederAuditAccess(domain.getContext(), feederAudit);
    }

    static I_FeederAuditAccess retrieveInstance(I_DomainAccess domainAccess, UUID id){
        return FeederAuditAccess.retrieveInstance(domainAccess , id);
    }

    static I_FeederAuditAccess retrieveInstance(I_DomainAccess domainAccess, Result<?> records){
        return FeederAuditAccess.retrieveInstance(domainAccess , records);
    }

    UUID commit(Timestamp transactionTime) throws Exception;

    UUID commit() throws Exception;

    Boolean update(Timestamp transactionTime) throws SQLException;

    Boolean update(Timestamp transactionTime, boolean force) throws Exception;

    Boolean update() throws Exception;

    Boolean update(Boolean force) throws Exception;

    Integer delete();

    FeederAudit mapRmFeederAudit();

    void setRecordFields(UUID id, FeederAudit feederAudit) throws Exception;

    void setCompositionId(UUID compositionId);

    UUID getId();
}
