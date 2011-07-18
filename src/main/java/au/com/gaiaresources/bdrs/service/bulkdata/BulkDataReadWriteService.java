package au.com.gaiaresources.bdrs.service.bulkdata;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;

public interface BulkDataReadWriteService {
    String formatCensusMethodNameId(CensusMethod cm);
    Integer parseCensusMethodId(String s);
    String parseCensusMethodName(String s);
}
