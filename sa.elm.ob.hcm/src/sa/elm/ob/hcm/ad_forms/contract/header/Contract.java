package sa.elm.ob.hcm.ad_forms.contract.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.contract.dao.ContractDAO;
import sa.elm.ob.hcm.ad_forms.contract.vo.ContractVO;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 08/11/2016
 */

public class Contract extends HttpSecureAppServlet {

  /**
   * Contract form details
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    ContractDAO dao = null;
    Connection con = null;
    // EmploymentVO employmentVO = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    ContractVO vo = null;
    EmployeeDAO employeeDao = null;
    try {

    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Contract : ", e);
    } finally {
      try {
        OBContext.setAdminMode();
        String action = (request.getParameter("inpAction") == null ? ""
            : request.getParameter("inpAction"));
        String submitType = request.getParameter("SubmitType") == null ? ""
            : request.getParameter("SubmitType");
        String employeeId = request.getParameter("inpEmployeeId") == null ? ""
            : request.getParameter("inpEmployeeId");
        String contractId = request.getParameter("inpContractId") == null ? ""
            : request.getParameter("inpContractId");
        String nextTab = (request.getParameter("inpNextTab") == null ? ""
            : request.getParameter("inpNextTab"));
        String employeeaddId = (request.getParameter("inpAddressId") == null ? ""
            : request.getParameter("inpAddressId"));
        String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
            : request.getParameter("inpEmpStatus"));
        log4j.debug("inpempstatus" + inpempstatus);
        String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
            : request.getParameter("inpEmployeeStatus"));
        log4j.debug("inpEmployeeStatus" + inpEmployeeStatus);

        con = getConnection();
        vars = new VariablesSecureApp(request);
        dao = new ContractDAO(con);
        employeeDao = new EmployeeDAO(con);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");

        // process save operation
        if (submitType != null && (submitType.equals("Save") || submitType.equals("SaveNew")
            || submitType.equals("IssueDecision"))) {
          vo = new ContractVO();
          vo.setContractType(request.getParameter("inpContractType"));
          vo.setTrxStatus(request.getParameter("inpTrxStatus"));
          vo.setContractNo(request.getParameter("inpContractNo"));
          vo.setStartDate(request.getParameter("inpStartDate"));
          vo.setDuration(request.getParameter("inpDuration"));
          vo.setDurationType(request.getParameter("inpDurationType"));
          vo.setEndDate(request.getParameter("inpEndDate"));
          vo.setJobDescription(request.getParameter("inpJobDescription"));
          vo.setContractDescription(request.getParameter("inpContractDesc"));
          vo.setGrade(request.getParameter("inpGrade"));
          vo.setJobNo(request.getParameter("inpJobNo"));
          vo.setLetterNo(request.getParameter("inpletterNo"));
          vo.setLetterDate(request.getParameter("inpLettrDate"));
          vo.setDecisionNo(request.getParameter("inpDecisionNo"));
          vo.setAnnualBalance(request.getParameter("inpAnnualBalance"));
          vo.setEmployeeId(employeeId);
          if (contractId.equals(""))
            contractId = dao.addContract(vars.getClient(), vars.getUser(), vo);
          else
            contractId = dao.updateContract(vars.getClient(), vars.getUser(), vo, contractId);

          if (nextTab.equals("") || nextTab.equals("EMPCTRCT")) {

            if (contractId == null) {
              request.setAttribute("SaveStatus", "False");
              request.setAttribute("ErrorMsg", "Process Failed");
            } else {
              if (request.getParameter("inpContractId").equals(""))
                request.setAttribute("SaveStatus", "Add-True");
              else
                request.setAttribute("SaveStatus", "Update-True");
              if (request.getParameter("SubmitType").equals("SaveNew"))
                contractId = "";
            }
            log4j.debug("contractId:" + contractId);
            if (action.equals("GridView"))
              action = "GridView";
            else
              action = "EditView";
          } else {
            ServletContext context = this.getServletContext();
            if (!nextTab.equals("") && !nextTab.equals("EMPCTRCT")) {
              String redirectStr = dao.redirectStr(nextTab, employeeId, inpempstatus,
                  inpEmployeeStatus);
              response.sendRedirect(context.getContextPath() + redirectStr);
            }
          }

        }
        if (submitType.equals("CancelContract")) {
          sa.elm.ob.hcm.Contract objContract = OBDal.getInstance().get(sa.elm.ob.hcm.Contract.class,
              contractId);
          objContract.setTrxstatus("UP");
          OBDal.getInstance().save(objContract);
          OBDal.getInstance().flush();
          request.setAttribute("SaveStatus", "Update-True");
          action = "GridView";
        }

        if (action.equals("") || action.equals("GridView")) {
          log4j.debug("employeeId:" + employeeId);
          ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              view.getEhcmEmpPerinfo().getId());
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          if (request.getParameter("inpEmployeeStatus") != null) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpIssuance", objEmployee.getStatus());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ")
                  .concat(StringUtils.isNotEmpty(objEmployee.getFathername())
                      ? objEmployee.getFathername() : "")
                  .concat(" ").concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                      ? objEmployee.getGrandfathername() : ""));
          request.setAttribute("inpAddressId", employeeaddId);
          request.setAttribute("CancelHiring",
              employeeDao.checkEmploymentStatusCancel(vars.getClient(), employeeId));

          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/contract/ContractList.jsp");
        }
        if (action.equals("EditView")) {
          log4j.debug("employeid:" + employeeId);
          log4j.debug("Edit view contractId:" + contractId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          if (contractId == null || contractId.equals("") || contractId.equals("null")) {
            String date = df.format(new Date());
            date = dateYearFormat.format(df.parse(date));
            date = UtilityDAO.convertTohijriDate(date);
            if (objEmployee.getGradeClass() != null) {
              if (objEmployee.getGradeClass().isContract()) {
                request.setAttribute("inpempCategory", "Y");
              } else {
                request.setAttribute("inpempCategory", "");
              }
            } else {
              request.setAttribute("inpempCategory", "");
            }
            request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
            request.setAttribute("inpName1", objEmployee.getArabicfullname());
            request.setAttribute("inpName2",
                objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                    .concat(objEmployee.getGrandfathername()));
            request.setAttribute("inpStartDate", date);
            request.setAttribute("inptodayDate", date);
            request.setAttribute("inpEndDate", "");
            request.setAttribute("inpContractType", "");
            request.setAttribute("inpTrxStatus", "UP");
            request.setAttribute("inpContractNo", "");
            request.setAttribute("inpDuration", "");
            request.setAttribute("inpDurationType", "");
            request.setAttribute("inpJobDescription", "");
            request.setAttribute("inpContractDesc", "");
            request.setAttribute("inpGrade", "");
            request.setAttribute("inpJobNo", "");
            request.setAttribute("inpletterNo", "");
            request.setAttribute("inpLettrDate", date);
            request.setAttribute("inpDecisionNo", "");
            request.setAttribute("inpDecisionDate", "");
            request.setAttribute("inpAnnualBalance", "0");
            request.setAttribute("inpEmployeeId", employeeId);
            request.setAttribute("inpContractId", "");
            request.setAttribute("inpAddressId", employeeaddId);

            if (objEmployee.getHiredate() != null) {
              date = df.format(objEmployee.getHiredate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpHireDate", date);
            }
          }
          if (contractId != null && !contractId.equals("") && !contractId.equals("null")) {
            sa.elm.ob.hcm.Contract objContract = OBDal.getInstance()
                .get(sa.elm.ob.hcm.Contract.class, contractId);
            if (objEmployee.getGradeClass() != null) {
              if (objEmployee.getGradeClass().isContract()) {
                request.setAttribute("inpempCategory", "Y");
              } else {
                request.setAttribute("inpempCategory", "");
              }
            } else {
              request.setAttribute("inpempCategory", "");
            }
            request.setAttribute("inpContractType", objContract.getContracttype());
            request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
            request.setAttribute("inpName1", objEmployee.getArabicfullname());
            request.setAttribute("inpName2",
                objEmployee.getName().concat(" ").concat(objEmployee.getFathername()).concat(" ")
                    .concat(objEmployee.getGrandfathername()));
            request.setAttribute("inpTrxStatus", objContract.getTrxstatus());
            request.setAttribute("inpContractNo", objContract.getContractNo());
            if (objContract.getPosition() != null) {
              request.setAttribute("inpJobNo", objContract.getPosition().getId());
              request.setAttribute("inpJobName", objContract.getPosition().getJOBNo());
            }
            request.setAttribute("inpDuration", objContract.getDuration());
            request.setAttribute("inpDurationType", objContract.getDurationType());
            request.setAttribute("inpJobDescription", objContract.getJobdescription());
            request.setAttribute("inpContractDesc", objContract.getContractdesc());
            if (objContract.getGrade() != null) {
              request.setAttribute("inpGrade", objContract.getGrade().getId());
              request.setAttribute("inpGradeName", objContract.getGrade().getSearchKey());
            }
            if (objContract.getPosition() != null) {
              request.setAttribute("inpJobNo", objContract.getPosition().getId());
              request.setAttribute("inpJobName", objContract.getPosition().getJOBNo());
            }
            request.setAttribute("inpletterNo", objContract.getLetterNo());
            request.setAttribute("inpDecisionNo", objContract.getDecisionNo());
            request.setAttribute("inpAnnualBalance", objContract.getAnnualbalance());
            request.setAttribute("inpEmployeeId", employeeId);

            String todaydate = df.format(new Date());
            todaydate = dateYearFormat.format(df.parse(todaydate));
            todaydate = UtilityDAO.convertTohijriDate(todaydate);
            request.setAttribute("inptodayDate", todaydate);

            if (objContract.getStartDate() != null) {
              String date = df.format(objContract.getStartDate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpStartDate", date);
            }
            if (objContract.getExpirydate() != null) {
              String date = df.format(objContract.getExpirydate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpEndDate", date);
            }
            if (objContract.getLetterDate() != null) {
              String date = df.format(objContract.getLetterDate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpLettrDate", date);
            }
            if (objContract.getDecisionDate() != null) {
              String date = df.format(objContract.getDecisionDate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpDecisionDate", date);
            }
            if (objEmployee.getHiredate() != null) {
              String date = df.format(objEmployee.getHiredate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpHireDate", date);
            }
            request.setAttribute("inpContractId", contractId);
            request.setAttribute("inpEmployeeId", employeeId);
            request.setAttribute("inpAddressId", employeeaddId);

          }
          ContractVO clientage = dao.getagevalue(vars.getClient());
          request.setAttribute("inpminconser", clientage.getContractNo());
          request.setAttribute("inpmaxconser", clientage.getContractType());
          request.setAttribute("CancelHiring",
              employeeDao.checkEmploymentStatusCancel(vars.getClient(), employeeId));
          dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/contract/Contract.jsp");
        }

      } catch (final Exception e) {
        dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
        log4j.error("Error in Employment : ", e);
      } finally {
        OBContext.restorePreviousMode();
        try {
          con.close();
          if (dispatch != null) {
            response.setContentType("text/html; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            dispatch.include(request, response);
          } else
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (final Exception e) {
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
          log4j.error("Error in Employment : ", e);
        }
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}
