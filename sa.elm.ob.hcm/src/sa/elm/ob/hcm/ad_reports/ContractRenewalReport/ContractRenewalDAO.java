package sa.elm.ob.hcm.ad_reports.ContractRenewalReport;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.exception.OBException;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.Contract;
import sa.elm.ob.hcm.EHCMEmpSupervisor;

//EHCMEmpSupervisor - LineManager
//EHCMEmpSupervisorNode - Employee
public class ContractRenewalDAO {
  public static JSONObject getLineManagerList(String empId) throws JSONException {
    StringBuffer query = null;
    Query deptQuery = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      query = new StringBuffer();
      query.append(
          " select node.ehcmEmpSupervisor.id as linemngid,(node.ehcmEmpSupervisor.employee.searchKey||'-'||"
              + " node.ehcmEmpSupervisor.employee.arabicfullname) as name"
              + " from EHCM_Emp_SupervisorNode node " + " where node.ehcmEmpPerinfo.id=:empId");
      deptQuery = OBDal.getInstance().getSession().createQuery(query.toString());
      deptQuery.setParameter("empId", empId);
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
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  public static JSONObject getContractEmployeesList(String clientId) throws JSONException {
    String query = " as e where e.client.id = ? and e.ehcmEmpPerinfo.id is not null ";
    List<Contract> empLs = null;
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(clientId);
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    List<String> EmployeeList = new ArrayList<String>();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      OBQuery<Contract> empList = OBDal.getInstance().createQuery(Contract.class, query,
          parametersList);
      empLs = empList.list();

      if (empLs.size() > 0) {
        for (Contract empVO : empLs) {
          if ((empVO.getEhcmEmpPerinfo().getStatus().equals("I"))
              && (empVO.getEhcmEmpPerinfo().isEnabled())) {
            if (!EmployeeList.contains(empVO.getEhcmEmpPerinfo().getId())) {
              EmployeeList.add(empVO.getEhcmEmpPerinfo().getId());

              JSONObject jsonData = new JSONObject();
              jsonData.put("id", empVO.getEhcmEmpPerinfo().getId());
              jsonData.put("empName", (empVO.getEhcmEmpPerinfo().getSearchKey() + "-"
                  + empVO.getEhcmEmpPerinfo().getArabicfullname()));
              jsonArray.put(jsonData);
            }
          }
        }
      }

      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      // else
      // jsob.put("data", "");
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  public static JSONObject getDepartmentList(String clientId) throws JSONException {
    PreparedStatement st = null;
    ResultSet rs = null;
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      int count = 0;
      OBContext.setAdminMode();
      st = OBDal.getInstance().getConnection().prepareStatement(
          "select ad_org_id,concat(value,'-' ,name) as name from ad_org where ad_org_id in (select ad_org_id from Ehcm_Hrorg_Classfication where Ehcm_Org_Classfication_ID in (select ehcm_org_classfication_id from ehcm_org_classfication where classification = 'HR' and isactive ='Y') "
              + "and isactive ='Y' and ad_client_id =?)");
      st.setString(1, clientId);
      rs = st.executeQuery();
      while (rs.next()) {
        JSONObject jsonData = new JSONObject();
        jsonData.put("id", rs.getString("ad_org_id"));
        jsonData.put("recordIdentifier", rs.getString("name"));
        count++;
        jsonArray.put(jsonData);
      }
      jsob.put("totalRecords", count);
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

  public static JSONObject getLineManagerlistByNullEmployee(String clientId) throws JSONException {
    String query = " as e where e.client.id = ? ";
    List<EHCMEmpSupervisor> empLs = null;
    List<Object> parametersList = new ArrayList<Object>();
    parametersList.add(clientId);
    JSONObject jsob = null;
    JSONArray jsonArray = new JSONArray();
    try {
      OBContext.setAdminMode();
      jsob = new JSONObject();
      OBQuery<EHCMEmpSupervisor> empList = OBDal.getInstance().createQuery(EHCMEmpSupervisor.class,
          " as e  where e.client.id ='" + OBContext.getOBContext().getCurrentClient().getId()
              + "'  order by e.name asc");
      empLs = empList.list();

      if (empLs.size() > 0) {
        for (EHCMEmpSupervisor empVO : empLs) {
          JSONObject jsonData = new JSONObject();
          jsonData.put("id", empVO.getId());
          jsonData.put("recordIdentifier",
              (empVO.getEmployee().getSearchKey() + "-" + empVO.getEmployee().getArabicfullname()));
          jsonArray.put(jsonData);
        }
      }
      if (jsonArray.length() > 0)
        jsob.put("data", jsonArray);
      else
        jsob.put("data", "");
    } catch (OBException e) {
      throw new OBException(e.getMessage());
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsob;
  }

}
