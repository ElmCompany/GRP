package sa.elm.ob.hcm.ad_forms.employment.ajax;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;

import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmgrade;
import sa.elm.ob.hcm.ad_forms.employment.dao.EmploymentDAO;
import sa.elm.ob.hcm.ad_forms.employment.vo.EmploymentVO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.scm.EscmLocation;
import sa.elm.ob.utility.util.Utility;

public class EmploymentAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String lang = vars.getLanguage();
    Connection con = null;
    EmploymentDAO dao = null;
    JSONArray jsonArray = null;
    JSONObject jsonResponse = null;
    String strGradeId = "", strPositionId = "", strPayScaleId = "", strEmpGradeId;
    DateFormat dateYearFormat = Utility.YearFormat;
    try {
      OBContext.setAdminMode();
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new EmploymentDAO(con);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      strGradeId = request.getParameter("gradeId");
      strPositionId = request.getParameter("positionId");
      strPayScaleId = request.getParameter("payScaleId");
      strEmpGradeId = request.getParameter("empGrade");
      log4j.debug(strGradeId);
      log4j.debug("action" + action);
      AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();

      if (action.equals("DeleteEmployment")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteEmployment>");
        response.getWriter().write("<Response>"
            + dao.deleteEmployment(request.getParameter("inpEmploymentId")) + "</Response>");
        response.getWriter().write("</DeleteEmployment>");

      } else if (action.equals("getGrade")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getGradeList(vars.getClient(), request.getParameter("searchTerm"),
            Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getJobNo")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getPositionList(strGradeId, request.getParameter("inpEmployeeId"),
            request.getParameter("inpStartDate"), vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg(),
            request.getParameter("inpEmploymentId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("checkActiveEmployment")) {
        String isExist = "N";
        String inpEmployeeId = request.getParameter("inpEmployeeId");
        String inpEmploymentId = request.getParameter("inpEmploymentId") == null ? ""
            : request.getParameter("inpEmploymentId");
        OBQuery<EmploymentInfo> objEmployeeQuery = OBDal.getInstance()
            .createQuery(EmploymentInfo.class, "as e where e.ehcmEmpPerinfo.id='" + inpEmployeeId
                + "' and e.enabled='Y' and e.id not in ('" + inpEmploymentId + "')");
        if (objEmployeeQuery.list().size() > 0) {
          isExist = "Y";
        }
        jsonResponse = new JSONObject();
        jsonResponse.put("isExists", isExist);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

      } else if (action.equals("getEmploymentGrade")) {

        Long seqNo = (long) 0;
        if (strGradeId != null && !strGradeId.equals("") && !strGradeId.equals("null")) {
          ehcmgrade objGrade = OBDal.getInstance().get(ehcmgrade.class, strGradeId);
          seqNo = objGrade.getSequenceNumber();
        }
        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getEmpGrade(strGradeId, seqNo, vars.getClient(),
            request.getParameter("searchTerm"), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getJobDetails")) {
        List<EhcmPosition> jobList = null;
        jobList = dao.getJobDetailsList(strPositionId, strGradeId);
        if (jobList != null && jobList.size() > 0) {
          for (EhcmPosition jobDetail : jobList) {
            jsonResponse = new JSONObject();
            if (jobDetail.getEhcmJobs() != null) {
              // jsonResponse.put("jobCode", jobDetail.getEhcmJobs().getJobCode());
              jsonResponse.put("jobCode", jobDetail.getEhcmJobs().getId());
              jsonResponse.put("jobname", jobDetail.getEhcmJobs().getJobCode());
            } else {
              jsonResponse.put("jobCode", "");
            }
            if (jobDetail.getEhcmJobs() != null) {
              jsonResponse.put("jobTitle", jobDetail.getEhcmJobs().getJOBTitle());
            } else {
              jsonResponse.put("jobTitle", "");
            }
            if (jobDetail.getDepartment() != null) {
              // jsonResponse.put("DeptCode", jobDetail.getDepartment().getSearchKey());
              jsonResponse.put("DeptCode", jobDetail.getDepartment().getId());
              jsonResponse.put("deptName", jobDetail.getDepartment().getSearchKey());
            } else {
              jsonResponse.put("DeptCode", "");
            }
            if (jobDetail.getDeptname() != null) {
              jsonResponse.put("DeptName", jobDetail.getDeptname());
            } else {
              jsonResponse.put("DeptName", "");
            }
            if (jobDetail.getSection() != null) {
              // jsonResponse.put("secCode", jobDetail.getSection().getSearchKey());
              jsonResponse.put("secCode", jobDetail.getSection().getId());
              jsonResponse.put("secname", jobDetail.getSection().getSearchKey());
            } else {
              jsonResponse.put("secCode", "");
            }
            if (jobDetail.getSectionname() != null) {
              jsonResponse.put("secName", jobDetail.getSectionname());
            } else {
              jsonResponse.put("secName", "");
            }
            if (jobDetail.getDepartment() != null
                && jobDetail.getDepartment().getEhcmEscmLoc() != null) {
              EscmLocation loc = OBDal.getInstance().get(EscmLocation.class,
                  jobDetail.getDepartment().getEhcmEscmLoc().getId());
              if (loc.getLocationName() != null) {
                jsonResponse.put("location", loc.getLocationName());
              } else if (loc.getLocationName() == null) {
                jsonResponse.put("location", "");
              }
            } else {
              jsonResponse.put("location", "");
            }

          }
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

      } else if (action.equals("getEmploymentCategory")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getEmploymentCatList(vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getPayscale")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getPayscaleList(strEmpGradeId, vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getGradeStep")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getGradeStepList(strPayScaleId, vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("SearchEmployee")) {
        JSONArray array = new JSONArray();
        JSONObject json = null;
        try {
          JSONObject searchAttr = new JSONObject();
          String col = request.getParameter("col").toString();
          searchAttr.put("sortName", request.getParameter("col"));
          if (col.equals("aname") && !StringUtils.isEmpty(request.getParameter("term")))
            searchAttr.put("aname", request.getParameter("term").replace("'", "''"));
          if (col.equals("fname") && !StringUtils.isEmpty(request.getParameter("term")))
            searchAttr.put("fname", request.getParameter("term").replace("'", "''"));
          if (col.equals("empno") && !StringUtils.isEmpty(request.getParameter("term")))
            searchAttr.put("empno", request.getParameter("term").replace("'", "''"));

          List<EmploymentVO> list = dao.getSearchEmployee(vars.getClient(), searchAttr);
          for (EmploymentVO vo : list) {
            json = new JSONObject();
            json.put("id", vo.getEmployeeId());
            json.put("fname", vo.getFullName());
            json.put("aname", vo.getArabicName());
            json.put("empno", vo.getEmploymentNo());
            array.put(json);
          }
        } catch (final Exception e) {
          log4j.error("Exception in EmploymentAjax - SearchEmployee : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(array.toString());
        }
      } else if (action.equals("GetEmploymentList")) {
        log4j.debug("getting employmentList Ajax");
        String employeeId = request.getParameter("inpEmployeeId");
        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        String hijiriDate = "";
        EmploymentVO vo = new EmploymentVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("grade") != null)
            vo.setGrade(request.getParameter("grade").replace("'", "''"));
          if (request.getParameter("jobno") != null)
            vo.setJobNo(request.getParameter("jobno").replace("'", "''"));
          if (request.getParameter("jobcode") != null)
            vo.setJobCode(request.getParameter("jobcode").replace("'", "''"));
          if (request.getParameter("DepartmentCode") != null)
            vo.setDeptCode(request.getParameter("DepartmentCode").replace("'", "''"));
          if (request.getParameter("sectioncode") != null
              && !request.getParameter("sectioncode").equals(""))
            vo.setSectionCode(request.getParameter("sectioncode").replace("'", "''"));
          if (request.getParameter("Payscale") != null)
            vo.setPayscale(request.getParameter("Payscale").replace("'", "''"));
          if (request.getParameter("EmploymentGrade") != null)
            vo.setEmpGrade(request.getParameter("EmploymentGrade").replace("'", "''"));
          if (request.getParameter("gradeStep") != null) {
            vo.setGradeStep(request.getParameter("gradeStep").replace("'", "''"));
          }
          if (request.getParameter("Status") != null) {
            vo.setStatus(request.getParameter("Status").replace("'", "''"));
          }
          if (request.getParameter("ChangeReason") != null) {
            vo.setChangeReason(request.getParameter("ChangeReason").replace("'", "''"));
          }
          if (!StringUtils.isEmpty(request.getParameter("startdate"))) {
            hijiriDate = Utility.convertToGregorian(request.getParameter("startdate"));
            vo.setStartDate(request.getParameter("startdate_s") + "##" + hijiriDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("enddate"))) {
            hijiriDate = Utility.convertToGregorian(request.getParameter("enddate"));
            vo.setEndDate(request.getParameter("enddate_s") + "##" + hijiriDate);
          }
        }
        int totalRecord = dao.getEmploymentCount(vars.getClient(), employeeId, searchFlag, vo);
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
        List<EmploymentVO> list = dao.getEmployMentList(vars.getClient(), employeeId, vo, rows,
            offset, sortColName, sortColType, searchFlag, lang);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            EmploymentVO VO = (EmploymentVO) list.get(i);
            xmlData.append("<row id='" + VO.getEmploymnetId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getGrade() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getJobNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getJobCode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDeptCode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getSectionCode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEmpGrade() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getPayscale() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getGradeStep() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEndDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStatus() + "]]></cell>");
            if (VO.getChangereasoninfo() != null && !VO.getChangereasoninfo().equals("")) {
              if (VO.getChangereasonemp().equals("T"))
                xmlData.append("<cell><![CDATA[" + VO.getChangeReason() + "-"
                    + dao.getTermiationReason(vars.getClient(), VO.getChangereasoninfo())
                    + "]]></cell>");
              else if (VO.getChangereasonemp().equals("SUS"))
                xmlData.append("<cell><![CDATA[" + VO.getChangeReason() + "-"
                    + dao.getSuspensionReason(VO.getChangereasoninfo()) + "]]></cell>");
              else if (VO.getChangereasonemp().equals("SUE"))
                xmlData.append("<cell><![CDATA[" + VO.getChangeReason() + "-"
                    + dao.getSuspensionReason(VO.getChangereasoninfo()) + "]]></cell>");
            } else {
              xmlData.append("<cell><![CDATA[" + VO.getChangeReason() + "]]></cell>");
            }
            xmlData.append("</row>");
          }
          xmlData.append("<page>" + page + "</page><total>" + totalPage + "</total><records>"
              + totalRecord + "</records>");
        } else
          xmlData
              .append("<page>" + 0 + "</page><total>" + 0 + "</total><records>" + 0 + "</records>");
        xmlData.append("</rows>");
        response.getWriter().write(xmlData.toString());
      }

      else if (action.equals("getSupervisor")) {
        JSONObject jsob = new JSONObject();
        jsob = dao.getSupervisorList(vars.getClient(), request.getParameter("searchTerm"),
            Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg(),
            request.getParameter("inpEmployeeId"));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("getPayroll")) {

        JSONObject jsob = new JSONObject();
        jsob = EmploymentDAO.getPayrollDefinition(vars.getClient(),
            request.getParameter("searchTerm"), Integer.parseInt(request.getParameter("pageLimit")),
            Integer.parseInt(request.getParameter("page")), vars.getRole(), vars.getOrg());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());

      } else if (action.equals("Submit")) {
        Boolean terminateEmployee = false;
        String employmentId = request.getParameter("inpEmploymentId");
        String decisionNo = request.getParameter("decisionNo");
        if (!decisionNo.equals("") && !request.getParameter("decisionDate").equals("")) {
          Date decisionDate = dateYearFormat.parse(sa.elm.ob.hcm.util.UtilityDAO
              .convertToGregorian_tochar(request.getParameter("decisionDate")));
          terminateEmployee = dao.terminateEmployeeByDecisionNumber(employmentId, decisionNo,
              decisionDate);

        }
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(terminateEmployee == true ? "success" : "failed");
      } else if (action.equals("CheckPosAvailableOrNot")) {
        StringBuffer sb = new StringBuffer();
        OBQuery<EmploymentInfo> info = OBDal.getInstance().createQuery(EmploymentInfo.class,
            " as e where e.ehcmEmpPerinfo.id=:employeeId ");
        info.setNamedParameter("employeeId", request.getParameter("inpEmployeeId"));
        info.setMaxResult(1);
        if (info.list().size() > 0) {
          EmploymentInfo employInfo = info.list().get(0);
          EhcmPosition position = employInfo.getPosition();
          Boolean chkPositionAvailableOrNot = assingedOrReleaseEmpInPositionDAO
              .chkPositionAvailableOrNot(employInfo.getEhcmEmpPerinfo(), position,
                  employInfo.getStartDate(), null, "CR", false);
          sb.append("<CheckPosAvailableOrNot>");
          sb.append("<result>" + chkPositionAvailableOrNot + "</result>");
          sb.append("</CheckPosAvailableOrNot>");
        }
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(sb.toString());
      }

    } catch (

    final Exception e) {
      e.printStackTrace();
      log4j.error("Error in EmploymentAjax : ", e);
    } finally {
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in EmploymentAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "EmploymentAjax Servlet";
  }
}