package au.com.gaiaresources.bdrs.service.bulkdata;

import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

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
    
    @Override
    public void writeTypedAttributeValueCell(StyledRow styledRow, Cell cell,
            TypedAttributeValue attr) {
        if (attr != null) {
            //val = attr.getStringValue();
            switch (attr.getAttribute().getType()) {
            case INTEGER:
            case INTEGER_WITH_RANGE:
            case DECIMAL:
                if (attr.getNumericValue() != null) {
                    cell.setCellValue(attr.getNumericValue().doubleValue());
                }
                break;
            case DATE:
                if (attr.getDateValue() != null) {
                    cell.setCellValue(attr.getDateValue());
                    cell.setCellStyle(styledRow.getCellStyleByKey(StyledRow.STYLE_DATE_CELL));
                }
                break;
            case IMAGE:
            case FILE:
                throw new UnsupportedOperationException(
                        "Spreadsheet download of file data is not supported.");
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case STRING:
            case HTML:
            case HTML_NO_VALIDATION:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
            default:
                if (attr.getStringValue() != null) {
                    cell.setCellValue(attr.getStringValue());
                }
                break;
            }
        }
    }

}
