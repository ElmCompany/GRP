package sa.elm.ob.hcm.ad_forms.contract.dao;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.Contract;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ad_forms.contract.vo.ContractVO;
import sa.elm.ob.hcm.ad_forms.employment.vo.EmploymentVO;
import sa.elm.ob.hcm.properties.Resource;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 08/11/2016
 * 
 */
public class ContractDAO {
  private static Connection conn = null;
  VariablesSecureApp vars = null;
  private static Logger log4j = Logger.getLogger(ContractDAO.class);

  public ContractDAO(Connection con) {
    this.conn = con;
  }

  /**
   * 
   * @param ClientId
   * @return gradeList
   */
  public List<ehcmgrade> getGrade(String ClientId) {
    OBQuery<ehcmgrade> gradeList = null;
    try {
      gradeList = OBDal.getInstance().createQuery(ehcmgrade.class,
          "as e where e.client.id='" + ClientId + "' order by e.sequenceNumber ");
      gradeList.setFilterOnReadableOrganization(false);
    } catch (Exception e) {
      log4j.debug("Exception getGrade :" + e);
    }
    return gradeList.list();
  }

  /**
   * 
   * @param ClientId
   * @param searchTerm
   * @param pagelimit
   * @param page
   * @return gradeList
   */
  @SuppressWarnings("resource")
  public static JSONObject getGradeList(String clientId, String searchTerm, int pagelimit,
      int page) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct gr.ehcm_grade_id) as count ");
      selectQuery.append(" select distinct gr.ehcm_grade_id as gradeId,gr.value as value ");
      fromQuery.append(" from ehcm_grade gr where  ad_client_id=?  ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ((gr.value ilike '%" + searchTerm + "%' ))");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);

      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = conn.prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by gr.value limit ? offset ? ");
        st.setString(1, clientId);
        st.setInt(2, pagelimit);
        st.setInt(3, (page - 1) * pagelimit);
      }
      log4j.debug("gradelist:" + st.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("gradeId"));
        jsonData.put("recordIdentifier", rs.getString("value"));
        jsonArray.put(jsonData);
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getGradeList :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {
        log4j.error("Exception in getGradeList :", e);

      }
    }
    return jsob;
  }

  /**
   * 
   * @param ClientId
   * @param gradeId
   * @param searchTerm
   * @param pagelimit
   * @param page
   * @return Position List according to grade
   */
  @SuppressWarnings("resource")
  public static JSONObject getJobList(String clientId, String gradeId, String searchTerm,
      int pagelimit, int page) {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(distinct po.ehcm_position_id) as count ");
      selectQuery.append(" select distinct po.ehcm_position_id as positionId ,po.job_no as value ");
      fromQuery.append(
          " from ehcm_position po where  ad_client_id=? and ehcm_grade_id=? and po.transaction_status='I' and po.isactive='Y'");
      fromQuery.append(
          "and po.ehcm_postransactiontype_ID in (select ehcm_postransactionTYPE_id from Ehcm_PosTransactionType t where t.value not in('CAPO','TROPO'))");
      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and ((po.job_no ilike '%" + searchTerm + "%' ))");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      st.setString(2, gradeId);

      log4j.debug("qry>>" + st.toString());
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        st = conn.prepareStatement(
            (selectQuery.append(fromQuery)).toString() + " order by po.job_no limit ? offset ? ");
        st.setString(1, clientId);
        st.setString(2, gradeId);
        st.setInt(3, pagelimit);
        st.setInt(4, (page - 1) * pagelimit);
        log4j.debug("joblist:" + st.toString());
        rs = st.executeQuery();

        while (rs.next()) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", rs.getString("positionId"));
          jsonData.put("recordIdentifier", rs.getString("value"));
          jsonArray.put(jsonData);
        }
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");

    } catch (final Exception e) {
      log4j.error("Exception in getJobList :", e);
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();
      } catch (Exception e) {
        log4j.error("Exception in getGradeList :", e);
      }
    }
    return jsob;
  }

  /**
   * 
   * @param GradeId
   * @return position List corresponding to Grade
   */
  public List<EhcmPosition> getPositionList(String GradeId) {
    OBQuery<EhcmPosition> postionList = null;
    String whereClause = "";
    try {
      whereClause = "as e where e.transactionStatus='I' and e.grade.id='" + GradeId
          + "' and e.active='Y' and e.ehcmPostransactiontype.id in "
          + " ( select t.id from Ehcm_PosTransactionType t where t.searchKey not in ('CAPO','TROPO') ) ";

      postionList = OBDal.getInstance().createQuery(EhcmPosition.class, whereClause.toString());
      postionList.setFilterOnReadableOrganization(false);
    } catch (Exception e) {
      log4j.debug("Exception getPositionList :" + e);
    }
    return postionList.list();
  }

  /**
   * 
   * @param inpEmployeeId
   * @param fromDate
   * @param endDate
   * @param contractId
   * @return Y/N
   */
  public String checkPeriod(String inpEmployeeId, String fromDate, String endDate,
      String contractId) {
    PreparedStatement ps = null;
    ResultSet rs = null;
    String isExists = "N";
    try {
      ps = conn.prepareStatement(
          "select startdate from ehcm_contract where ehcm_emp_perinfo_id ='" + inpEmployeeId
              + "' and ((to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') >= to_date('"
              + fromDate + "')"
              + " and to_date(to_char(coalesce (expirydate,to_date('21-06-2058','dd-MM-yyyy')),'dd-MM-yyyy'),'dd-MM-yyyy') "
              + "<= to_date('" + endDate
              + "','dd-MM-yyyy')) or (to_date(to_char( coalesce (expirydate,to_date('21-06-2058','dd-MM-yyyy')) ,'dd-MM-yyyy'),'dd-MM-yyyy') "
              + ">= to_date('" + fromDate
              + "') and to_date(to_char(startdate,'dd-MM-yyyy'),'dd-MM-yyyy') <= to_date('"
              + endDate + "','dd-MM-yyyy'))) and ehcm_contract_id not in('" + contractId + "') ");
      log4j.debug("checkQuery:" + ps.toString());
      rs = ps.executeQuery();
      if (rs.next()) {
        isExists = "Y";
      }
    } catch (Exception e) {
      log4j.debug("Exception checkPeriod :" + e);
    }
    return isExists;
  }

  /**
   * 
   * @param contractId
   * @return success message
   */
  public boolean deleteContract(String contractId) {
    PreparedStatement st = null;
    try {

      conn.prepareStatement(
          "DELETE FROM ehcm_contract_line WHERE ehcm_contract_id = '" + contractId + "'")
          .executeUpdate();
      st = conn.prepareStatement("DELETE FROM ehcm_contract WHERE ehcm_contract_id = ?");

      st.setString(1, contractId);
      st.executeUpdate();

    } catch (final SQLException e) {
      log4j.error("", e);
      return false;
    } catch (final Exception e) {
      log4j.error("", e);
      return false;
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
        return false;
      }
    }
    return true;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param contractVo
   * @return contractId
   */
  public String addContract(String clientId, String userId, ContractVO vo) {
    String contractId = "";
    try {
      OBContext.setAdminMode();
      Contract contract = OBProvider.getInstance().get(Contract.class);
      contract.setContracttype(vo.getContractType());

      contract.setTrxstatus(vo.getTrxStatus());
      contract.setContractNo(vo.getContractNo());
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        contract.setStartDate(convertGregorian(vo.getStartDate()));
      }
      contract.setDuration(Long.valueOf(vo.getDuration()));
      contract.setDurationType(vo.getDurationType());
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        contract.setExpirydate(convertGregorian(vo.getEndDate()));
      }
      contract.setJobdescription(vo.getJobDescription());
      contract.setContractdesc(vo.getContractDescription());
      contract.setGrade(OBDal.getInstance().get(ehcmgrade.class, vo.getGrade()));
      // employInfo.setJobGroup(OBDal.getInstance().get(EhcmJobGroup.class, vo.getj))
      if (vo.getJobNo() != null && !vo.getJobNo().equals("0") && !vo.getJobNo().equals("")) {
        contract.setPosition(OBDal.getInstance().get(EhcmPosition.class, vo.getJobNo()));
      }
      contract.setLetterNo(vo.getLetterNo());
      if (vo.getLetterDate() != null && !vo.getLetterDate().equals("")) {
        contract.setLetterDate(convertGregorian(vo.getLetterDate()));
      }
      contract.setDecisionNo(vo.getDecisionNo());
      if (vo.getTrxStatus().equals("ISS")) {
        contract.setDecisionDate(new Date());
      }
      contract.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getEmployeeId()));
      if (vo.getAnnualBalance() != null && !vo.getAnnualBalance().equals("")) {
        contract.setAnnualbalance(Long.valueOf(vo.getAnnualBalance()));
      } else {
        contract.setAnnualbalance(Long.valueOf(0));
      }
      OBDal.getInstance().save(contract);
      OBDal.getInstance().flush();
      contractId = contract.getId();
    } catch (Exception e) {
      log4j.error("error while addContract", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return contractId;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param ContractId
   * @return ContractId
   */

  public String updateContract(String clientId, String userId, ContractVO vo, String ContractId) {
    String contractId = "";
    try {
      OBContext.setAdminMode();
      Contract contract = OBDal.getInstance().get(Contract.class, ContractId);
      contract.setContracttype(vo.getContractType());
      contract.setTrxstatus(vo.getTrxStatus());
      contract.setContractNo(vo.getContractNo());
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        contract.setStartDate(convertGregorian(vo.getStartDate()));
      }
      contract.setDuration(Long.valueOf(vo.getDuration()));
      contract.setDurationType(vo.getDurationType());
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        contract.setExpirydate(convertGregorian(vo.getEndDate()));
      }
      contract.setJobdescription(vo.getJobDescription());
      contract.setContractdesc(vo.getContractDescription());
      contract.setGrade(OBDal.getInstance().get(ehcmgrade.class, vo.getGrade()));
      // employInfo.setJobGroup(OBDal.getInstance().get(EhcmJobGroup.class, vo.getj))
      if (vo.getJobNo() != null && !vo.getJobNo().equals("0") && !vo.getJobNo().equals("")) {
        contract.setPosition(OBDal.getInstance().get(EhcmPosition.class, vo.getJobNo()));
      }
      contract.setLetterNo(vo.getLetterNo());
      if (vo.getLetterDate() != null && !vo.getLetterDate().equals("")) {
        contract.setLetterDate(convertGregorian(vo.getLetterDate()));
      }
      contract.setDecisionNo(vo.getDecisionNo());
      if (vo.getTrxStatus().equals("ISS")) {
        contract.setDecisionDate(new Date());
      }
      contract.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getEmployeeId()));
      if (vo.getAnnualBalance() != null && !vo.getAnnualBalance().equals("")) {
        contract.setAnnualbalance(Long.valueOf(vo.getAnnualBalance()));
      } else {
        contract.setAnnualbalance(Long.valueOf(0));
      }
      OBDal.getInstance().save(contract);
      OBDal.getInstance().flush();
      contractId = contract.getId();
    } catch (Exception e) {
      log4j.error("error while updateContract", e);
      return null;
      // TODO: handle exception
    } finally {
      OBContext.restorePreviousMode();
    }
    return contractId;
  }

  /**
   * 
   * @param hijridate
   * @return gregorian Date
   */
  public Date convertGregorian(String hijridate) {
    log4j.debug("hi:" + hijridate);
    String gregDate = Utility.convertToGregorian(hijridate);
    log4j.debug("gregDate:" + gregDate);
    Date greDate = null;
    try {
      DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
      greDate = df1.parse(gregDate);
      log4j.debug("greDate:" + greDate);
    } catch (Exception e) {
      log4j.error("Exception in convertGregorian", e);

    }
    return greDate;

  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param vo
   * @param limit
   * @param offset
   * @param sortColName
   * @param sortColType
   * @param searchFlag
   * @return ContractList
   */
  public List<ContractVO> getContractList(String clientId, String employeeId, ContractVO vo,
      int limit, int offset, String sortColName, String sortColType, String searchFlag,
      String lang) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<ContractVO> ls = new ArrayList<ContractVO>();
    String sqlQuery = "";
    String date = "";
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      sqlQuery = " select ctrct.ehcm_contract_id as id,ctrct.contracttype as ctype,grade.value as grade,pos.job_no as job , ctrct.contract_no as ctrctno, ctrct.duration as duration ,"
          + "  ctrct.letter_no as letterno,ctrct.decision_no as decsionno, "
          + " ctrct.startdate as startdate, ctrct.expirydate as expirydate ,ctrct.letter_date as letterDate,ctrct.trxstatus "
          + " from ehcm_contract ctrct left join ehcm_grade grade on grade.ehcm_grade_id=ctrct.ehcm_grade_id "
          + " left join ehcm_position pos on pos.ehcm_position_id =ctrct.ehcm_position_id where ctrct.ehcm_emp_perinfo_id=? ";
      if (searchFlag.equals("true")) {
        if (vo.getGrade() != null)
          sqlQuery += " and grade.value ilike '%" + vo.getGrade() + "%'";
        if (vo.getJobNo() != null)
          sqlQuery += " and pos.job_no ilike '%" + vo.getJobNo() + "%'";
        if (vo.getContractType() != null)
          sqlQuery += " and ctrct.contracttype ilike '%" + vo.getContractType() + "%'";
        if (vo.getContractNo() != null)
          sqlQuery += " and ctrct.contract_no ilike '%" + vo.getContractNo() + "%'";
        if (vo.getDuration() != null)
          sqlQuery += " and ctrct.duration= '" + vo.getDuration() + "'";
        if (vo.getLetterNo() != null)
          sqlQuery += " and ctrct.letter_no ilike '%" + vo.getLetterNo() + "%'";
        if (vo.getDecisionNo() != null)
          sqlQuery += " and ctrct.decision_no ilike '%" + vo.getDecisionNo() + "%'";
        if (!StringUtils.isEmpty(vo.getStartDate()))
          sqlQuery += " and ctrct.startdate " + vo.getStartDate().split("##")[0] + " to_timestamp('"
              + vo.getStartDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEndDate()))
          sqlQuery += " and ctrct.expirydate " + vo.getEndDate().split("##")[0] + " to_timestamp('"
              + vo.getEndDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(vo.getLetterDate()))
          sqlQuery += " and ctrct.letter_date " + vo.getLetterDate().split("##")[0]
              + " to_timestamp('" + vo.getLetterDate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";
      }

      if (sortColName != null && sortColName.equals("contractType"))
        sqlQuery += " order by ctype  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("contractNo"))
        sqlQuery += " order by ctrctno " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("Duration"))
        sqlQuery += " order by duration " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("grade"))
        sqlQuery += " order by grade " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("jobno"))
        sqlQuery += " order by job " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("letterNo"))
        sqlQuery += " order by letterno " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("decisionNo"))
        sqlQuery += " order by decsionno  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("StartDate"))
        sqlQuery += " order by startdate  " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("expiryDate"))
        sqlQuery += " order by expirydate  " + sortColType + " limit " + limit + " offset "
            + offset;
      else if (sortColName != null && sortColName.equals("letterDate"))
        sqlQuery += " order by letterDate  " + sortColType + " limit " + limit + " offset "
            + offset;
      else
        sqlQuery += " order by ctrctno " + sortColType + " limit " + limit + " offset " + offset;
      log4j.debug("DAO select Query:" + sqlQuery + ">> employeeId:" + employeeId);
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, employeeId);
      rs = st.executeQuery();
      while (rs.next()) {
        ContractVO cVO = new ContractVO();
        String ctype = "";
        cVO.setContractId(Utility.nullToEmpty(rs.getString("id")));
        if (rs.getString("ctype").equals("P"))
          ctype = Resource.getProperty("hcm.periodic.contract", lang);
        if (rs.getString("ctype").equals("T"))
          ctype = Resource.getProperty("hcm.task.contract", lang);
        if (rs.getString("ctype").equals("R"))
          ctype = Resource.getProperty("hcm.renewal.contract", lang);
        if (rs.getString("ctype").equals("TR"))
          ctype = Resource.getProperty("hcm.terminate.contract", lang);

        cVO.setContractType(Utility.nullToEmpty(ctype));
        cVO.setContractNo(Utility.nullToEmpty(rs.getString("ctrctno")));
        cVO.setDuration(Utility.nullToEmpty(rs.getString("duration")));
        cVO.setGrade(Utility.nullToEmpty(rs.getString("grade")));
        cVO.setJobNo(Utility.nullToEmpty(rs.getString("job")));
        cVO.setLetterNo(Utility.nullToEmpty(rs.getString("letterno")));
        cVO.setDecisionNo(Utility.nullToEmpty(rs.getString("decsionno")));
        if (rs.getString("trxstatus").equalsIgnoreCase("ISS")) {
          cVO.setTrxStatus(Resource.getProperty("hcm.issued", lang));
        } else
          cVO.setTrxStatus(Resource.getProperty("hcm.underproc", lang));
        if (rs.getDate("startdate") != null) {
          date = df.format(rs.getDate("startdate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          cVO.setStartDate(date);
        } else
          cVO.setStartDate(null);
        if (rs.getDate("expirydate") != null
            && !StringUtils.isEmpty(rs.getDate("expirydate").toString())) {
          date = df.format(rs.getDate("expirydate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          cVO.setEndDate(date);
        } else
          cVO.setEndDate("");

        if (rs.getDate("letterDate") != null
            && !StringUtils.isEmpty(rs.getDate("letterDate").toString())) {
          date = df.format(rs.getDate("letterDate"));
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          cVO.setLetterDate(date);
        } else
          cVO.setLetterDate(null);
        cVO.setStatus(rs.getString("trxstatus"));
        ls.add(cVO);
      }
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return ls;
  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param searchFlag
   * @param vo
   * @return ContractCount
   */
  public int getContractCount(String clientId, String employeeId, String searchFlag,
      ContractVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = " select count(*) as totalRecord from ehcm_contract ctrct left join ehcm_grade grade on grade.ehcm_grade_id=ctrct.ehcm_grade_id "
          + " left join ehcm_position pos on pos.ehcm_position_id =ctrct.ehcm_position_id where ctrct.ehcm_emp_perinfo_id=? ";
      if (searchFlag.equals("true")) {
        if (vo.getGrade() != null)
          sqlQuery += " and grade.value ilike '%" + vo.getGrade() + "%'";
        if (vo.getJobNo() != null)
          sqlQuery += " and pos.job_no ilike '%" + vo.getJobNo() + "%'";
        if (vo.getContractType() != null)
          sqlQuery += " and ctrct.contracttype ilike '%" + vo.getContractType() + "%'";
        if (vo.getContractNo() != null)
          sqlQuery += " and ctrct.contract_no ilike '%" + vo.getContractNo() + "%'";
        if (vo.getDuration() != null)
          sqlQuery += " and ctrct.duration ='" + vo.getDuration() + "'";
        if (vo.getLetterNo() != null)
          sqlQuery += " and ctrct.letter_no ilike '%" + vo.getLetterNo() + "%'";
        if (vo.getDecisionNo() != null)
          sqlQuery += " and ctrct.decision_no ilike '%" + vo.getDecisionNo() + "%'";

        if (!StringUtils.isEmpty(vo.getStartDate()))
          sqlQuery += " and ctrct.startdate " + vo.getStartDate().split("##")[0] + " to_timestamp('"
              + vo.getStartDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";

        if (!StringUtils.isEmpty(vo.getEndDate()))
          sqlQuery += " and ctrct.expirydate " + vo.getEndDate().split("##")[0] + " to_timestamp('"
              + vo.getEndDate().split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ";
        if (!StringUtils.isEmpty(vo.getLetterDate()))
          sqlQuery += " and ctrct.letter_date " + vo.getLetterDate().split("##")[0]
              + " to_timestamp('" + vo.getLetterDate().split("##")[1]
              + "', 'yyyy-MM-dd HH24:MI:SS') ";

      }
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, employeeId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("totalRecord");
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return totalRecord;
  }

  /**
   * 
   * @param clientId
   * @param contractId
   * @param searchFlag
   * @param vo
   * @return ContractCount
   */
  public int getSalaryCount(String clientId, String contractId, String searchFlag, ContractVO vo) {
    PreparedStatement st = null;
    ResultSet rs = null;
    int totalRecord = 0;
    String sqlQuery = "";
    try {
      sqlQuery = " select count(*) as totalRecord  from ehcm_contract_line ln where ln.ehcm_contract_id = ? ";
      if (searchFlag.equals("true")) {
        if (vo.getElement() != null)
          sqlQuery += " and ln.payroll ilike '%" + vo.getElement() + "%'";
        if (vo.getPercentage() != null)
          sqlQuery += " and ln.percentage ilike '%" + vo.getPercentage() + "%'";
        if (vo.getValue() != null)
          sqlQuery += " and ln.contractvalue ='" + vo.getValue() + "'";

      }
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, contractId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("totalRecord");
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return totalRecord;
  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param vo
   * @param limit
   * @param offset
   * @param sortColName
   * @param sortColType
   * @param searchFlag
   * @return ContractList
   */
  public List<ContractVO> getSalaryList(String clientId, String contractId, ContractVO vo,
      int limit, int offset, String sortColName, String sortColType, String searchFlag) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<ContractVO> ls = new ArrayList<ContractVO>();
    String sqlQuery = "";
    try {
      sqlQuery = " select ln.ehcm_contract_line_id as id,ln.payroll,ln.contractvalue,ln.percentage from ehcm_contract_line ln where ln.ehcm_contract_id =? ";
      if (searchFlag.equals("true")) {
        if (vo.getElement() != null)
          sqlQuery += " and ln.payroll ilike '%" + vo.getElement() + "%'";
        if (vo.getPercentage() != null)
          sqlQuery += " and ln.percentage ilike '%" + vo.getPercentage() + "%'";
        if (vo.getValue() != null)
          sqlQuery += " and ln.contractvalue ='" + vo.getValue() + "'";

      }
      if (sortColName != null && sortColName.equals("element"))
        sqlQuery += " order by payroll " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("Percentage"))
        sqlQuery += " order by percentage " + sortColType + " limit " + limit + " offset " + offset;
      else if (sortColName != null && sortColName.equals("value"))
        sqlQuery += " order by contractvalue " + sortColType + " limit " + limit + " offset "
            + offset;
      else
        sqlQuery += " order by created  " + sortColType + " limit " + limit + " offset " + offset;
      log4j.debug("DAO select Query:" + sqlQuery + ">> contractId:" + contractId);
      st = conn.prepareStatement(sqlQuery);
      st.setString(1, contractId);
      rs = st.executeQuery();
      while (rs.next()) {
        ContractVO cVO = new ContractVO();
        cVO.setSalaryId(rs.getString("id"));
        cVO.setElement(rs.getString("payroll"));
        cVO.setValue(String.valueOf(rs.getInt("contractvalue")));
        cVO.setPercentage(rs.getString("percentage"));
        ls.add(cVO);
      }
    } catch (final SQLException e) {
      log4j.error("", e);
    } catch (final Exception e) {
      log4j.error("", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("", e);
      }
    }
    return ls;
  }

  /**
   * 
   * 
   * @param tab
   * @param employeeId
   * @return TabUrl
   */
  public String redirectStr(String tab, String employeeId, String empStatus,
      String employeeStatus) {
    String redirStr = "";
    try {
      log4j.debug("tab:" + tab);
      String url = "inpEmployeeId=" + employeeId + "&inpEmpStatus=" + empStatus
          + "&inpEmployeeStatus=" + employeeStatus;
      if (tab.equals("EMP")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView&" + url;
      } else if (tab.equals("EMPINF")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&" + url;
      } else if (tab.equals("EMPADD")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView&"
            + url;
      } else if (tab.equals("Dependent")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?" + url;
      } else if (tab.equals("EMPCTRCT")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=EditView&" + url;
      } else if (tab.equals("EMPQUAL")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&"
            + url;
      } else if (tab.equals("Asset")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.asset.header/Asset?" + url;
      } else if (tab.equals("PREEMP")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment?" + url;
      } else if (tab.equals("DOC")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.documents.header/Documents?" + url;
      } else if (tab.equals("MEDIN")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance?" + url;
      } else if (tab.equals("PERPAYMETHOD")) {
        redirStr = "/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod?"
            + url;
      }
    } catch (final Exception e) {
      log4j.error("Exception in redirectStr", e);
    }
    return redirStr;
  }

  /**
   * 
   * @param clientId
   * @param searchAttr
   * @return ContractEmployeeList
   */
  public List<EmploymentVO> getSearchEmployee(String clientId, JSONObject searchAttr) {
    PreparedStatement st = null;
    ResultSet rs = null;
    List<EmploymentVO> ls = new ArrayList<EmploymentVO>();
    EmploymentVO eVO = null;
    StringBuilder whereClause = new StringBuilder();
    String orderClause = "";
    try {

      whereClause
          .append("where gclass.iscontract ='Y' and info.ad_client_id = '" + clientId + "' ");

      if (searchAttr.has("fname"))
        whereClause
            .append(" and concat(info.name,' ',info.fathername,' ',info.grandfathername) ilike '%")
            .append(searchAttr.getString("fname")).append("%'");
      if (searchAttr.has("aname"))
        whereClause
            .append(
                " and concat(info.arabicname,' ',info.arabicfatname,' ',info.arbgrafaname) ilike '%")
            .append(searchAttr.getString("aname")).append("%'");
      if (searchAttr.has("empno"))
        whereClause.append(" and info.value ilike '%").append(searchAttr.getString("empno"))
            .append("%'");
      if (searchAttr.getString("sortName").equals("empno")) {
        orderClause = " order by info.value asc";
      } else {
        orderClause = " order by " + searchAttr.getString("sortName") + " asc";
      }

      // Employee Details
      StringBuilder sqlQuery = new StringBuilder(
          " select info.ehcm_emp_perinfo_id as id,concat(info.name,' ',info.fathername,' ',info.grandfathername) as fname ,concat(info.arabicname,' ',info.arabicfatname,' ',info.arbgrafaname) as aname,info.value from ehcm_emp_perinfo info"
              + " left join ehcm_gradeclass gclass on gclass.ehcm_gradeclass_id=info.ehcm_gradeclass_id ");
      sqlQuery.append(whereClause);
      sqlQuery.append(orderClause);
      sqlQuery.append(" limit 10 offset 0");
      log4j.debug("employee no search:" + sqlQuery);
      st = conn.prepareStatement(sqlQuery.toString());
      rs = st.executeQuery();
      while (rs.next()) {
        eVO = new EmploymentVO();
        eVO.setEmployeeId(rs.getString("id"));
        eVO.setEmploymentNo(rs.getString("value"));
        eVO.setFullName(Utility.nullToEmpty(rs.getString("fname")));
        eVO.setArabicName(Utility.nullToEmpty(rs.getString("aname")));
        ls.add(eVO);
      }
    } catch (final Exception e) {
      log4j.error("Exception in getSearchEmployee", e);
    } finally {
      try {
        st.close();
        rs.close();
      } catch (final SQLException e) {
        log4j.error("Exception in getSearchEmployee", e);
      }
    }
    return ls;
  }

  public ContractVO checkcontractval(String Clientid, String empid, String inpContractId) {

    PreparedStatement ps, st = null;
    ResultSet rs = null;
    String isExists = "N";
    String strQuery = "";
    SQLQuery query = null;
    int minservice = 0;
    int maxservice = 0;
    String startdate = null;
    String expirydate = null;
    BigInteger days = new BigInteger("0");
    BigInteger day = new BigInteger("0");
    ContractVO vo = null;
    SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");

    JSONObject result = new JSONObject();
    try {
      ps = conn.prepareStatement(
          "select count(trxstatus) as trx,startdate,expirydate from ehcm_contract where (trxstatus = 'Issued' or ehcm_contract_id =?) and ehcm_emp_perinfo_id = ? group by startdate,expirydate");

      ps.setString(1, inpContractId);
      ps.setString(2, empid);
      rs = ps.executeQuery();
      log4j.debug("query" + ps.toString());
      while (rs.next()) {

        startdate = rs.getDate("startdate").toString();

        expirydate = rs.getDate("expirydate").toString();

      }

      expirydate = convertTohijriDate(dateYearFormat.format(dateYearFormat.parse(expirydate)));

      expirydate = getOneDayAddHijiriDate(
          expirydate.split("-")[2] + expirydate.split("-")[1] + expirydate.split("-")[0], Clientid);

      Date strStartDate = dateYearFormat.parse(startdate);

      Date strExpiryDate = dateYearFormat
          .parse(sa.elm.ob.hcm.util.UtilityDAO.convertToGregorian_tochar((expirydate)));

      result = sa.elm.ob.hcm.util.UtilityDAO.CalculateAge(strStartDate, strExpiryDate, Clientid);
      int Years = Integer.parseInt(result.getString("Years"));
      int Months = Integer.parseInt(result.getString("Months"));
      int Days = Integer.parseInt(result.getString("Days"));

      st = conn.prepareStatement(
          "select em_ehcm_mincontractservice,em_ehcm_maxcontractservice from ad_client where ad_client_id = ?");
      st.setString(1, Clientid);
      rs = st.executeQuery();
      if (rs.next()) {
        minservice = rs.getInt("em_ehcm_mincontractservice");
        maxservice = rs.getInt("em_ehcm_maxcontractservice");

      }
      if (Years >= minservice && Years <= maxservice) {
        if (Years == maxservice) {
          if (Months > 0 || Days > 0) {
            isExists = "Y";
          } else {
            isExists = "N";
          }
        }

      } else {
        isExists = "Y";
      }
      vo = new ContractVO();
      vo.setMinservice(minservice);
      vo.setMaxservice(maxservice);
      vo.setValue(isExists);

    } catch (Exception e) {
      log4j.debug("Exception checkcontractval :" + e);
    }
    return vo;
  }

  public String convertTohijriDate(String gregDate) {
    String hijriDate = "";
    try {
      SQLQuery gradeQuery = OBDal.getInstance().getSession()
          .createSQLQuery("select eut_convert_to_hijri(to_char(to_timestamp('" + gregDate
              + "','YYYY-MM-DD HH24:MI:SS'),'YYYY-MM-DD  HH24:MI:SS'))");
      if (gradeQuery.list().size() > 0) {
        Object row = (Object) gradeQuery.list().get(0);
        hijriDate = (String) row;
      }
    }

    catch (final Exception e) {
      log4j.error("Exception in convertTohijriDate() Method : ", e);
      return "0";
    }
    return hijriDate;
  }

  public String getOneDayAddHijiriDate(String gregoriandate, String clientId) {
    Query query = null;
    String strQuery = "", startdate = "";
    try {

      strQuery = " select  hijri_date from eut_hijri_dates  where hijri_date > '" + gregoriandate
          + "' order by hijri_date asc limit 1 ";
      query = OBDal.getInstance().getSession().createSQLQuery(strQuery);
      if (query != null && query.list().size() > 0) {
        Object row = query.list().get(0);
        startdate = (String) row;

        startdate = startdate.substring(6, 8) + "-" + startdate.substring(4, 6) + "-"
            + startdate.substring(0, 4);
      }
    } catch (Exception e) {
      log4j.error("Exception in getOneDayAddHijiriDate", e);
      e.printStackTrace();
    }
    return startdate;
  }

  public static int yearValidation(String strstartDate, String strendDate) {

    int years = 0;
    int months = 0;
    int days = 0;
    Boolean validation = Boolean.TRUE;

    try {

      int startyear = Integer.parseInt(strstartDate.split("-")[2]);
      int startmonth = Integer.parseInt(strstartDate.split("-")[1]);
      int startdate = Integer.parseInt(strstartDate.split("-")[0]);
      log4j.debug(
          "startyear>>startmonth>>startdate" + startyear + "-" + startmonth + "-" + startdate); // 17-03-1442
      int endyear = Integer.parseInt(strendDate.split("-")[2]);
      int endmonth = Integer.parseInt(strendDate.split("-")[1]);
      int enddate = Integer.parseInt(strendDate.split("-")[0]); // 15-03-1442

      years = endyear - startyear;
      if (years == 0) {
        validation = Boolean.TRUE;
      } else if (years >= 2) {
        log4j.debug("Incyear" + years);
        validation = Boolean.FALSE;
      } else if (years == 1) { // check with year diff 1
        months = endmonth - startmonth;
        if (months < 0) {// check months with in one year ,yes then allow
          validation = Boolean.TRUE;
        } else if (months > 0) {// check months with in one year ,no then dnt allow
          validation = Boolean.FALSE;
        } else if (months == 0) { // same month check dates
          days = enddate - startdate;
          if (days > 0) {
            validation = Boolean.FALSE;
          } else if (days < 0) {
            validation = Boolean.TRUE;
          } else if (days == 0) {
            validation = Boolean.FALSE;
          }
        }
      } else {
        validation = Boolean.TRUE;
      }

    } catch (Exception e) {
      log4j.error("Exception in yearValidation", e);
      e.printStackTrace();
    }
    return years;
  }

  public ContractVO getagevalue(String clientId) {
    ContractVO vo = null;
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = conn.prepareStatement(
          "select em_ehcm_mincontractservice,em_ehcm_maxcontractservice from ad_client where ad_client_id= ?");
      st.setString(1, clientId);
      rs = st.executeQuery();
      if (rs.next()) {
        vo = new ContractVO();
        vo.setContractNo((rs.getString("em_ehcm_mincontractservice")));
        vo.setContractType((rs.getString("em_ehcm_maxcontractservice")));

      }
    } catch (final Exception e) {
      log4j.error("Exception in getagevalue", e);
    }
    return vo;
  }
}
