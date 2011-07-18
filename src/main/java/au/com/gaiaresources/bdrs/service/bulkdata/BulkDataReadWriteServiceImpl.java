package au.com.gaiaresources.bdrs.service.bulkdata;

import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;

@Service
public class BulkDataReadWriteServiceImpl implements BulkDataReadWriteService {

    @Override
    public String formatCensusMethodNameId(CensusMethod cm) {
        StringBuilder sb = new StringBuilder();
        sb.append(cm.getName());
        sb.append(":");
        sb.append(cm.getId().toString());
        return sb.toString();
    }

    @Override
    public Integer parseCensusMethodId(String s) {
        String[] spleet = s.split(":");
        Integer result = null;
        try {
            result = Integer.parseInt(spleet[spleet.length - 1]);
        } catch (NumberFormatException e) {
            result = null;
        }
        return result;
    }

    @Override
    public String parseCensusMethodName(String s) {
        String[] spleet = s.split(":");
        String[] withoutId = new String[spleet.length - 1];
        for (int i=0; i<withoutId.length; ++i) {
            withoutId[i] = spleet[i];
        }
        return org.apache.commons.lang.StringUtils.join(withoutId, ":");
    }

}
