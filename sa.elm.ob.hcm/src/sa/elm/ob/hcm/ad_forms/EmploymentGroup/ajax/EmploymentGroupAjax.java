package sa.elm.ob.hcm.ad_forms.EmploymentGroup.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;

import sa.elm.ob.hcm.ad_forms.EmploymentGroup.dao.EmploymentGroupDAO;
import sa.elm.ob.hcm.ad_forms.EmploymentGroup.vo.EmploymentGroupVO;
import sa.elm.ob.utility.util.Utility;

/**
 * 
 * @author gopalakrishnan on 09/01/2017
 * 
 */
public class EmploymentGroupAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    EmploymentGroupDAO dao = null;
    try {
      OBContext.setAdminMode();
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new EmploymentGroupDAO(con);
      String action = (request.getParameter("action") == null ? "" : request.getParameter("action"));
      if (action.equals("GetEmploymentGroupList")) {
        log4j.debug("getting EmploymentGroup Ajax");
        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        String gregorianStartDate = "", gregorianEndDate = "";
        int totalPage = 1;
        int offset = 0;
        EmploymentGroupVO vo = new EmploymentGroupVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("Code") != null)
            vo.setCode(request.getParameter("Code").replace("'", "''"));
          if (request.getParameter("Name") != null)
            vo.setName(request.getParameter("Name").replace("'", "''"));
          if (!StringUtils.isEmpty(request.getParameter("StartDate"))) {
            gregorianStartDate = Utility.convertToGregorian(request.getParameter("StartDate"));
            vo.setStartdate(request.getParameter("startdate_s") + "##" + gregorianStartDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
            gregorianEndDate = Utility.convertToGregorian(request.getParameter("enddate"));
            vo.setEnddate(request.getParameter("enddate_s") + "##" + gregorianEndDate);
          }

        }
        int totalRecord = dao.getEmploymentGrpCount(vars.getClient(), searchFlag, vo);
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
        List<EmploymentGroupVO> list = dao.getEmploymentGrpList(vars.getClient(), vo, rows, offset,
            sortColName, sortColType, searchFlag, vars.getLanguage());
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            EmploymentGroupVO VO = (EmploymentGroupVO) list.get(i);
            xmlData.append("<row id='" + VO.getGrpId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getCode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getName() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEnddate() + "]]></cell>");
            xmlData.append("</row>");
          }
          xmlData.append("<page>" + page + "</page><total>" + totalPage + "</total><records>"
              + totalRecord + "</records>");
        } else
          xmlData.append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0
              + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());

      }

    } catch (final Exception e) {
      log4j.error("Error in EmploymentGroupAjax : ", e);
    } finally {
      OBContext.restorePreviousMode();
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in EmploymentGroupAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "EmploymentGroupAjax Servlet";
  }
}