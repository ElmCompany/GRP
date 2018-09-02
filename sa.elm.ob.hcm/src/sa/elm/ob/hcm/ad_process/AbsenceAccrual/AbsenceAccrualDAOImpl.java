package sa.elm.ob.hcm.ad_process.AbsenceAccrual;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EHCMAbsenceAccrualDetails;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EHCMEmployeeStatusV;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * This process class used for Absence DecisionDAO Implementation
 * 
 * @author divya 09-04-2018
 *
 */
@Repository
public class AbsenceAccrualDAOImpl implements AbsenceAccrualDAO {

    private static final Logger log = LoggerFactory.getLogger(AbsenceAccrualDAOImpl.class);
    DateFormat yearFormat = Utility.YearFormat;
    DateFormat dateFormat = Utility.dateFormat;

    /**
     * get available and availed days
     * 
     * @param conn
     * @param absenceaccrual
     * @param absencetype
     * @param startdate
     * @param enddate
     * @param availabledays
     * @return
     */
    public JSONObject getAvailableAndAvaileddays(Connection conn, EHCMAbsenceAccrual absenceaccrual,
      EHCMAbsenceType absencetype, String startdate, String enddate, Boolean availabledays,
      String subTypeId) {
	BigDecimal days = BigDecimal.ZERO;
	PreparedStatement st = null;
	ResultSet rs = null;
	JSONObject result = new JSONObject();
	try {

      st = conn.prepareStatement(
          " select * from ehcm_getavailed_availablelev(?, ?, ?, ?, ?,?,?,?,?,?) ");
      st.setString(1, absenceaccrual.getEhcmEmpPerinfo().getId());
      st.setString(2, startdate);
      st.setString(3, enddate);
      st.setString(4, absencetype.getId());
      st.setString(5, absenceaccrual.getClient().getId());
      st.setInt(6, 1);
      st.setString(7, "CR");
      st.setString(8, null);
      st.setBoolean(9, availabledays);
      if (subTypeId != null)
        st.setString(10, subTypeId);
      else
        st.setString(10, "");
	    log.debug("st" + st.toString());

	    rs = st.executeQuery();
	    if (rs.next()) {
		result.put("availeddays", rs.getBigDecimal("p_availedleavedays"));
		result.put("availabledays", rs.getBigDecimal("p_availableleavedays"));
		// days = rs.getBigDecimal("ehcm_checkavailableleave");
	    }
	    log.debug("days" + days);
	} catch (final Exception e) {
	    e.printStackTrace();
	    log.error("Exception in getavailableandavaileddays() Method : ", e);
	}
	return result;
    }

    /**
     * insert absence accrual details
     * 
     * @param conn
     * @param absenceaccrual
     * @param absencetype
     * @param startdate
     * @param enddate
     * @param entitilement
     * @param availeddays
     * @return
     */
  public int insertAbsenceAccrualDetails(EHCMAbsenceAccrual absenceaccrual,
	    EHCMAbsenceType absencetype, Date startdate, Date enddate, BigDecimal entitilement,
      BigDecimal availeddays, EHCMDeflookupsTypeLn subType) {
	int count = 0;
	try {

	    EHCMAbsenceAccrualDetails details = OBProvider.getInstance().get(EHCMAbsenceAccrualDetails.class);
	    details.setClient(absenceaccrual.getClient());
	    details.setOrganization(absenceaccrual.getOrganization());
	    details.setCreatedBy(absenceaccrual.getCreatedBy());
	    details.setCreationDate(new java.util.Date());
	    details.setUpdated(new java.util.Date());
	    details.setUpdatedBy(absenceaccrual.getUpdatedBy());
	    details.setEnabled(true);
	    details.setAbsenceType(absencetype);
	    details.setEhcmAbsenceAccrual(absenceaccrual);
	    details.setCalculationFrom(startdate);
	    details.setCalculationTo(enddate);
	    details.setEntitilement(entitilement);
	    details.setLeaves(availeddays);
      if (subType != null) {
        details.setSubType(subType);
      } else {
        details.setSubType(null);
      }
	    OBDal.getInstance().save(details);
	    OBDal.getInstance().flush();
	    count = 1;
	    log.debug("count" + count);

	} catch (final Exception e) {
	    log.error("Exception in insertAbsenceAccrualDetails" + e);
	}
	return count;

    }

