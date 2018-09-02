package sa.elm.ob.hcm.ad_forms.preemp.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;

import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_forms.preemp.dao.PreEmploymentDAO;
import sa.elm.ob.utility.util.Utility;

public class PreEmploymentAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    PreEmploymentDAO dao = null;
    JSONArray jsonArray = null;
    JSONObject jsonResponse = null;
    String strGradeId = "", strPositionId = "";
    try {
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new PreEmploymentDAO(con);
      String action = (request.getParameter("action") == null ? "" : request.getParameter("action"));

      if (action.equals("DeletePrevEmployment")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeletePrevEmployment>");
        response.getWriter().write(
            "<Response>" + dao.deletePrevEmployment(request.getParameter("inpPreEmplymentId"))
                + "</Response>");
        response.getWriter().write("</DeletePrevEmployment>");
      }

      else if (action.equals("GetPreEmploymentList")) {
        log4j.debug("getting preemploymentList Ajax");
        String preemploymentId = request.getParameter("inpPreEmplymentId");
        String employeeId = request.getParameter("inpEmployeeId");
        log4j.debug("employeeId:" + employeeId);
        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        EmployeeVO vo = new EmployeeVO();
        String startdate = "", enddate = "";
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("employer_name") != null)
            vo.setEmpName(request.getParameter("employer_name").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("startdate"))) {
            startdate = Utility.convertToGregorian(request.getParameter("startdate"));
            vo.setStartdate(request.getParameter("startdate_s") + "##" + startdate);
          }
          if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
            enddate = Utility.convertToGregorian(request.getParameter("enddate"));
            vo.setEnddate(request.getParameter("enddate_s") + "##" + enddate);
          }
          if (request.getParameter("empposition") != null)
            vo.setPosition(request.getParameter("empposition").replace("'", "''"));
          if (request.getParameter("department") != null
              && !request.getParameter("department").equals(""))
            vo.setDeptname(request.getParameter("department").replace("'", "''"));
          if (request.getParameter("emp_cat") != null)
            vo.setCategorycode(request.getParameter("emp_cat").replace("'", "''"));
        }
        JSONObject searchAttr = new JSONObject();
        searchAttr.put("rows", request.getParameter("rows").toString());
        searchAttr.put("page", request.getParameter("page").toString());
        searchAttr.put("search", searchFlag);
        searchAttr.put("sortName", request.getParameter("sidx").toString());
        searchAttr.put("sortType", request.getParameter("sord").toString());
        List<EmployeeVO> list = dao.getPrevEmployeeList(vars.getClient(), request.getSession()
            .getAttribute("Employee_ChildOrg").toString(), vo, searchAttr, preemploymentId,
            employeeId);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        if (list.size() > 0) {
          String[] pageDetails = list.get(0).getStatus().split("_");
          xmlData.append("<page>" + pageDetails[0] + "</page><total>" + pageDetails[1]
              + "</total><records>" + pageDetails[2] + "</records>");
          for (int i = 1; i < list.size(); i++) {
            EmployeeVO VO = (EmployeeVO) list.get(i);
            xmlData.append("<row id='" + VO.getPreEmpId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getEmployeeId() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEmpName() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEnddate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getPosition() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDeptname() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getCategorycode() + "]]></cell>");
            xmlData.append("</row>");
          }
        } else
          xmlData.append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0
              + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());
      } else if (action.equals("ChkOverlapRecords")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<OverLapRecords>");
        response.getWriter().write(
            "<Result>"
                + dao.overlapRecord(request.getParameter("inpEmployeeId"),
                    request.getParameter("inpstartdate"), request.getParameter("inpenddate"),
                    request.getParameter("inpPreEmplymentId")) + "</Result>");
        response.getWriter().write("</OverLapRecords>");
      }
    } catch (final Exception e) {
      log4j.error("Error in EmploymentAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in EmploymentAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "EmploymentAjax Servlet";
  }
}
