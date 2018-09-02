package sa.elm.ob.hcm.ad_forms.MedicalInsurance.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.hcm.EhcmDependents;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmMedicalInsurance;
import sa.elm.ob.hcm.ad_forms.MedicalInsurance.vo.MedicalInsuranceVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author Priyanka Ranjan 17-03-2018
 *
 */
// Medical Insurance DAO Implement file
public class MedicalInsuranceDAOImpl implements MedicalInsuranceDAO {

  private Connection conn = null;

  public MedicalInsuranceDAOImpl(Connection con) {
    this.conn = con;
  }

  public MedicalInsuranceDAOImpl() {

  }

  private static Logger LOG = Logger.getLogger(MedicalInsuranceDAOImpl.class);
  public static final String Insu_Category_RefId = "12FEA10BCAE2475987A3F14C7A44539B";
  public static final String Relationship_RefId = "FE365705D1AB4E86986C7264040976F2";

  DateFormat dateFormat = sa.elm.ob.utility.util.Utility.YearFormat;

  public JSONObject getDependents(String clientId, String employeeId, String searchTerm,
      int pagelimit, int page) {
    PreparedStatement st = null, st1 = null;
    ResultSet rs = null, rs1 = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    Connection conn = OBDal.getInstance().getConnection();
    JSONObject searchAttr = new JSONObject();
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(ehcm_dependents_v_id) as count ");
      selectQuery.append(" select ehcm_dependents_v_id,dependentname as name ");
      fromQuery.append(
          "from  ehcm_dependents_v  where ad_client_id = ? and  ehcm_emp_perinfo_id = ?   ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and dependentname ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      st.setString(2, employeeId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);
      if (totalRecords > 0) {
        searchAttr.put("limit", pagelimit);
        searchAttr.put("offset", (page - 1) * pagelimit);
        if (pagelimit > 0) {
          st = conn.prepareStatement(
              (selectQuery.append(fromQuery).append(" limit ").append(searchAttr.getInt("limit"))
                  .append(" offset ").append(searchAttr.getInt("offset"))).toString());
        } else {
          st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        }

        st.setString(1, clientId);
        st.setString(2, employeeId);

      }

      rs = st.executeQuery();

      JSONObject jsonData = new JSONObject();

      if (totalRecords > 0) {

        while (rs.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs.getString("ehcm_dependents_v_id")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs.getString("name")));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      // else
      // jsob.put("data", "");

    } catch (final Exception e) {
      LOG.error("Exception in getDependents :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();

      } catch (Exception e) {

      }
    }
    return jsob;
  }

