package hirs.persist;

import hirs.FilteredRecordsList;
import hirs.data.persist.ReportSummary;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A <code>ReportSummaryManager</code> manages <code>ReportSummary</code>
 * objects. A <code>ReportSummaryManager</code> is used to store and
 * manage report summary. It has support for the basic create, read,
 * update, and delete methods.
 */
public interface ReportSummaryManager {

    /**
     * Stores a new <code>ReportSummary</code>. This stores a new
     * <code>ReportSummary</code> to be managed by
     * <code>ReportSummaryManager</code>. If <code>ReportSummary</code>
     * is successfully saved then a reference to it is
     * returned.
     *
     * @param report Report summary to save
     * @return reference to saved ReportSummary
     * @throws ReportSummaryManagerException
     *             if the ReportSummary has previously been saved or unexpected
     *             error occurs
     */
    ReportSummary saveReportSummary(ReportSummary report) throws ReportSummaryManagerException;

    /**
     * Updates a <code>ReportSummary</code>. This updates the
     * <code>ReportSummary</code> that is managed so subsequent calls to get
     * this <code>ReportSum</code> will return the values set by the incoming
     * <code>ReportSum</code>.
     *
     * @param report Report summary
     * @throws ReportSummaryManagerException if unable to update the ReportSum
     */
    void updateReportSummary(ReportSummary report) throws ReportSummaryManagerException;

    /**
     * Returns a list of all <code>ReportSummary</code>s managed by this
     * manager. A <code>Class</code> argument may be specified to limit which
     * types of <code>ReportSummary</code>s to return. Argument may be null to
     * return all <code>ReportSum</code>s.
     *
     * @param clazz class type of <code>ReportSummary</code>s to return (may be null)
     * @return list of all managed <code>ReportSummary</code> objects
     * @throws ReportSummaryManagerException if unable to create the list
     */
    List<ReportSummary> getReportSummaryList(Class<? extends ReportSummary> clazz)
            throws ReportSummaryManagerException;

    /**
     * Returns a list of <code>ReportSummary</code>s of type
     * <code>clazz</code> that share the provided <code>hostname</code>. This
     * searches through the database for this information.
     *
     * @param hostname hostname for the machine for which the <code>ReportSummary</code>
     *                 was generated
     * @return list of <code>ReportSummary</code>s
     * @throws ReportSummaryManagerException if unable to search the database
     */
    List<ReportSummary> getReportSummaryListByHostname(String hostname)
            throws ReportSummaryManagerException;

    /**
     * Returns a list of all <code>ReportSummary</code> objects that are ordered
     * by a column and direction (ASC, DESC) that is provided by the user.  This
     * method helps support the server-side processing in the JQuery DataTables.
     *
     * @param columnToOrder Column to be ordered
     * @param ascending direction of sort
     * @param firstResult starting point of first result in set
     * @param maxResults total number we want returned for display in table
     * @param search string of criteria to be matched to visible columns
     * @param searchableColumns Map of String and boolean values with column
     *      headers and whether they are to.  Boolean is true if field provides
     *      a typical String that can be searched by Hibernate without
     *      transformation.
     * @param hostname name of the device to filter on
     * @return FilteredRecordsList object with fields for DataTables
     * @throws ReportSummaryManagerException if unable to create the list
     */
    FilteredRecordsList<ReportSummary> getOrderedReportSummaryList(
            String columnToOrder, boolean ascending, int firstResult,
            int maxResults, String search,
            Map<String, Boolean> searchableColumns,
            String hostname) throws ReportSummaryManagerException;
    /**
     * Retrieves the <code>ReportSummary</code> identified by <code>id</code>.
     * If the <code>ReportSummary</code> cannot be found then null is returned.
     *
     * @param id id of the <code>ReportSummary</code>
     * @return <code>ReportSummary</code> whose name is <code>name</code> or null if not found
     * @throws ReportSummaryManagerException if unable to retrieve the ReportSum
     */
    ReportSummary getReportSummary(long id) throws ReportSummaryManagerException;

    /**
     * Retrieves the <code>ReportSummary</code> identified by the
     * <code>Report</code>'s <code>id</code>. If the <code>ReportSummary</code>
     * cannot be found then null is returned.
     *
     * @param id
     *            <code>UUID</code> of the <code>Report</code>
     * @return <code>ReportSummary</code> whose <code>Report</code>'s
     *         <code>UUID</code> is <code>id</code> or null if not found
     * @throws ReportSummaryManagerException if unable to retrieve the ReportSum
     */
    ReportSummary getReportSummaryByReportID(UUID id) throws ReportSummaryManagerException;

    /**
     * Deletes the <code>ReportSummary</code> identified by <code>id</code>.
     * If the <code>ReportSummary</code> is found and deleted then true is
     * returned, otherwise false.
     *
     * @param id id of <code>ReportSummary</code> to be deleted
     * @return true if successfully found and deleted from repo, otherwise false
     * @throws ReportSummaryManagerException if unable to delete the ReportSum for any reason other
     *                                       than not found
     */
    boolean deleteReportSummary(Long id) throws ReportSummaryManagerException;

    /**
     * Returns a list of <code>ReportSummary</code>s that contains the latest
     * report from each client. This searches through the database for this
     * information.
     *
     * @return list of <code>ReportSummary</code>s
     * @throws ReportSummaryManagerException if unable to search the database
     */
    List<ReportSummary> getUniqueClientLatestReportList() throws ReportSummaryManagerException;

    /**
     * Retrieves the newest report timestamp for a given <code>hostname</code>.
     *
     * @param hostname hostname of <code>client</code> to be checked for
     * @return newest timestamp from matching <code>ReportSummaries</code>
     * @throws ReportSummaryManagerException if unable to return a timestamp
     */
    ReportSummary getNewestReport(String hostname) throws ReportSummaryManagerException;

    /**
     * Retrieves the first report timestamp for a given <code>hostname</code>.
     *
     * @param hostname hostname of <code>client</code> to be checked for
     * @return newest timestamp from matching <code>ReportSummaries</code>
     * @throws ReportSummaryManagerException if unable to search the database
     */
    ReportSummary getFirstReport(String hostname) throws ReportSummaryManagerException;
}
