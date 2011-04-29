package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;

import net.sf.json.JSONObject;

public class JqGridDataBuilder {

    // current page for query
    private Integer page;
    // total records for the query
    private Integer records;
    // the rows
    private List<JqGridDataRow> rows = new ArrayList<JqGridDataRow>();
    
    // total pages for query
    private Integer total;
    
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
    
    // prepares our json....
    public String toJson() {
        JSONObject obj = new JSONObject();
        obj.accumulate("total", total.toString());
        obj.accumulate("page", page.toString());
        obj.accumulate("records", records.toString());
        List<Map<String,String>> rowData = new ArrayList<Map<String,String>>(rows.size());
        for (JqGridDataRow r : rows) {
            rowData.add(r.getValueMap());
        }
        obj.accumulate("rows", rowData);
        return obj.toString();
    }
    
    public void addRow(JqGridDataRow newRow) {
        rows.add(newRow);
    }
    
    public Integer getTotal() {
        return total;
    }
    public void setTotal(Integer total) {
        this.total = total;
    }
    public Integer getPage() {
        return page;
    }
    public void setPage(Integer page) {
        this.page = page;
    }
    public Integer getRecords() {
        return records;
    }
    public void setRecords(Integer records) {
        this.records = records;
    }
    public List<JqGridDataRow> getRows() {
        return rows;
    }
    public void setRows(List<JqGridDataRow> rows) {
        this.rows = rows;
    }
}