    public String getstartdate(Connection conn, EHCMAbsenceType absencetype, EHCMAbsenceAccrual accrual) {
	SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
	String calDateHijiri = "", fstCalDateGre = "", startdate = "", hiredatehijiri = "", preyearfrstdate = "";
	int calmonth = 0, calday = 0, hiremonth = 0, hireday = 0;
	try {
	    // hiredate
	    hiredatehijiri = UtilityDAO
		    .convertTohijriDate(dateYearFormat.format(accrual.getEhcmEmpPerinfo().getHiredate()));
	    // get hijiridate for calculation date
	    calDateHijiri = UtilityDAO.convertTohijriDate(dateYearFormat.format(accrual.getCalculationDate()));
	    // get year first hijirdate based on caalculation date
	    fstCalDateGre = UtilityDAO.convertToGregorian("01-01-" + calDateHijiri.split("-")[2]);
	    preyearfrstdate = UtilityDAO
		    .convertToGregorian("01-01-" + (Integer.valueOf(calDateHijiri.split("-")[2]) - 1));
	    if (absencetype.getAccrualResetDate().equals("BHY")) {
		startdate = fstCalDateGre;

	    } else if (absencetype.getAccrualResetDate().equals("BGY")) {
		startdate = dateYearFormat.format(accrual.getCalculationDate()).split("-")[0] + "-01-01";

	    } else if (absencetype.getAccrualResetDate().equals("HAD")) {

		calmonth = Integer.valueOf(calDateHijiri.split("-")[1]);
		hiremonth = Integer.valueOf(hiredatehijiri.split("-")[1]);
		calday = Integer.valueOf(calDateHijiri.split("-")[0]);
		hireday = Integer.valueOf(hiredatehijiri.split("-")[0]);
		if ((calmonth - hiremonth) < 0) {

		    startdate = preyearfrstdate;
		} else if ((calmonth - hiremonth) == 0) {
		    if ((calday - hireday) <= 0) {
			startdate = preyearfrstdate;
		    } else {
			startdate = fstCalDateGre;
		    }
		} else if ((calmonth - hiremonth) > 0) {
		    startdate = fstCalDateGre;
		}
	    } else if (absencetype.getAccrualResetDate().equals("FS")) {
		startdate = dateYearFormat.format(accrual.getEhcmEmpPerinfo().getHiredate());
	    }

	} catch (final Exception e) {
	    log.error("Exception in getstartdate" + e);
	}
	return startdate;

    }

    /**
     * get startdate and enddate of the absence accrual
     * 
     * @param startDate
     * @param absencetype
     * @param employeeId
     * @return
     */
    public JSONObject getStartDateAndEndDate(String startDate, EHCMAbsenceType absencetype, String employeeId) {
	JSONObject result = new JSONObject();
	try {
	    SQLQuery Query = OBDal.getInstance().getSession()
		    .createSQLQuery(" select  * from ehcm_getaccrualstartenddate(?,?,?,?,?,?) ");
	    log.debug("Query:" + Query.toString());
	    Query.setParameter(0, employeeId);
	    Query.setParameter(1, startDate);
	    Query.setParameter(2, absencetype.getAccrualResetDate());
	    Query.setParameter(3, absencetype.getFrequency());
	    Query.setParameter(4, absencetype.getId());
	    Query.setParameter(5, "");// need to add subtype
	    log.debug("Query:" + Query.getQueryString());
	    log.debug("getEhcmEmpPerinfo:" + employeeId);
	    log.debug("getStartDate:" + startDate);
	    log.debug("getEndDate:" + absencetype.getAccrualResetDate());
	    log.debug("getClient:" + absencetype.getFrequency());
	    log.debug("absencetype:" + absencetype.getId());
	    log.debug("size:" + Query.getQueryString());
	    if (Query.list().size() > 0) {
		log.debug("get:" + Query.list().get(0));
		Object[] row = (Object[]) Query.list().get(0);
		log.debug("row:" + row);
		result.put("startdate", row[0]);
		result.put("enddate", row[1]);
	    }

	} catch (final Exception e) {
	    log.error("Exception in getStartDateAndEndDate() :", e);
	    return result;
	}
	return result;
    }

