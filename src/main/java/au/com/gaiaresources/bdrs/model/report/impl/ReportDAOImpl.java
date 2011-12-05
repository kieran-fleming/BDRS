package au.com.gaiaresources.bdrs.model.report.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.report.Report;
import au.com.gaiaresources.bdrs.model.report.ReportDAO;

@Repository
public class ReportDAOImpl extends AbstractDAOImpl implements ReportDAO {

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.report.ReportDAO#delete(au.com.gaiaresources.bdrs.model.report.Report)
     */
    @Override
    public void delete(Report report) {
        getSession().delete(report);
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.report.ReportDAO#getReport(int)
     */
    @Override
    public Report getReport(int reportId) {
        return super.getByID(Report.class, reportId);
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.report.ReportDAO#getReports()
     */
    @Override
    public List<Report> getReports() {
        return super.find("from Report order by name");
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.report.ReportDAO#save(au.com.gaiaresources.bdrs.model.report.Report)
     */
    @Override
    public Report save(Report report) {
        return super.save(report);
    }
}