  @SuppressWarnings("resource")
  public JSONObject getInsuranceSchema(String clientId, String searchTerm, int pagelimit,
      int page) {
    PreparedStatement st = null, st1 = null;
    ResultSet rs = null, rs1 = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    int totalRecords = 0;
    JSONObject searchAttr = new JSONObject();
    Connection conn = OBDal.getInstance().getConnection();
    try {
      jsob = new JSONObject();
      StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
          fromQuery = new StringBuilder();

      countQuery.append(" select count(EHCM_Deflookups_TypeLn_id) as count ");
      selectQuery.append(" select EHCM_Deflookups_TypeLn_id,name as name ");
      fromQuery.append(
          " from  EHCM_Deflookups_TypeLn  left join ehcm_deflookups_type on ehcm_deflookups_typeln.ehcm_deflookups_type_id = ehcm_deflookups_type.ehcm_deflookups_type_id"
              + " where reference = 'MI'  and EHCM_Deflookups_TypeLn.ad_client_id = ? ");

      if (searchTerm != null && !searchTerm.equals(""))
        fromQuery.append(" and name ilike '%" + searchTerm.toLowerCase() + "%' ");

      st = conn.prepareStatement(countQuery.append(fromQuery).toString());
      st.setString(1, clientId);
      rs = st.executeQuery();
      if (rs.next())
        totalRecords = rs.getInt("count");
      jsob.put("totalRecords", totalRecords);

      if (totalRecords > 0) {
        searchAttr.put("limit", pagelimit);
        searchAttr.put("offset", (page - 1) * pagelimit);
        if (pagelimit > 0) {
          st = conn.prepareStatement(
              (selectQuery.append(fromQuery).append(" limit ").append(searchAttr.getInt("limit"))
                  .append(" offset ").append(searchAttr.getInt("offset"))).toString());
        } else {
          st = conn.prepareStatement((selectQuery.append(fromQuery)).toString());
        }

        st.setString(1, clientId);

      }

      rs = st.executeQuery();

      JSONObject jsonData = new JSONObject();
      if (totalRecords > 0) {

        while (rs.next()) {
          jsonData = new JSONObject();
          jsonData.put("id", Utility.nullToEmpty(rs.getString("EHCM_Deflookups_TypeLn_id")));
          jsonData.put("recordIdentifier", Utility.nullToEmpty(rs.getString("name")));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      // else
      // jsob.put("data", "");

    } catch (final Exception e) {
      LOG.error("Exception in getInsuranceSchema :", e);
      return jsob;
    } finally {
      try {
        if (rs != null)
          rs.close();
        if (st != null)
          st.close();

      } catch (Exception e) {

      }
    }
    return jsob;
  }

  /**
   * 
   * @param searchKey
   * @return relationShipName
   */
  public String getRelationshipName(String searchKey) {
    String relationShipName = "";
    List<org.openbravo.model.ad.domain.List> refList = new ArrayList<org.openbravo.model.ad.domain.List>();
    try {
      OBQuery<org.openbravo.model.ad.domain.List> reference = OBDal.getInstance().createQuery(
          org.openbravo.model.ad.domain.List.class,
          "as e where e.reference.id=:refId and e.searchKey=:searchKey order by e.sequenceNumber ");
      reference.setNamedParameter("refId", Relationship_RefId);
      reference.setNamedParameter("searchKey", searchKey);
      reference.setMaxResult(1);
      refList = reference.list();
      if (refList.size() > 0) {
        relationShipName = refList.get(0).getName();
        return relationShipName;
      }
    } catch (final Exception e) {
      LOG.error("Exception in getRelationshipName", e);
      return relationShipName;
    }

    return relationShipName;
  }

  /**
   * 
   * @return Insurance Category
   */
  public List<MedicalInsuranceVO> getInsuCategoryReference() {

    List<MedicalInsuranceVO> ls = new ArrayList<MedicalInsuranceVO>();
    try {
      MedicalInsuranceVO vo = null;
      OBQuery<org.openbravo.model.ad.domain.List> reference = OBDal.getInstance().createQuery(
          org.openbravo.model.ad.domain.List.class,
          "as e where e.reference.id=:refId  order by e.sequenceNumber ");
      reference.setNamedParameter("refId", Insu_Category_RefId);
      if (reference.list() != null && reference.list().size() > 0) {
        for (org.openbravo.model.ad.domain.List listlocal : reference.list()) {
          vo = new MedicalInsuranceVO();
          vo.setSearchkey(listlocal.getSearchKey());
          vo.setName(listlocal.getName());
          ls.add(vo);
        }
      }
    } catch (final Exception e) {
      LOG.error("Exception in getInsuranceSchema", e);
      return ls;
    }

    return ls;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param vars
   * @return medicalInsuranceId
   */
  public String addMedicalInsurance(String clientId, String userId, MedicalInsuranceVO vo,
      VariablesSecureApp vars) {
    String medicalInsuranceId = "";
    String startDate = null;
    String endDate = null;
    try {

      EhcmMedicalInsurance medicalinsu = OBProvider.getInstance().get(EhcmMedicalInsurance.class);
      medicalinsu.setInsuranceCategory(vo.getInsuranceCategory());
      if (vo.getDependents() != null) {
        if (vo.getDependents().equals("0")) {
          medicalinsu.setEhcmDependents(null);
        } else {
          medicalinsu
              .setEhcmDependents(OBDal.getInstance().get(EhcmDependents.class, vo.getDependents()));
        }
      }

      medicalinsu.setInsuranceCompanyName(vo.getInsuranceCompanyName());
      medicalinsu.setEhcmDeflookupsTypeln(
          OBDal.getInstance().get(EHCMDeflookupsTypeLn.class, vo.getInsuranceSchema()));
      medicalinsu.setMemberShipNo(vo.getMemberShipNo());

      medicalinsu.setEmployee(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getEmployee()));
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        startDate = UtilityDAO.convertToGregorian(vo.getStartDate());
        medicalinsu.setStartDate(dateFormat.parse(startDate));
      }
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        endDate = UtilityDAO.convertToGregorian(vo.getEndDate());
        medicalinsu.setEndDate(dateFormat.parse(endDate));
      }

      OBDal.getInstance().save(medicalinsu);
      OBDal.getInstance().flush();

      medicalInsuranceId = medicalinsu.getId();
    } catch (Exception e) {
      LOG.error("error while addMedicalInsurance", e);
      return null;
      // TODO: handle exception
    }
    return medicalInsuranceId;
  }