    /**
     * before process delete the previous absence accrual lines
     * 
     * @param absenceaccrualId
     */
    public void deletePrevAbsenceAccrual(String absenceaccrualId) {
	List<EHCMAbsenceAccrualDetails> lineDetailList = new ArrayList<EHCMAbsenceAccrualDetails>();
	try {
	    // before process need to delete the record in line tab
	    OBQuery<EHCMAbsenceAccrualDetails> det = OBDal.getInstance().createQuery(EHCMAbsenceAccrualDetails.class,
		    " as e where e.ehcmAbsenceAccrual.id=:absenceAccrualId ");
	    det.setNamedParameter("absenceAccrualId", absenceaccrualId);
	    lineDetailList = det.list();
	    if (lineDetailList.size() > 0) {
		for (EHCMAbsenceAccrualDetails details : lineDetailList) {
		    OBDal.getInstance().remove(details);
		    OBDal.getInstance().flush();
		}
	    }
	} catch (final Exception e) {
	    log.error("Exception in deletePrevAbsenceAccrual() :", e);
	}
    }

    /**
     * get accrual list from absence type
     * 
     * @param absenceaccrual
     * @return
     */
    public List<EHCMAbsenceType> getAccrualList(EHCMAbsenceAccrual absenceaccrual) {
	List<EHCMAbsenceType> absenceTypeList = new ArrayList<EHCMAbsenceType>();
	String sql = null;
	try {
	    sql = " as e where isAccrual='Y' and enabled='Y' "; // and accrualResetDate <> 'LO'

	    if (absenceaccrual.getAbsenceType() != null) {
		sql += " and id='" + absenceaccrual.getAbsenceType().getId() + "'";
	    }

	    OBQuery<EHCMAbsenceType> type = OBDal.getInstance().createQuery(EHCMAbsenceType.class,
		    sql + "  order by creationDate asc ");
	    absenceTypeList = type.list();
	    if (absenceTypeList.size() > 0) {
		return absenceTypeList;
	    }
	} catch (final Exception e) {
	    log.error("Exception in deletePrevAbsenceAccrual() :", e);
	}
	return absenceTypeList;
  }

  public List<EHCMDeflookupsTypeLn> getAbsenceSubTypeListFromRefLookup(
      EHCMAbsenceType absenceType) {
    List<EHCMDeflookupsTypeLn> absenceSubTypeList = null;
    try {

      OBQuery<EHCMDeflookupsTypeLn> absenceSubTypeQry = OBDal.getInstance().createQuery(
          EHCMDeflookupsTypeLn.class,
          " as e where e.ehcmDeflookupsType.reference='AS' and e.ehcmDeflookupsType.client.id=:clientId ");
      absenceSubTypeQry.setNamedParameter("clientId", absenceType.getClient().getId());
      absenceSubTypeList = absenceSubTypeQry.list();
      if (absenceSubTypeList.size() > 0) {
        return absenceSubTypeList;
      }
    } catch (final Exception e) {
      log.error("Exception in getAbsenceSubTypeListFromRefLookup() :", e);
    }
    return absenceSubTypeList;
  }

