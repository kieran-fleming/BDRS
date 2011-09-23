package au.com.gaiaresources.bdrs.service.bulkdata;

import org.apache.poi.ss.usermodel.Cell;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

public interface BulkDataReadWriteService {
    String formatCensusMethodNameId(CensusMethod cm);
    Integer parseCensusMethodId(String s);
    String parseCensusMethodName(String s);
    /**
     * Creates a representation of a <code>TypedAttributeValue</code> in the
     * specified cell.
     *
     * @param styleRow provides a mapping of cell styles for date formatting.
     * @param cell the cell where the data shall be placed.
     * @param attr the attribute value to be represented in the cell.
     */
    void writeTypedAttributeValueCell(StyledRow styledRow, Cell cell,
                                 TypedAttributeValue attr);
}
