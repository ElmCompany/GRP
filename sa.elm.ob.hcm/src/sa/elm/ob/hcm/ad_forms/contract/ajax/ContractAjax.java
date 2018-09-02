package sa.elm.ob.hcm.ad_forms.contract.ajax;

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
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.Contract;
import sa.elm.ob.hcm.ContractLine;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ad_forms.contract.dao.ContractDAO;
import sa.elm.ob.hcm.ad_forms.contract.vo.ContractVO;
import sa.elm.ob.hcm.ad_forms.employment.vo.EmploymentVO;
import sa.elm.ob.utility.util.Utility;

/**
 * 
 * @author gopalakrishnan on 08/11/2016
 * 
 */
public class ContractAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    ContractDAO dao = null;
    JSONArray jsonArray = null;
    JSONObject jsonResponse = null;
    String strGradeId = "";
    try {
      OBContext.setAdminMode();
      con = getConnection();
      // commonDAO = new CommonDAO(con);
      dao = new ContractDAO(con);
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      String contractId = (request.getParameter("inpContractId") == null ? ""
          : request.getParameter("inpContractId"));
      String inpStartDate = request.getParameter("inpStartDate");
      String inpEndDate = request.getParameter("inpEndDate");
      if (action.equals("GetSalaryDetails")) {
        log4j.debug("getting salarydetails Ajax");
        contractId = (request.getParameter("inpContractId") == null ? ""
            : request.getParameter("inpContractId"));
        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        ContractVO vo = new ContractVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("element") != null)
            vo.setElement(request.getParameter("element").replace("'", "''"));
          if (request.getParameter("value") != null)
            vo.setValue(request.getParameter("value").replace("'", "''"));
          if (request.getParameter("Percentage") != null)
            vo.setPercentage(request.getParameter("Percentage").replace("'", "''"));

        }
        int totalRecord = dao.getSalaryCount(vars.getClient(), contractId, searchFlag, vo);
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
        List<ContractVO> list = dao.getSalaryList(vars.getClient(), contractId, vo, rows, offset,
            sortColName, sortColType, searchFlag);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            ContractVO VO = (ContractVO) list.get(i);
            xmlData.append("<row id='" + VO.getSalaryId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getElement() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getValue() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getPercentage() + "]]></cell>");
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

      // get Contract Details
      if (action.equals("GetContractList")) {
        log4j.debug("getting contractDetails Ajax");
        String employeeId = request.getParameter("inpEmployeeId");
        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        String gregorianStartDate = "", gregorianEndDate = "", gregorianletterDate = "";
        int totalPage = 1;
        int offset = 0;
        ContractVO vo = new ContractVO();
        if (searchFlag != null && searchFlag.equals("true")) {
          if (request.getParameter("contractType") != null)
            vo.setContractType(request.getParameter("contractType").replace("'", "''"));
          if (request.getParameter("contractNo") != null)
            vo.setContractNo(request.getParameter("contractNo").replace("'", "''"));
          if (request.getParameter("Duration") != null)
            vo.setDuration(request.getParameter("Duration").replace("'", "''"));
          if (request.getParameter("grade") != null)
            vo.setGrade(request.getParameter("grade").replace("'", "''"));
          if (request.getParameter("jobno") != null && !request.getParameter("jobno").equals(""))
            vo.setJobNo(request.getParameter("jobno").replace("'", "''"));
          if (request.getParameter("letterNo") != null)
            vo.setLetterNo(request.getParameter("letterNo").replace("'", "''"));
          if (request.getParameter("decisionNo") != null)
            vo.setDecisionNo(request.getParameter("decisionNo").replace("'", "''"));

          if (!StringUtils.isEmpty(request.getParameter("StartDate"))) {
            gregorianStartDate = Utility.convertToGregorian(request.getParameter("StartDate"));
            vo.setStartDate(request.getParameter("startdate_s") + "##" + gregorianStartDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("expiryDate"))) {
            gregorianEndDate = Utility.convertToGregorian(request.getParameter("expiryDate"));
            vo.setEndDate(request.getParameter("enddate_s") + "##" + gregorianEndDate);
          }

          if (!StringUtils.isEmpty(request.getParameter("letterDate"))) {
            gregorianletterDate = Utility.convertToGregorian(request.getParameter("letterDate"));
            vo.setLetterDate(request.getParameter("letterdate_s") + "##" + gregorianletterDate);
          }
        }
        int totalRecord = dao.getContractCount(vars.getClient(), employeeId, searchFlag, vo);
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
        List<ContractVO> list = dao.getContractList(vars.getClient(), employeeId, vo, rows, offset,
            sortColName, sortColType, searchFlag, vars.getLanguage());
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            ContractVO VO = (ContractVO) list.get(i);
            xmlData.append("<row id='" + VO.getContractId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getContractType() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getContractNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDuration() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEndDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getGrade() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getJobNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getLetterNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getLetterDate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDecisionNo() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getTrxStatus() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStatus() + "]]></cell>");
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

      else if (action.equals("DeleteContract")) {
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<DeleteContract>");
        response.getWriter().write("<Response>"
            + dao.deleteContract(request.getParameter("inpContractId")) + "</Response>");
        response.getWriter().write("</DeleteContract>");
      }
      // SaveSalaryDetails
      else if (action.equals("SaveSalaryDetails")) {
        String operation = request.getParameter("oper");
        String employeeId = request.getParameter("inpEmployeeId");
        String contractValue = request.getParameter("value");
        String payRoll = request.getParameter("element");
        String percentage = request.getParameter("Percentage");
        String strSalaryId = request.getParameter("id");
        if (operation.equals("add")) {
          log4j.debug("contractId:" + contractId);
          ContractLine objLine = OBProvider.getInstance().get(ContractLine.class);
          objLine.setEhcmContract(OBDal.getInstance().get(Contract.class, contractId));
          objLine.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId));
          objLine.setPayroll(payRoll);
          objLine.setContractvalue(Long.valueOf(contractValue));
          objLine.setPercentage(percentage);
          OBDal.getInstance().save(objLine);
          OBDal.getInstance().flush();
        } else if (operation.equals("edit")) {
          ContractLine objLine = OBDal.getInstance().get(ContractLine.class, strSalaryId);
          objLine.setEhcmContract(OBDal.getInstance().get(Contract.class, contractId));
          objLine.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId));
          objLine.setPayroll(payRoll);
          objLine.setContractvalue(Long.valueOf(contractValue));
          objLine.setPercentage(percentage);
          OBDal.getInstance().save(objLine);
          OBDal.getInstance().flush();
        } else if (operation.equals("del")) {
          OBDal.getInstance().remove(OBDal.getInstance().get(ContractLine.class, strSalaryId));
        }
      }
      // Contract Employee
      else if (action.equals("SearchEmployee")) {
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
          log4j.error("Exception in ContractAjax - SearchEmployee : ", e);
        } finally {
          response.setContentType("application/json");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(array.toString());
        }
      }
      // get grade
      /*
       * else if (action.equals("getGrade")) { List<ehcmgrade> gradeList = null; gradeList =
       * dao.getGrade(vars.getClient()); jsonArray = new JSONArray(); if (gradeList != null &&
       * gradeList.size() > 0) { for (ehcmgrade grade : gradeList) { jsonResponse = new
       * JSONObject(); jsonResponse.put("gradeId", grade.getId()); jsonResponse.put("gradeCode",
       * grade.getSearchKey()); jsonArray.put(jsonResponse); } }
       * response.setCharacterEncoding("UTF-8"); response.setContentType("application/json");
       * response.setHeader("Cache-Control", "no-cache");
       * response.getWriter().write(jsonArray.toString());
       * 
       * }
       */
      else if (action.equals("getJobNo")) {
        JSONObject jsob = new JSONObject();
        if (request.getParameter("gradeId") != null && request.getParameter("pageLimit") != null
            && request.getParameter("page") != null && request.getParameter("searchTerm") != null)
          jsob = ContractDAO.getJobList(vars.getClient(), request.getParameter("gradeId"),
              request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      }
      // get Position
      /*
       * else if (action.equals("getJobNo")) { List<EhcmPosition> positionList = null; strGradeId =
       * request.getParameter("gradeId"); positionList = dao.getPositionList(strGradeId); jsonArray
       * = new JSONArray(); if (positionList != null && positionList.size() > 0) { for (EhcmPosition
       * jobNo : positionList) { jsonResponse = new JSONObject(); jsonResponse.put("jobId",
       * jobNo.getId()); jsonResponse.put("jobNo", jobNo.getJOBNo()); jsonArray.put(jsonResponse); }
       * } response.setCharacterEncoding("UTF-8"); response.getWriter().write(jsonArray.toString());
       * }
       */
      else if (action.equals("getGrade")) {
        JSONObject jsob = new JSONObject();
        if (request.getParameter("searchTerm") != null && request.getParameter("pageLimit") != null
            && request.getParameter("page") != null)
          jsob = ContractDAO.getGradeList(vars.getClient(), request.getParameter("searchTerm"),
              Integer.parseInt(request.getParameter("pageLimit")),
              Integer.parseInt(request.getParameter("page")));
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write(jsob.toString());
      } else if (action.equals("checkPeriod")) {
        String fromDate = "";
        String endDate = "";
        String inpEmployeeId = request.getParameter("inpEmployeeId");
        if (inpStartDate != null && !inpStartDate.equals("")) {
          fromDate = Utility.formatDate(dao.convertGregorian(inpStartDate));
        }
        if (inpEndDate != null && !inpEndDate.equals("")) {
          endDate = Utility.formatDate(dao.convertGregorian(inpEndDate));
        }
        String isExist = dao.checkPeriod(inpEmployeeId, fromDate, endDate, contractId);
        jsonResponse = new JSONObject();
        jsonResponse.put("isExists", isExist);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
      } else if (action.equals("Checkcontractvalidation")) {
        String duration = request.getParameter("inpDuration");
        String empid = request.getParameter("inpEmployeeId");
        String inpContractId = request.getParameter("inpContractId");

        ContractVO vo = dao.checkcontractval(vars.getClient(), empid, inpContractId);
        jsonResponse = new JSONObject();
        jsonResponse.put("isExists", vo.getValue());
        jsonResponse.put("minservice", vo.getMinservice());
        jsonResponse.put("maxservice", vo.getMaxservice());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());

      }

    } catch (final Exception e) {
      log4j.error("Error in ContractAjax : ", e);
    } finally {
      OBContext.restorePreviousMode();
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in ContractAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "ContractAjax Servlet";
  }
}