  public int calStartDateEndDateAndInsertAbsAccuralDetails(JSONObject availAvailableDaysRes,
      String calculationDate, EHCMAbsenceType absenceType, EHCMAbsenceAccrual absenceAccruals,
      EHCMDeflookupsTypeLn subType) {

    BigDecimal entitlement = BigDecimal.ZERO;
    BigDecimal availeddays = BigDecimal.ZERO;
    BigDecimal availabledays = BigDecimal.ZERO;

    int count = 0;
    JSONObject result = null;
    try {

      if (availAvailableDaysRes != null) {
        availeddays = new BigDecimal(availAvailableDaysRes.getString("availeddays"));
        availabledays = new BigDecimal(availAvailableDaysRes.getString("availabledays"));
      }

      entitlement = availeddays.add(availabledays);
      log.debug("entitlement" + entitlement);

      result = getStartDateAndEndDate(calculationDate, absenceType,
          absenceAccruals.getEhcmEmpPerinfo().getId());

      if (result != null) {
        count = insertAbsenceAccrualDetails(absenceAccruals, absenceType,
            yearFormat.parse(result.getString("startdate")), absenceAccruals.getCalculationDate(),
            entitlement, availeddays, subType);
      }
    } catch (final Exception e) {
      log.error("Exception in insertAbsenceAccuralDetails() :", e);
    }
    return count;
  }

