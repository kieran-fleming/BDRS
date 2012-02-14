package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONObject;

/**
 * Helper object for building jqgrid compliant JSON
 *
 */
public class JqGridDataBuilder {

    // current page for query
    private Integer page;
    // total records for the query
    private Integer records;
    // the rows
    private List<JqGridDataRow> rows = new ArrayList<JqGridDataRow>();
    
    // total pages for query
    private Integer total;
    
    /**
     * Create a new data builder
     * 
     * @param maxPerPage Maximum items per page
     * @param totalCount Total items returned from query
     * @param currentPage Current page we are displaying
     */
    public JqGridDataBuilder(Integer maxPerPage, Integer totalCount, Integer currentPage) {
        this.setRecords(totalCount);
        this.setPage(currentPage);
        //this.setTotal()
        Integer totalPage = totalCount / maxPerPage;
        if (totalCount % maxPerPage != 0) {
            totalPage += 1;
        }
        this.setTotal(totalPage);
    }
    
   /**
    * Turns all our data into a json string
    * 
    * @return String - our json string
    */
    public String toJson() {
        JSONObject obj = new JSONObject();
        obj.accumulate("total", total.toString());
        obj.accumulate("page", page.toString());
        obj.accumulate("records", records.toString());
        List<Map<String,Object>> rowData = new ArrayList<Map<String,Object>>(rows.size());
        for (JqGridDataRow r : rows) {
            rowData.add(r.getValueMap());
        }
        obj.accumulate("rows", rowData);
        return obj.toString();
    }
    
    /**
     * Add a data row
     * @param newRow JqGridDataRow
     */
    public void addRow(JqGridDataRow newRow) {
        rows.add(newRow);
    }
    
    /**
     * Gets the total number of items
     * @return Integer
     */
    public Integer getTotal() {
        return total;
    }
    /**
     * Sets the total number of items 
     * @param total Integer
     */
    public void setTotal(Integer total) {
        this.total = total;
    }
    /**
     * Gets the current page
     * @return Integer
     */
    public Integer getPage() {
        return page;
    }
    /**
     * Sets the current page
     * @param page Integer
     */
    public void setPage(Integer page) {
        this.page = page;
    }
    /**
     * Gets the number of records returned
     * @return Integer
     */
    public Integer getRecords() {
        return records;
    }
    /**
     * Sets the number of records returned
     * @param records Integer
     */
    public void setRecords(Integer records) {
        this.records = records;
    }
    /**
     * Gets a list of all the data rows
     * @return List<JqGridDataRow>
     */
    public List<JqGridDataRow> getRows() {
        return rows;
    }
    /**
     * Sets a list of all the data rows
     * @param rows List<JqGridDataRow>
     */
    public void setRows(List<JqGridDataRow> rows) {
        this.rows = rows;
    }
}