  /**
   * 
   * @param clientId
   * @param userId
   * @param vo
   * @param employmentId
   * @param vars
   * @return medicalInsuranceId
   */

  public String updateMedicalInsurance(String clientId, String userId, MedicalInsuranceVO vo,
      String medInsId, VariablesSecureApp vars) {
    String medicalInsuranceId = "";
    String startDate = null;
    String endDate = null;

    try {

      EhcmMedicalInsurance medicalinsu = OBDal.getInstance().get(EhcmMedicalInsurance.class,
          medInsId);

      medicalinsu.setInsuranceCategory(vo.getInsuranceCategory());
      if (vo.getDependents() != null) {
        if (vo.getDependents().equals("0")) {
          medicalinsu.setEhcmDependents(null);
        } else {
          medicalinsu
              .setEhcmDependents(OBDal.getInstance().get(EhcmDependents.class, vo.getDependents()));
        }
      } else {
        medicalinsu.setEhcmDependents(null);
      }
      medicalinsu.setInsuranceCompanyName(vo.getInsuranceCompanyName());
      medicalinsu.setEhcmDeflookupsTypeln(
          OBDal.getInstance().get(EHCMDeflookupsTypeLn.class, vo.getInsuranceSchema()));
      medicalinsu.setMemberShipNo(vo.getMemberShipNo());

      medicalinsu.setEmployee(OBDal.getInstance().get(EhcmEmpPerInfo.class, vo.getEmployee()));
      if (vo.getStartDate() != null && !vo.getStartDate().equals("")) {
        startDate = UtilityDAO.convertToGregorian(vo.getStartDate());
        medicalinsu.setStartDate(dateFormat.parse(startDate));
      }
      if (vo.getEndDate() != null && !vo.getEndDate().equals("")) {
        endDate = UtilityDAO.convertToGregorian(vo.getEndDate());
        medicalinsu.setEndDate(dateFormat.parse(endDate));
      } else {
        medicalinsu.setEndDate(null);
      }
      OBDal.getInstance().flush();
      medicalInsuranceId = medicalinsu.getId();
    } catch (Exception e) {
      LOG.error("error while updateMedicalInsurance", e);
      return null;
      // TODO: handle exception
    }
    return medicalInsuranceId;
  }