  public JSONObject getAbsenceAccrualList(String clientId, String employeeId, String absenceTypeId,
      String calculationDate, JSONObject searchAttr, Connection conn) {
    PreparedStatement ps = null, ps1 = null;
    ResultSet rs = null, rs1 = null;
    String date = "";
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    JSONObject availAvailableDaysRes = null;
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
        fromQuery = new StringBuilder(), whereClause = new StringBuilder(),
        orderClause = new StringBuilder();
    BigDecimal availeddays = BigDecimal.ZERO;
    BigDecimal availabledays = BigDecimal.ZERO;
    List<EHCMDeflookupsTypeLn> absenceSubTypeList = null;
    int count = 0;
    try {

      if (calculationDate != null) {
        date = dateYearFormat
            .format(dateYearFormat.parse(UtilityDAO.convertToGregorian(calculationDate)));
      } else {
        date = dateYearFormat.format(new Date());
      }
      result.put("page", "0");
      result.put("total", "0");
      result.put("records", "0");
      result.put("rows", jsonArray);

      int offset = 0, totalPage = 0, totalRecord = 0;
      int rows = Integer.parseInt(searchAttr.getString("rows")),
          page = Integer.parseInt(searchAttr.getString("page"));
      if (page == 0)
        page = 1;

      countQuery.append("SELECT count( type.ehcm_absence_type_id) as count ");

      selectQuery.append(
          " select get_uuid() as id, type.ad_client_id ,(per.value||'-'||per.arabicfullname) as empname, per.ehcm_emp_perinfo_id, ");
      selectQuery.append(" type.ehcm_absence_type_id,type.name as absencename,type.issubtype, ");
      selectQuery.append(" (select p_yearstartdate  as p_yearstartdate"
          + "          from ehcm_getaccrualstartenddate(per.ehcm_emp_perinfo_id,"
          + "              ? ,(select to_char(type.accrual_reset_date)),"
          + "                type.frequency,type.ehcm_absence_type_id,'')) as startdate , ( select eut_convert_to_hijri(to_char(hiredate,'yyyy-MM-dd')))  as hiredate , (class.value||'-'||class.name) as category , (dept.value||'-'|| dept.name )as dept ");
      fromQuery.append("  from  ehcm_emp_perinfo per"
          + "      LEFT JOIN ehcm_gradeclass class on class.ehcm_gradeclass_id  = per.ehcm_gradeclass_id "
          + "      LEFT JOIN ehcm_employment_info info ON info.ehcm_emp_perinfo_id = per.ehcm_emp_perinfo_id "
          + "     AND info.created =  (SELECT MAX(maxinfo.created)   FROM ehcm_employment_info maxinfo     "
          + "          WHERE maxinfo.ehcm_emp_perinfo_id = info.ehcm_emp_perinfo_id            )"
          + "            LEFT JOIN ad_org dept on dept.ad_org_id  = info.deptcode,  ehcm_absence_type type ");

      whereClause.append(" where per.ad_client_id = ?  and type.ad_client_id = ? ");
      whereClause.append(" and  per.status='I' and per.isactive='Y' and type.isaccrual='Y' ");

      if (employeeId != null && !employeeId.equals("0")) {
        whereClause.append(" and  per.ehcm_emp_perinfo_id = '").append(employeeId).append("'");
      }
      if (absenceTypeId != null && !absenceTypeId.equals("0")) {
        whereClause.append(" and  type.ehcm_absence_type_id= '").append(absenceTypeId).append("'");
      }
      if (date != null && !date.equals("0")) {
        whereClause.append(" and cast('" + date + "' as date) >= per.hiredate");
      }

      orderClause.append(" order by ");
      if (searchAttr.getString("sortName").equals("employee"))
        orderClause.append(" (per.value||'-'||per.arabicfullname) ");

      else if (searchAttr.getString("sortName").equals("hiredate"))
        orderClause.append("  ( select eut_convert_to_hijri(to_char(hiredate,'yyyy-MM-dd')))  ");
      else if (searchAttr.getString("sortName").equals("absence"))
        orderClause.append("type.name");
      else if (searchAttr.getString("sortName").equals("category"))
        orderClause.append("(class.value||'-'||class.name) ");

      orderClause.append(" ").append(searchAttr.getString("sortType"));

      // Get Row Count
      ps = conn.prepareStatement(countQuery.append(fromQuery).append(whereClause).toString());
      ps.setString(1, clientId);
      ps.setString(2, clientId);
      log.debug("Penalty count:" + ps.toString());
      rs = ps.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");
      log.debug("totalRecord:" + totalRecord);
      if (totalRecord > 0) {
        totalPage = totalRecord / rows;
        if (totalRecord % rows > 0)
          totalPage += 1;
        offset = ((page - 1) * rows);
        if (page > totalPage) {
          page = totalPage;
          offset = ((page - 1) * rows);
        }
      } else {
        page = 0;
        totalPage = 0;
        offset = 0;
      }
      result.put("page", page);
      result.put("total", totalPage);
      // result.put("records", totalRecord);

      searchAttr.put("limit", rows);
      searchAttr.put("offset", offset);

      // Penalty Details
      ps1 = conn.prepareStatement((selectQuery.append(fromQuery).append(whereClause)
          .append(orderClause).append(" limit ").append(searchAttr.getInt("limit"))
          .append(" offset ").append(searchAttr.getInt("offset"))).toString());
      // ps1.setString(1, date);
      ps1.setString(1, date);
      ps1.setString(2, clientId);
      ps1.setString(3, clientId);
      log.debug("Penalty:" + ps1.toString());

      rs1 = ps1.executeQuery();
      while (rs1.next()) {

        log.debug("absencename:" + rs1.getString("absencename"));
        if (rs1.getString("issubtype").equals("Y")) {
          EHCMAbsenceType absenceType = OBDal.getInstance().get(EHCMAbsenceType.class,
              rs1.getString("ehcm_absence_type_id"));
          absenceSubTypeList = getAbsenceSubTypeListFromRefLookup(absenceType);
          for (EHCMDeflookupsTypeLn subType : absenceSubTypeList) {
            count++;
            json = new JSONObject();
            json.put("id", SequenceIdData.getUUID());
            json.put("employee", Utility.nullToEmpty(rs1.getString("empname")));
            json.put("dept", rs1.getString("dept"));
            json.put("category", rs1.getString("category"));
            json.put("absence", Utility.nullToEmpty(rs1.getString("absencename")));
            json.put("startdate",
                UtilityDAO.convertTohijriDate(Utility.nullToEmpty(rs1.getString("startdate"))));
            json.put("enddate", UtilityDAO.convertTohijriDate(date));

            availAvailableDaysRes = getAvailableAvaileddays(conn,
                rs1.getString("ehcm_emp_perinfo_id"), rs1.getString("ehcm_absence_type_id"),
                rs1.getString("ad_client_id"), date, null, false, subType.getId());
            if (availAvailableDaysRes != null) {
              availeddays = new BigDecimal(availAvailableDaysRes.getString("availeddays"));
              availabledays = new BigDecimal(availAvailableDaysRes.getString("availabledays"));
            }

            json.put("entitlement", availeddays.add(availabledays));
            json.put("leaves", availeddays);
            json.put("netentitlement", availeddays.add(availabledays).subtract(availeddays));
            json.put("subType", subType.getName());
            jsonArray.put(json);
          }
        } else {
          count++;
          json = new JSONObject();
          json.put("id", Utility.nullToEmpty(rs1.getString("id")));
          json.put("employee", Utility.nullToEmpty(rs1.getString("empname")));
          json.put("dept", rs1.getString("dept"));
          json.put("category", rs1.getString("category"));
          json.put("absence", Utility.nullToEmpty(rs1.getString("absencename")));
          json.put("startdate",
              UtilityDAO.convertTohijriDate(Utility.nullToEmpty(rs1.getString("startdate"))));
          json.put("enddate", UtilityDAO.convertTohijriDate(date));

          availAvailableDaysRes = getAvailableAvaileddays(conn,
              rs1.getString("ehcm_emp_perinfo_id"), rs1.getString("ehcm_absence_type_id"),
              rs1.getString("ad_client_id"), date, null, false, "");
          if (availAvailableDaysRes != null) {
            availeddays = new BigDecimal(availAvailableDaysRes.getString("availeddays"));
            availabledays = new BigDecimal(availAvailableDaysRes.getString("availabledays"));
          }

          json.put("entitlement", availeddays.add(availabledays));
          json.put("leaves", availeddays);
          json.put("netentitlement", availeddays.add(availabledays).subtract(availeddays));
          json.put("subType", "");
          jsonArray.put(json);
        }
      }
      result.put("rows", jsonArray);
      // result.put("records", count);
    } catch (final Exception e) {
      log.error("Exception in getAbsenceAccrualList", e);
    } finally {
      try {
        if (ps != null)
          ps.close();
        if (rs != null)
          rs.close();
      } catch (final SQLException e) {
        log.error("Exception in getAbsenceAccrualList", e);
      }
    }
    return result;
  }

