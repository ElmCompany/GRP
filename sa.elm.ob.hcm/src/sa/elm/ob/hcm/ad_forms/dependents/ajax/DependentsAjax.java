package sa.elm.ob.hcm.ad_forms.dependents.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.dependents.dao.DependentDAO;
import sa.elm.ob.hcm.ad_forms.dependents.vo.DependentVO;
import sa.elm.ob.utility.util.Utility;

public class DependentsAjax extends HttpSecureAppServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    DependentDAO dao = null;
    JSONArray jsonArray = null;
    JSONObject jsonresponse = null;
    try {
      con = getConnection();
      dao = new DependentDAO(con);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      if (action.equals("getDependentList")) {
        log4j.debug("getting getDependentList Ajax");
        String employeeId = request.getParameter("inpEmployeeId");
        log4j.debug("dependentAjax:" + employeeId);
        String gregorianStartDate = "", gregorianEndDate = "";

        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        DependentVO vo = new DependentVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("name") != null)
            vo.setName(request.getParameter("name").replace("'", "''"));
          if (request.getParameter("rel") != null)
            vo.setRelationship(request.getParameter("rel").replace("'", "''"));
          if (request.getParameter("age") != null)
            vo.setAge(request.getParameter("age").replace("'", "''"));
          if (request.getParameter("gender") != null)
            vo.setGender(request.getParameter("gender").replace("'", "''"));
          if (request.getParameter("nationdalId") != null
              && !request.getParameter("nationdalId").equals(""))
            vo.setNatidf(request.getParameter("nationdalId").replace("'", "''"));
          if (request.getParameter("phoneno") != null)
            vo.setPhoneno(request.getParameter("phoneno").replace("'", "''"));
          if (request.getParameter("location") != null)
            vo.setLocation(request.getParameter("location").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("startdate"))) {
            gregorianStartDate = Utility.convertToGregorian(request.getParameter("startdate"));
            vo.setStartdate(request.getParameter("startdate_s") + "##" + gregorianStartDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
            gregorianEndDate = Utility.convertToGregorian(request.getParameter("enddate"));
            vo.setEnddate(request.getParameter("enddate_s") + "##" + gregorianEndDate);
          }
        }
        int totalRecord = dao.getDependentCount(vars.getClient(), employeeId, searchFlag, vo);
        log4j.debug("totalRecord:" + totalRecord);
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
        }
        List<DependentVO> list = dao.getDependentList(vars.getClient(), employeeId, vo, rows,
            offset, sortColName, sortColType, searchFlag, vars.getLanguage());
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            DependentVO VO = (DependentVO) list.get(i);
            xmlData.append("<row id='" + VO.getId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getName() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getRelationship() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getAge() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getGender() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + Utility.nullToEmpty(VO.getEnddate()) + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getNatidf() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getPhoneno() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getLocation() + "]]></cell>");
            xmlData.append("</row>");
          }
          xmlData.append("<page>" + page + "</page><total>" + totalPage + "</total><records>"
              + totalRecord + "</records>");
        } else
          xmlData
              .append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0 + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());
      } else if (action.equals("getEmployeeNames")) {
        EhcmEmpPerInfo empnames = null;
        JSONObject jsonResponse = new JSONObject();

        String employeeId = request.getParameter("inpEmployeeId");
        log4j.debug("employeeId: " + employeeId);
        try {
          OBContext.setAdminMode(true);
          empnames = dao.getEmployeeNames(employeeId);
          /*
           * jsonResponse.put("Arabicname", empNamels.getArabicname());
           * jsonResponse.put("Arabicfatname", empNamels.getArabicfatname());
           * jsonResponse.put("Arbgrafaname", empNamels.getArbgrafaname());
           * jsonResponse.put("Arbfouname", empNamels.getArbfouname());
           * jsonResponse.put("Arabicfamilyname", empNamels.getArabicfamilyname());
           */
          log4j.debug("firstname: " + empnames.getName());
          jsonResponse.put("firstname", empnames.getName());
          jsonResponse.put("fathername", empnames.getFathername());
          jsonResponse.put("grandfather", empnames.getGrandfathername());
          jsonResponse.put("fourthname", empnames.getFourthname());
          jsonResponse.put("family", empnames.getFamilyname());
          jsonResponse.put("marialst", empnames.getMarialstatus());
          jsonResponse.put("gender", empnames.getGender());
        } catch (JSONException je) {
          OBContext.restorePreviousMode();
          je.printStackTrace();
        } finally {
          OBContext.restorePreviousMode();
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
      } else if (action.equals("DeleteDependent")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteDependent>");
        response.getWriter().write("<Response>"
            + dao.deleteDependent(request.getParameter("inpDependentId")) + "</Response>");
        response.getWriter().write("</DeleteDependent>");
      } else if (action.equals("getRelationShip")) {
        List<DependentVO> relationShipList = null;
        String employeeId = request.getParameter("inpEmployeeId");
        relationShipList = dao.getRelationShipList(employeeId);
        jsonArray = new JSONArray();
        if (relationShipList != null && relationShipList.size() > 0) {
          for (DependentVO vo : relationShipList) {
            jsonresponse = new JSONObject();
            jsonresponse.put("relValue", vo.getRelationValue());
            jsonresponse.put("rel", vo.getRelationship());
            jsonArray.put(jsonresponse);
          }
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
      } else if (action.equals("getEmpGender")) {
        EhcmEmpPerInfo empdetails = null;
        JSONObject jsonResponse = new JSONObject();

        String employeeId = request.getParameter("inpEmployeeId");
        log4j.debug("employeeId: " + employeeId);
        try {
          OBContext.setAdminMode(true);
          empdetails = dao.getEmployeeNames(employeeId);
          log4j.debug("firstname: " + empdetails.getName());
          jsonResponse.put("marialst", empdetails.getMarialstatus());
          jsonResponse.put("empgender", empdetails.getGender());

        } catch (JSONException je) {
          OBContext.restorePreviousMode();
          je.printStackTrace();
        } finally {
          OBContext.restorePreviousMode();
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
      }

      else if (action.equals("CheckNationalNum")) {
        log4j.debug("entering: " + action);
        StringBuffer sb = new StringBuffer();
        DependentVO vo = null;
        try {
          vo = dao.checkNationalID(vars.getClient(), request.getParameter("inpNationalId"),
              request.getParameter("inpDependentId"));
          sb.append("<ChkNatID>");
          sb.append("<Valid>" + vo.isValue() + "</Valid>");
          sb.append("<Exist>" + vo.isResult() + "</Exist>");
          log4j.debug("vo.isResult():" + vo.isResult());
          sb.append("</ChkNatID>");
        } catch (final Exception e) {
          log4j.error("Exception in dependent - checking national identifier number: ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      }
    } catch (final Exception e) {
      log4j.error("Error in DependentAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in DependentAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "DependentAjax Servlet";
  }

}