  /**
   * 
   * @param clientId
   * @param employeeId
   * @param searchAttr
   * @param lang
   * @return Medical Insurance Lists
   */
  public JSONObject getMedicalInsuranceList(String clientId, String employeeId,
      JSONObject searchAttr, String medicalInsuranceId) {
    PreparedStatement ps = null, ps1 = null;
    ResultSet rs = null, rs1 = null;
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    String active = null;
    StringBuilder countQuery = new StringBuilder(), selectQuery = new StringBuilder(),
        fromQuery = new StringBuilder(), whereClause = new StringBuilder();
    StringBuilder orderClause = new StringBuilder();

    int rows = 0;
    int page = 0;
    int offset = 0, totalPage = 0, totalRecord = 0;
    String sql = null;
    try {
      result.put("page", "0");
      result.put("total", "0");
      result.put("records", "0");
      result.put("rows", jsonArray);

      rows = Integer.parseInt(searchAttr.getString("rows"));
      page = Integer.parseInt(searchAttr.getString("page"));
      if (page == 0)
        page = 1;

      countQuery.append("SELECT count(mediinsu.ehcm_medical_insurance_id) as count ");
      selectQuery.append(
          " select mediinsu.ehcm_medical_insurance_id as id,mediinsu.inscompanyname as insucompname,ref.value as inscategorykey,ref.name as inscategory,mediinsu.membershipno as memshipno,concat(depend.firstname,' ',depend.fathername,' ',depend.grandfather,' ',depend.fourthname,' ',depend.family) as dependents,emp.arabicname as employee , ");
      selectQuery.append(
          "eut_convert_to_hijri(to_char(mediinsu.startdate,'yyyy-MM-dd'))  as  startdate,coalesce(eut_convert_to_hijri(to_char(mediinsu.enddate,'yyyy-MM-dd')),null) as enddate");
      selectQuery
          .append(",defln.name as insuschema,defln.ehcm_deflookups_typeln_id as insuschemaId ");

      fromQuery.append(" from ehcm_medical_insurance mediinsu ");
      fromQuery.append(
          " left join ehcm_dependents depend on depend.ehcm_dependents_id = mediinsu.ehcm_dependents_id ");
      fromQuery.append(
          "  join ehcm_emp_perinfo emp on emp.ehcm_emp_perinfo_id=mediinsu.ehcm_emp_perinfo_id  "
              + " left join (select name,value from ad_ref_list where ad_reference_id='"
              + Insu_Category_RefId + "') "
              + "           ref on ref.value=mediinsu.insucategory   ");
      fromQuery.append(
          " join ehcm_deflookups_typeln defln on defln.ehcm_deflookups_typeln_id = mediinsu.ehcm_deflookups_typeln_id "
              + " join  ehcm_deflookups_type defl on defl.ehcm_deflookups_type_id = defln.ehcm_deflookups_type_id ");
      whereClause.append(" where defl.reference='MI' and mediinsu.ehcm_emp_perinfo_id  = '")
          .append(employeeId).append("' ");

      if (searchAttr.has("search") && searchAttr.getString("search").equals("true")) {
        if (searchAttr.has("insucompname")
            && !StringUtils.isEmpty(searchAttr.getString("insucompname")))
          whereClause.append("and mediinsu.inscompanyname ilike '%")
              .append(searchAttr.getString("insucompname")).append("%' ");
        if (searchAttr.has("dependents")
            && !StringUtils.isEmpty(searchAttr.getString("dependents")))
          whereClause.append("and depend.ehcm_dependents_id = '")
              .append(searchAttr.getString("dependents") + "'");
        if (searchAttr.has("insuschema")
            && !StringUtils.isEmpty(searchAttr.getString("insuschema")))
          whereClause.append("and defln.ehcm_deflookups_typeln_id = '")
              .append(searchAttr.getString("insuschema") + "'");
        if (searchAttr.has("memshipno") && !StringUtils.isEmpty(searchAttr.getString("memshipno")))
          whereClause.append("and mediinsu.membershipno ilike '%")
              .append(searchAttr.getString("memshipno")).append("%' ");
        if (searchAttr.has("inscategory")
            && !StringUtils.isEmpty(searchAttr.getString("inscategory")))
          whereClause.append("and ref.value = '").append(searchAttr.getString("inscategory") + "'");
        if (searchAttr.has("startdate") && !StringUtils.isEmpty(searchAttr.getString("startdate")))
          whereClause.append(" and mediinsu.startdate "
              + searchAttr.getString("startdate").split("##")[0] + " to_timestamp('"
              + searchAttr.getString("startdate").split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ");

        if (searchAttr.has("enddate") && !StringUtils.isEmpty(searchAttr.getString("enddate")))
          whereClause.append(" and mediinsu.enddate "
              + searchAttr.getString("enddate").split("##")[0] + " to_timestamp('"
              + searchAttr.getString("enddate").split("##")[1] + "', 'yyyy-MM-dd HH24:MI:SS') ");

        if (searchAttr.getString("sortName").equals("inscategory")) {
          orderClause.append(" order by ref.value ");
        } else if (searchAttr.getString("sortName").equals("dependents")) {
          orderClause.append(" order by depend.firstname ");
        } else if (searchAttr.getString("sortName").equals("insucompname")) {
          orderClause.append(" order by mediinsu.inscompanyname ");
        } else if (searchAttr.getString("sortName").equals("insuschema")) {
          orderClause.append(" order by defln.name ");
        } else if (searchAttr.getString("sortName").equals("memshipno")) {
          orderClause.append(" order by mediinsu.membershipno ");
        } else if (searchAttr.getString("sortName").equals("startdate")) {
          orderClause.append(" order by mediinsu.startdate ");
        } else if (searchAttr.getString("sortName").equals("enddate")) {
          orderClause.append(" order by mediinsu.enddate ");
        } else {
          orderClause.append(" order by " + searchAttr.getString("sortName"));
        }
        orderClause.append(" " + searchAttr.getString("sortType"));
      }

      // Get Row Count
      ps = conn.prepareStatement(countQuery.append(fromQuery).append(whereClause).toString());
      LOG.debug("Employee Manager history count:" + ps.toString());
      rs = ps.executeQuery();
      if (rs.next())
        totalRecord = rs.getInt("count");
      LOG.debug("offset:" + offset);
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
      result.put("records", totalRecord);

      searchAttr.put("limit", rows);
      searchAttr.put("offset", offset);
      // Penalty Details
      ps1 = conn.prepareStatement((selectQuery.append(fromQuery).append(whereClause)
          .append(orderClause).append(" limit ").append(searchAttr.getInt("limit"))
          .append(" offset ").append(searchAttr.getInt("offset"))).toString());

      LOG.debug("Employee Manager history:" + ps1.toString());
      rs1 = ps1.executeQuery();
      while (rs1.next()) {
        json = new JSONObject();
        // active = Utility.nullToEmpty(rs1.getString("isactive"));
        json.put("dependents", Utility.nullToEmpty(rs1.getString("dependents")));
        json.put("insucompname", Utility.nullToEmpty(rs1.getString("insucompname")));
        json.put("insuschema", Utility.nullToEmpty(rs1.getString("insuschema")));
        json.put("memshipno", Utility.nullToEmpty(rs1.getString("memshipno")));
        json.put("startdate", Utility.nullToEmpty(rs1.getString("startdate")));
        json.put("enddate", Utility.nullToEmpty(rs1.getString("enddate")));
        json.put("id", Utility.nullToEmpty(rs1.getString("id")));
        json.put("inscategory", Utility.nullToEmpty(rs1.getString("inscategory")));
        json.put("employee", Utility.nullToEmpty(rs1.getString("employee")));
        jsonArray.put(json);

      }
      result.put("rows", jsonArray);
    } catch (final Exception e) {
      LOG.error("Exception in getMedicalInsuranceList", e);
    } finally {
      try {
        if (ps != null)
          ps.close();
        if (rs != null)
          rs.close();
      } catch (final SQLException e) {
        LOG.error("Exception in getMedicalInsuranceList", e);
      }
    }
    return result;
  }

  /**
   * @param clientId
   * @param employeeId
   * @param searchAttr
   * @param medicalInsuranceId
   * @return JSONObject
   */
  public JSONObject getMedicalInsEditList(String clientId, String employeeId, JSONObject searchAttr,
      String medicalInsuranceId) {
    JSONObject result = new JSONObject(), json = null;
    JSONArray jsonArray = new JSONArray();
    try {
      List<EhcmMedicalInsurance> medicalInsuList = new ArrayList<EhcmMedicalInsurance>();

      OBQuery<EhcmMedicalInsurance> medicalInsu = OBDal.getInstance().createQuery(
          EhcmMedicalInsurance.class,
          " as e where e.employee.id=:employeeId  and e.id=:medicalInsuId");
      medicalInsu.setNamedParameter("employeeId", employeeId);
      medicalInsu.setNamedParameter("medicalInsuId", medicalInsuranceId);
      medicalInsu.setMaxResult(1);

      medicalInsuList = medicalInsu.list();
      if (medicalInsuList.size() > 0) {
        EhcmMedicalInsurance medicalInsurance = medicalInsuList.get(0);
        json = new JSONObject();
        if (medicalInsurance.getEhcmDependents() != null)
          json.put("dependents", medicalInsurance.getEhcmDependents().getId());
        json.put("insucompname", medicalInsurance.getInsuranceCompanyName());
        json.put("memshipno", medicalInsurance.getMemberShipNo());
        json.put("startdate",
            UtilityDAO.convertTohijriDate(dateFormat.format(medicalInsurance.getStartDate())));
        if (medicalInsurance.getEndDate() != null) {
          json.put("enddate",
              UtilityDAO.convertTohijriDate(dateFormat.format(medicalInsurance.getEndDate())));
        } else {
          json.put("enddate", "");
        }
        json.put("id", medicalInsurance.getId());
        json.put("employee", medicalInsurance.getEmployee().getId());
        json.put("inscategorykey", medicalInsurance.getInsuranceCategory());
        json.put("insuschemaId", medicalInsurance.getEhcmDeflookupsTypeln().getId());
        jsonArray.put(json);

      }
      result.put("rows", jsonArray);

    } catch (Exception e) {
      LOG.error("Exception in getMedicalInsEditList", e);
    }
    return result;
  }

  /**
   * @param medicalInsuranceId
   * @return delete the medical insurance
   */
  public boolean deleteMedicalInsurance(String medicalInsuranceId) {
    List<EhcmMedicalInsurance> ls = new ArrayList<EhcmMedicalInsurance>();
    try {
      OBQuery<EhcmMedicalInsurance> medicalInsu = OBDal.getInstance()
          .createQuery(EhcmMedicalInsurance.class, " as e where e.id=:mediinsuId ");
      medicalInsu.setNamedParameter("mediinsuId", medicalInsuranceId);
      medicalInsu.setMaxResult(1);
      ls = medicalInsu.list();
      if (ls.size() > 0) {
        OBDal.getInstance().remove(ls.get(0));
        OBDal.getInstance().flush();
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while deleteMedicalInsurance", e);
      return false;
    }
    return true;
  }

  /**
   * 
   * @param dependent
   * @param membershipno
   * @param employeeId
   * @param clientId
   * @return true if exists else false
   */
  public boolean checkInsuranceAlreadyExistsForDependent(String dependent, String membershipno,
      String employeeId, String medicalinsuranceId, String clientId) {
    List<EhcmMedicalInsurance> ls = new ArrayList<EhcmMedicalInsurance>();
    try {
      OBQuery<EhcmMedicalInsurance> count = OBDal.getInstance().createQuery(
          EhcmMedicalInsurance.class,
          " as e where e.ehcmDependents.id=:dependentId and e.memberShipNo=:membershipNo and e.employee.id=:empId and e.client.id=:clientId  "
              + (medicalinsuranceId != null ? " and e.id <>:mediinsuId " : ""));
      count.setNamedParameter("dependentId", dependent);
      count.setNamedParameter("membershipNo", membershipno);
      count.setNamedParameter("empId", employeeId);
      count.setNamedParameter("clientId", clientId);
      if (medicalinsuranceId != null) {
        count.setNamedParameter("mediinsuId", medicalinsuranceId);
      }

      ls = count.list();
      if (ls.size() > 0) {
        return true;
      }
      // return true;
    } catch (Exception e) {
      LOG.error("error while checkInsuranceAlreadyExistsForDependent", e);
      return false;
    }
    return false;
  }

}
