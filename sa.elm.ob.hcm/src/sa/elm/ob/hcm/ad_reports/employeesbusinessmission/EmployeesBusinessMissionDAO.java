package sa.elm.ob.hcm.ad_reports.employeesbusinessmission;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EHCMMissionCategory;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;

public class EmployeesBusinessMissionDAO {

  private static Logger log4j = Logger.getLogger(EmployeesBusinessMissionDAO.class);

  public static JSONObject getEmployeesList(String clientId) throws JSONException {
    String query = " as e where e.client.id = ? and e.status in ('I') and e.employmentStatus not in ('TE') and e.enabled='Y' order by e.searchKey ";
    List<EhcmEmpPerInfo> empLs = null;
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(clientId);
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      OBQuery<EhcmEmpPerInfo> empList = OBDal.getInstance().createQuery(EhcmEmpPerInfo.class, query,
          parametersList);
      empLs = empList.list();

      if (empLs.size() > 0) {
        for (EhcmEmpPerInfo empVO : empLs) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", empVO.getId());
          jsonData.put("empName", (empVO.getSearchKey() + "-" + empVO.getArabicfullname()));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      log4j.error("Exception while getEmployeesList:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject getDepartmentList(String clientId, String empId) throws JSONException {
    StringBuffer query = null;
    Query deptQuery = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      query = new StringBuffer();
      query.append(" select distinct dept.id as deptid, dept.searchKey||'-'||dept.name as deptname "
          + " from Ehcm_Employment_Info ement " + " left join ement.position pos "
          + " left join pos.department dept " + " where ement.client.id=:clientId ");
      if (empId != null && !("null").equals(empId))
        query.append(" and ement.ehcmEmpPerinfo.id=:empId ");
      deptQuery = OBDal.getInstance().getSession().createQuery(query.toString());
      deptQuery.setParameter("clientId", clientId);
      if (empId != null && !("null").equals(empId))
        deptQuery.setParameter("empId", empId);
      log4j.debug(" Query : " + query.toString());
      if (deptQuery != null) {
        if (deptQuery.list().size() > 0) {
          for (Iterator iterator = deptQuery.iterate(); iterator.hasNext();) {
            Object[] objects = (Object[]) iterator.next();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", objects[0] == null ? "" : objects[0].toString());
            jsonData.put("recordIdentifier", objects[1] == null ? "" : objects[1].toString());
            jsonArray.put(jsonData);
          }
        }
      }
      jsob.put("totalRecords", deptQuery.list().size());
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      log4j.error("Exception while getDepartmentList:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  public static JSONObject getMissionCatg(String clientId) throws JSONException {
    String query = " as e where e.client.id = ? ";
    List<EHCMMissionCategory> catgLs = null;
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(clientId);
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      OBQuery<EHCMMissionCategory> misCatg = OBDal.getInstance()
          .createQuery(EHCMMissionCategory.class, query, parametersList);
      catgLs = misCatg.list();

      if (catgLs.size() > 0) {
        for (EHCMMissionCategory catgVO : catgLs) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", catgVO.getId());
          jsonData.put("catgName", catgVO.getName());
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      log4j.error("Exception while getMissionCatg:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  @SuppressWarnings("rawtypes")
  public static JSONObject getDepartmentListUsingLeave(String empId) throws JSONException {
    StringBuffer query = null;
    Query deptQuery = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      query = new StringBuffer();
      query.append(" select distinct dept.id as deptid, dept.name as deptname ,ement.creationDate  "
          + " from Ehcm_Employment_Info ement " + " left join ement.position pos "
          + " left join pos.department dept " + " where ement.ehcmEmpPerinfo.id=:empId"
          + " order by ement.creationDate desc  ");
      deptQuery = OBDal.getInstance().getSession().createQuery(query.toString());
      deptQuery.setParameter("empId", empId);
      deptQuery.setMaxResults(1);
      log4j.debug(" Query : " + query.toString());
      if (deptQuery != null) {
        if (deptQuery.list().size() > 0) {
          for (Iterator iterator = deptQuery.iterate(); iterator.hasNext();) {
            Object[] objects = (Object[]) iterator.next();
            JSONObject jsonData = new JSONObject();
            jsonData.put("id", objects[0] == null ? "" : objects[0].toString());
            jsonData.put("recordIdentifier", objects[1] == null ? "" : objects[1].toString());
            jsonArray.put(jsonData);
          }
        }
      }
      jsob.put("totalRecords", deptQuery.list().size());
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      log4j.error("Exception while getDepartmentListUsingLeave:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  /**
   * Get User's Department from employee details
   * 
   * @param userId
   * @return String
   */
  @SuppressWarnings("rawtypes")
  public static String getUserDepartment(String userId) {
    String query = null;
    Query usrDeptQuery = null;
    String userDepartment = "";
    try {
      OBContext.setAdminMode();
      /*
       * query = "select org.name as userdepartment from ehcm_emp_perinfo info " +
       * " join (select ehcm_emp_perinfo_id, employeeno, ehcm_position_id from ehcm_employment_info "
       * +
       * "  where startdate = (select max(startdate) from ehcm_employment_info where ehcm_emp_perinfo_id =(select em_ehcm_emp_perinfo_id from c_bpartner bp "
       * +
       * " left join ad_user usr on usr.c_bpartner_id=bp.c_bpartner_id where em_ehcm_emp_perinfo_id is not null "
       * + " and usr.ad_user_id=?)) " +
       * " and ehcm_emp_perinfo_id=(select em_ehcm_emp_perinfo_id from c_bpartner bp " +
       * " left join ad_user usr on usr.c_bpartner_id=bp.c_bpartner_id where em_ehcm_emp_perinfo_id is not null "
       * + " and usr.ad_user_id=?)) ement on info.ehcm_emp_perinfo_id=ement.ehcm_emp_perinfo_id " +
       * " left join ehcm_position dept on dept.ehcm_position_id=ement.ehcm_position_id " +
       * " left join ad_org org on org.ad_org_id=dept.department_id";
       */

      query = " select distinct org.name as userdepartment from ad_org org "
          + " join ehcm_position dept on dept.department_id=org.ad_org_id "
          + " join ehcm_employment_info empinfo on empinfo.ehcm_position_id = dept.ehcm_position_id "
          + " join c_bpartner bp on empinfo.ehcm_emp_perinfo_id=bp.em_ehcm_emp_perinfo_id "
          + " join ad_user usr on usr.c_bpartner_id=bp.c_bpartner_id "
          + " where usr.ad_user_id=? and empinfo.isactive='Y' and empinfo.status='ACT' ";

      usrDeptQuery = OBDal.getInstance().getSession().createSQLQuery(query);
      usrDeptQuery.setParameter(0, userId);
      // usrDeptQuery.setParameter(1, userId);
      log4j.debug("Query:" + query.toString());
      List queryList = usrDeptQuery.list();

      if (queryList != null && queryList.size() > 0) {
        userDepartment = (String) queryList.get(0);
      }
    } catch (OBException e) {
      log4j.error("Exception while getUserDepartment:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return userDepartment;
  }

  public static String getEmpDepartment(String empId) throws JSONException {
    String query = " as e where e.startDate = (select max(startDate) from Ehcm_Employment_Info einfo where einfo.ehcmEmpPerinfo.id=? ) and e.ehcmEmpPerinfo.id=?";
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(empId);
    parametersList.add(empId);
    String deptId = "";
    try {
      OBContext.setAdminMode();
      OBQuery<EmploymentInfo> dept = OBDal.getInstance().createQuery(EmploymentInfo.class, query,
          parametersList);

      if (dept.list().size() > 0) {
        EmploymentInfo eInfo = dept.list().get(0);
        deptId = eInfo.getPosition() == null ? "" : eInfo.getPosition().getDepartment().getId();
      }
    } catch (OBException e) {
      log4j.error("Exception while getEmpDepartment:" + e);
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return deptId;
  }
}