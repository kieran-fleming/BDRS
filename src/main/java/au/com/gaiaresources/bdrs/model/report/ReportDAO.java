package au.com.gaiaresources.bdrs.model.report;

import java.util.List;

/**
 * Performs all database access for <code>Report</code>s
 */ 
public interface ReportDAO {

    /**
     * Returns all reports for the current portal.
     * 
     * @return all reports for the current portal.
     */
    public List<Report> getReports();
    
    /**
     * Gets the report with the specified ID.
     * 
     * @param reportId the primary key of the report.
     * @return the report specified by the id or null if one does not exist.
     */
    public Report getReport(int reportId);

    /**
     * Saves the specified report to the database.
     * 
     * @param report the report to be saved.
     * @return the persisted instance.
     */
    public Report save(Report report);

    /**
     * Deletes the specified <code>Report</code>.
     * 
     * @param report the report to be deleted.
     */
    public void delete(Report report);
}