  public JSONObject getAvailableAvaileddays(Connection conn, String employeeId,
      String absenceTypeId, String clientId, String startdate, String enddate,
      Boolean availabledays, String subTypeId) {
    BigDecimal days = BigDecimal.ZERO;
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject result = new JSONObject();
    try {

      st = conn.prepareStatement(
          " select * from ehcm_getavailed_availablelev(?, ?, ?, ?, ?,?,?,?,?,?) ");
      st.setString(1, employeeId);
      st.setString(2, startdate);
      st.setString(3, enddate);
      st.setString(4, absenceTypeId);
      st.setString(5, clientId);
      st.setInt(6, 1);
      st.setString(7, "CR");
      st.setString(8, null);
      st.setBoolean(9, availabledays);
      if (subTypeId != null)
        st.setString(10, subTypeId);
      else
        st.setString(10, "");
      log.debug("st" + st.toString());

      rs = st.executeQuery();
      if (rs.next()) {
        result.put("availeddays", rs.getBigDecimal("p_availedleavedays"));
        result.put("availabledays", rs.getBigDecimal("p_availableleavedays"));
      }
      log.debug("days" + days);
    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in getavailableandavaileddays() Method : ", e);
    }
    return result;
  }

  public JSONObject getEmployeeDetails(String employeeId, String lang) {
    JSONObject result = new JSONObject();
    String refName = "";

    try {
      EhcmEmpPerInfo employee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);

      result.put("hiredate",
          UtilityDAO.convertTohijriDate(yearFormat.format(employee.getHiredate())));

      if (employee.getPersonType() != null)
        result.put("employeeType", employee.getPersonType().getPersonType());
      else
        result.put("employeeType", "");

      EmploymentInfo empinfo = sa.elm.ob.hcm.util.UtilityDAO.getActiveEmployInfo(employeeId);

      if (empinfo != null) {
        if (empinfo.getDeptcode() != null) {
          result.put("departmentName",
              empinfo.getDeptcode().getSearchKey() + " - " + empinfo.getDeptcode().getName());
        } else {
          result.put("departmentName", "");
        }
        if (empinfo.getSectioncode() != null) {
          result.put("section",
              empinfo.getSectioncode().getSearchKey() + " - " + empinfo.getSectioncode().getName());
        } else {
          result.put("section", "");
        }
        if (empinfo.getGrade() != null) {
          result.put("positionGrade",
              empinfo.getGrade().getSearchKey() + " - " + empinfo.getGrade().getCommercialName());
        } else {
          result.put("positionGrade", "");
        }
        if (empinfo.getPosition() != null) {
          result.put("job", empinfo.getPosition().getEhcmJobs().getJobCode() + " - "
              + empinfo.getPosition().getJOBName().getJOBTitle());
        } else {
          result.put("job", "");
        }
        if (empinfo.getPosition() != null && empinfo.getPosition().getEhcmJobs() != null) {
          result.put("jobTitle", empinfo.getPosition().getJOBName().getJOBTitle());
        } else {
          result.put("jobTitle", "");
        }
        if (empinfo.getEmploymentgrade() != null) {
          result.put("employmentGrade", empinfo.getEmploymentgrade().getSearchKey() + " - "
              + empinfo.getEmploymentgrade().getCommercialName());
        } else {
          result.put("employmentGrade", "");
        }
        if (empinfo.getSECDeptName() != null) {
          result.put("assignedDept", empinfo.getSECDeptName());
        } else {
          result.put("assignedDept", "");
        }
      } else {
        result.put("departmentName", "");
        result.put("section", "");
        result.put("positionGrade", "");
        result.put("job", "");
        result.put("jobTitle", "");
        result.put("employmentGrade", "");
        result.put("assignedDept", "");
      }
      EHCMEmployeeStatusV employeeStatus = OBDal.getInstance().get(EHCMEmployeeStatusV.class,
          employeeId);
      if (employeeStatus != null) {
        refName = getEmployeeStatusName(employeeStatus.getStatusvalue(), lang);
        result.put("inpempStatus", refName);
        result.put("inpempStatusCode", employeeStatus.getStatusvalue());
      } else {
        result.put("inpempStatus", "");
        result.put("inpempStatusCode", "");
      }

      if (employee.getGradeClass() != null)
        result.put("inpEmployeeCategory",
            employee.getGradeClass().getSearchKey() + " - " + employee.getGradeClass().getName());
      else
        result.put("inpEmployeeCategory", "");

    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in getEmployeeDetails() Method : ", e);
    }
    return result;
  }

  public String getEmployeeStatusName(String value, String lang) {
    Connection conn = OBDal.getInstance().getConnection();
    PreparedStatement st = null;
    ResultSet rs = null;
    String refId = "57889F5818294AE6B371B3FD3369E8B3";
    String refName = "";
    try {

      st = conn.prepareStatement(
          " select coalesce(ad_ref_list_trl.name,ad_ref_list.name) as name from ad_ref_list left join ad_ref_list_trl on ad_ref_list.ad_ref_list_id = ad_ref_list_trl.ad_ref_list_id and ad_ref_list_trl.ad_language=? where ad_ref_list.ad_reference_id = ? and ad_ref_list.value = ? ");
      st.setString(1, lang);
      st.setString(2, refId);
      st.setString(3, value);

      rs = st.executeQuery();
      if (rs.next()) {
        refName = rs.getString("name");
      }

    } catch (final Exception e) {
      e.printStackTrace();
      log.error("Exception in getEmployeeStatusName : ", e);
    }
    return refName;

    }
}
