package sa.elm.ob.hcm.ad_forms.employment.header;

import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.contract.dao.ContractDAO;
import sa.elm.ob.hcm.ad_forms.employment.dao.EmploymentDAO;
import sa.elm.ob.hcm.ad_forms.employment.vo.EmploymentVO;
import sa.elm.ob.utility.util.UtilityDAO;

/**
 * 
 * @author gopalakrishnan on 26/10/2016
 */

public class Employment extends HttpSecureAppServlet {

  /**
   * Employment form details
   */
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    EmploymentDAO dao = null;
    ContractDAO contractDAO = null;
    Connection con = null;
    // EmploymentVO employmentVO = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    EmploymentVO vo = null;
    try {

    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Employee : ", e);
    } finally {
      try {
        OBContext.setAdminMode();
        String action = (request.getParameter("inpAction") == null ? ""
            : request.getParameter("inpAction"));
        String submitType = request.getParameter("SubmitType") == null ? ""
            : request.getParameter("SubmitType");
        String employeeId = request.getParameter("inpEmployeeId") == null ? ""
            : request.getParameter("inpEmployeeId");
        String inpExEmployeeId = request.getParameter("inpExEmployeeId") == null ? ""
            : request.getParameter("inpExEmployeeId");
        String employmentId = request.getParameter("inpEmploymentId") == null ? ""
            : request.getParameter("inpEmploymentId");
        String employeeaddId = (request.getParameter("inpAddressId") == null ? ""
            : request.getParameter("inpAddressId"));
        String nextTab = (request.getParameter("inpNextTab") == null ? ""
            : request.getParameter("inpNextTab"));
        String inpempstatus = (request.getParameter("inpEmpStatus") == null ? ""
            : request.getParameter("inpEmpStatus"));
        log4j.debug("inpempstatus" + inpempstatus);
        String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
            : request.getParameter("inpEmployeeStatus"));
        log4j.debug("employmentId" + employmentId);
        log4j.debug("action" + action);

        con = getConnection();
        vars = new VariablesSecureApp(request);
        dao = new EmploymentDAO(con);
        contractDAO = new ContractDAO(con);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
        // process save operation
        if (submitType != null && (submitType.equals("Save") || submitType.equals("SaveNew"))) {
          vo = new EmploymentVO();
          vo.setChangeReason(request.getParameter("inpReason"));
          vo.setGradeId(request.getParameter("inpGrade"));
          vo.setJobNo(request.getParameter("inpJobno"));
          vo.setEmpGrade(request.getParameter("inpEmpGrade"));
          vo.setJobCode(request.getParameter("inpJobCode"));
          vo.setJobTitle(request.getParameter("inpJobTitle"));
          vo.setDeptCode(request.getParameter("inpDeptCode"));
          vo.setDeptName(request.getParameter("inpDeptName"));
          vo.setSectionCode(request.getParameter("inpSectionCode"));
          vo.setSectionName(request.getParameter("inpSectionName"));
          vo.setLocation(request.getParameter("inpLocation"));
          // vo.setPayroll(request.getParameter("inpLocation"));
          vo.setEhcmPayrollDefinition(request.getParameter("inpPayRoll"));
          vo.setStatus(request.getParameter("inpStatus"));
          vo.setEmploymentCategoryId(request.getParameter("inpEmpCat"));
          vo.setPayScaleId(request.getParameter("inpPayScale"));
          vo.setGradeStepId(request.getParameter("inpGradeStep"));
          vo.setChangeReason(request.getParameter("inpReason"));
          vo.setEmploymentNo(request.getParameter("inpEmpNo"));
          vo.setStartDate(request.getParameter("inpStartDate"));
          vo.setDecisionNo(request.getParameter("inpDecisionNo"));
          vo.setEmployeeId(employeeId);
          vo.setEmpSupervisorId(request.getParameter("inpSupervisorId"));
          vo.setJoinworkRequest(vo.getJoinworkRequest());
          if (employmentId.equals(""))
            employmentId = dao.addEmployment(vars.getClient(), vars.getUser(), vo, vars);
          else
            employmentId = dao.updateEmployment(vars.getClient(), vars.getUser(), vo, employmentId,
                vars);
          if (nextTab.equals("") || nextTab.equals("EMPCTRCT")) {

            if (employmentId == null) {
              request.setAttribute("SaveStatus", "False");
              request.setAttribute("ErrorMsg", "Process Failed");
            } else {
              if (request.getParameter("inpEmploymentId").equals(""))
                request.setAttribute("SaveStatus", "Add-True");
              else
                request.setAttribute("SaveStatus", "Update-True");
              if (request.getParameter("SubmitType").equals("SaveNew"))
                employmentId = "";
            }
            log4j.debug("employmentId:" + employmentId);
            if (action.equals("GridView"))
              action = "GridView";
            else
              action = "EditView";
          } else {
            ServletContext context = this.getServletContext();
            if (!nextTab.equals("") && !nextTab.equals("EMPINF")) {
              String redirectStr = contractDAO.redirectStr(nextTab, inpExEmployeeId, inpempstatus,
                  inpEmployeeStatus);
              response.sendRedirect(context.getContextPath() + redirectStr);
            }
          }
        }
        if (action.equals("") || action.equals("GridView")) {
          log4j.debug("employeeId:" + employeeId);
          log4j.debug("exemp" + inpExEmployeeId);
          ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              view.getEhcmEmpPerinfo().getId());
          request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
          if (objEmployee.getGradeClass() != null) {
            if (objEmployee.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else {
            request.setAttribute("inpempCategory", "");
          }
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpIssuance", objEmployee.getStatus());
          request.setAttribute("inpName1", objEmployee.getArabicfullname());
          request.setAttribute("inpName2",
              objEmployee.getName().concat(" ")
                  .concat(StringUtils.isNotEmpty(objEmployee.getFathername())
                      ? objEmployee.getFathername()
                      : "")
                  .concat(" ")
                  .concat(StringUtils.isNotEmpty(objEmployee.getGrandfathername())
                      ? objEmployee.getGrandfathername()
                      : ""));
          request.setAttribute("inpAddressId", employeeaddId);
          if (StringUtils.isNotBlank(request.getParameter("inpEmpStatus"))) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }
          if (StringUtils.isNotBlank(request.getParameter("inpEmployeeStatus"))) {
            request.setAttribute("inpEmployeeStatus",
                request.getParameter("inpEmployeeStatus").toString());
          }

          request.setAttribute("cancelHiring",
              dao.employmentCancelHiring(employeeId, vars.getClient()));

          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employement/EmploymentList.jsp");
        }
        if (action.equals("EditView")) {
          log4j.debug("employeid:" + employeeId);
          log4j.debug("Edit view employmentId:" + employmentId);
          EhcmEmpPerInfo objEmployee = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          if (employmentId == null || employmentId.equals("") || employmentId.equals("null")) {
            String date = df.format(objEmployee.getStartDate());
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
            request.setAttribute("inpEmpGrade", "");
            request.setAttribute("inpEndDate", "");
            request.setAttribute("inpReason", "");
            request.setAttribute("inpGrade", "");
            request.setAttribute("inpJobno", "");
            request.setAttribute("inpJobCode", "");
            request.setAttribute("inpJobTitle", "");
            request.setAttribute("inpDeptCode", "");
            request.setAttribute("inpDeptName", "");
            request.setAttribute("inpSectionCode", "");
            request.setAttribute("inpSectionName", "");
            request.setAttribute("inpLocation", "");
            request.setAttribute("inpPayRoll", "");
            request.setAttribute("inpEmpCat", "");
            request.setAttribute("inpPayScale", "");
            request.setAttribute("inpGradeStep", "");
            request.setAttribute("inpEmpNo", objEmployee.getSearchKey());
            request.setAttribute("inpEmploymentId", "");
            request.setAttribute("inpEmployeeId", employeeId);
            request.setAttribute("inpIssuance", objEmployee.getStatus());
            request.setAttribute("inpSupervisorId", "");
            request.setAttribute("inpjoinflag", "");
            if (objEmployee.getHiredate() != null) {
              date = df.format(objEmployee.getHiredate());
              date = dateYearFormat.format(df.parse(date));
              date = UtilityDAO.convertTohijriDate(date);
              request.setAttribute("inpHireDate", date);
            }
            request.setAttribute("inpEmpCat", objEmployee.getGradeClass().getId());
            String empCatName = objEmployee.getGradeClass().getSearchKey() + "-"
                + objEmployee.getGradeClass().getName();
            log4j.debug(empCatName);
            request.setAttribute("inpEmpCatName", empCatName);
            request.setAttribute("inpReason", "H");
            request.setAttribute("inpStatus", "ACT");
            request.setAttribute("inpDecisionNo", objEmployee.getDecisionno());

            /* secondary */
            request.setAttribute("inpSecReason", "");
            request.setAttribute("inpSecGrade", "");
            request.setAttribute("inpSecJobno", "");
            request.setAttribute("inpSecJobCode", "");
            request.setAttribute("inpSecJobTitle", "");
            request.setAttribute("inpSecDeptCode", "");
            request.setAttribute("inpSecDeptName", "");
            request.setAttribute("inpSecSectionCode", "");
            request.setAttribute("inpSecSectionName", "");
            request.setAttribute("inpSecStartDate", "");
            request.setAttribute("inpSecEndDate", "");
            request.setAttribute("inpSecDecisionNo", "");
            request.setAttribute("inpSecEmpNo", "");
            request.setAttribute("inpSecDecisionDate", "");
            request.setAttribute("inpSecLocation", "");
            request.setAttribute("inpissued", objEmployee.getStatus());
            request.setAttribute("inpgovAgency", "");

          }
          if (employmentId != null && !employmentId.equals("") && !employmentId.equals("null")) {
            EmploymentInfo ObjEmpInfo = OBDal.getInstance().get(EmploymentInfo.class, employmentId);

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
            request.setAttribute("inpEmploymentId", employmentId);
            request.setAttribute("inpEmployeeId", employeeId);
            request.setAttribute("inpIssuance", objEmployee.getStatus());
            request.setAttribute("inpAddressId", employeeaddId);
            request.setAttribute("inpissued", objEmployee.getStatus());

            if (ObjEmpInfo != null) {
              request.setAttribute("inpReason", ObjEmpInfo.getChangereason());

              if (ObjEmpInfo.getGrade() != null) {
                request.setAttribute("inpGrade", ObjEmpInfo.getGrade().getId());
                request.setAttribute("inpGradeName", ObjEmpInfo.getGrade().getSearchKey());
              }

              if (ObjEmpInfo.getEmploymentgrade() != null) {
                request.setAttribute("inpEmpGrade", ObjEmpInfo.getEmploymentgrade().getId());
                request.setAttribute("inpEmpGradeName",
                    ObjEmpInfo.getEmploymentgrade().getSearchKey());
              }

              if (ObjEmpInfo.getPosition() != null) {
                request.setAttribute("inpJobno", ObjEmpInfo.getPosition().getId());
                request.setAttribute("inpJobnoName", ObjEmpInfo.getPosition().getJOBNo());
              }

              if (ObjEmpInfo.getEhcmPayrollDefinition() != null) {
                request.setAttribute("inpPayRoll", ObjEmpInfo.getEhcmPayrollDefinition().getId());
                request.setAttribute("inpPayRollName",
                    ObjEmpInfo.getEhcmPayrollDefinition().getPayrollName());
              }

              request.setAttribute("inpJobCode",
                  ObjEmpInfo.getJobcode() == null ? "" : ObjEmpInfo.getJobcode().getId());
              request.setAttribute("inpJobCodeValue",
                  ObjEmpInfo.getJobcode() == null ? "" : ObjEmpInfo.getJobcode().getJobCode());
              request.setAttribute("inpJobTitle",
                  ObjEmpInfo.getJobtitle() == null ? "" : ObjEmpInfo.getJobtitle());
              request.setAttribute("inpDeptCode",
                  ObjEmpInfo.getDeptcode() == null ? "" : ObjEmpInfo.getDeptcode().getId());
              request.setAttribute("inpDeptCodeValue",
                  ObjEmpInfo.getDeptcode() == null ? "" : ObjEmpInfo.getDeptcode().getSearchKey());
              request.setAttribute("inpDeptName",
                  ObjEmpInfo.getDepartmentName() == null ? "" : ObjEmpInfo.getDepartmentName());
              request.setAttribute("inpSectionCode",
                  ObjEmpInfo.getSectioncode() == null ? "" : ObjEmpInfo.getSectioncode().getId());
              request.setAttribute("inpSectionCodeValue", ObjEmpInfo.getSectioncode() == null ? ""
                  : ObjEmpInfo.getSectioncode().getSearchKey());
              request.setAttribute("inpSectionName",
                  ObjEmpInfo.getSectionName() == null ? "" : ObjEmpInfo.getSectionName());
              request.setAttribute("inpLocation", ObjEmpInfo.getLocation());
              request.setAttribute("inpEmpCat", ObjEmpInfo.getEmpcategory());
              log4j.debug(ObjEmpInfo.getEmpcategory());
              request.setAttribute("inpEmpCatName",
                  EmploymentDAO.getGradeClass(ObjEmpInfo.getEmpcategory()));

              if (ObjEmpInfo.getEhcmPayscale() != null) {
                request.setAttribute("inpPayScale", ObjEmpInfo.getEhcmPayscale().getId());
                request.setAttribute("inpPayScaleName",
                    ObjEmpInfo.getEhcmPayscale().getCommercialName());
              }
              if (ObjEmpInfo.getEhcmPayscaleline() != null) {
                request.setAttribute("inpGradeStep", ObjEmpInfo.getEhcmPayscaleline().getId());
                request.setAttribute("inpGradeStepName",
                    ObjEmpInfo.getEhcmPayscaleline().getEhcmProgressionpt().getPoint());
              }
              request.setAttribute("inpEmpNo", ObjEmpInfo.getEmployeeno());
              request.setAttribute("inpReason", ObjEmpInfo.getChangereason());
              if (ObjEmpInfo.getChangereason().equals("T"))
                request.setAttribute("inpReasonLabel",
                    dao.getEmploymentInfo(ObjEmpInfo.getChangereason(),
                        ObjEmpInfo.getChangereasoninfo(), ObjEmpInfo.getClient().getId(),
                        vars.getLanguage()));
              else if (ObjEmpInfo.getChangereason().equals("SUS")
                  || ObjEmpInfo.getChangereason().equals("SUE"))
                request.setAttribute("inpReasonLabel",
                    dao.getEmploymentInfo(ObjEmpInfo.getChangereason(), null,
                        ObjEmpInfo.getClient().getId(), vars.getLanguage()) + "-"
                        + dao.getSuspensionReason(ObjEmpInfo.getChangereasoninfo()));
              else
                request.setAttribute("inpReasonLabel",
                    dao.getEmploymentInfo(ObjEmpInfo.getChangereason(), null,
                        ObjEmpInfo.getClient().getId(), vars.getLanguage()));
              request.setAttribute("inpStatus", ObjEmpInfo.getAlertStatus());

              if (ObjEmpInfo.getStartDate() != null) {
                String date = df.format(ObjEmpInfo.getStartDate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpStartDate", date);
              }
              if (ObjEmpInfo.getEndDate() != null) {
                String date = df.format(ObjEmpInfo.getEndDate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpEndDate", date);
              }
              if (objEmployee.getHiredate() != null) {
                String date = df.format(objEmployee.getHiredate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpHireDate", date);
              }
              request.setAttribute("inpDecisionNo", ObjEmpInfo.getDecisionNo());
              if (ObjEmpInfo.getDecisionDate() != null) {
                String date = df.format(ObjEmpInfo.getDecisionDate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpDecisionDate", date);
              }
              if (ObjEmpInfo.getEhcmEmpSupervisor() != null) {
                request.setAttribute("inpSupervisorId", ObjEmpInfo.getEhcmEmpSupervisor().getId());
                request.setAttribute("inpSupervisorName",
                    ObjEmpInfo.getEhcmEmpSupervisor().getEmployee().getSearchKey() + "-"
                        + ObjEmpInfo.getEhcmEmpSupervisor().getEmployee().getArabicname());
              } else {
                request.setAttribute("inpSupervisorId", "");
                request.setAttribute("inpSupervisorName", "");
              }
              if (ObjEmpInfo.isJoinworkreq() != null) {
                request.setAttribute("inpjoinflag", ObjEmpInfo.isJoinworkreq());
              }
              log4j.debug("join" + request.getAttribute("inpjoinflag"));
              /* secondary part */
              log4j.debug("employmentId:" + employmentId);
              request.setAttribute("inpSecReason",
                  ObjEmpInfo.getSECChangeReason() == null ? "" : ObjEmpInfo.getSECChangeReason());
              request.setAttribute("inpSecGrade", ObjEmpInfo.getSecpositionGrade() == null ? ""
                  : ObjEmpInfo.getSecpositionGrade().getId());
              request.setAttribute("inpSecGradeValue", ObjEmpInfo.getSecpositionGrade() == null ? ""
                  : ObjEmpInfo.getSecpositionGrade().getSearchKey());
              request.setAttribute("inpSecJobno",
                  ObjEmpInfo.getSecjobno() == null ? "" : ObjEmpInfo.getSecjobno().getId());
              request.setAttribute("inpSecJobnoValue",
                  ObjEmpInfo.getSecjobno() == null ? "" : ObjEmpInfo.getSecjobno().getJOBNo());
              request.setAttribute("inpSecJobCode",
                  ObjEmpInfo.getSecjobcode() == null ? "" : ObjEmpInfo.getSecjobcode().getId());
              request.setAttribute("inpSecJobCodeValue", ObjEmpInfo.getSecjobcode() == null ? ""
                  : ObjEmpInfo.getSecjobcode().getJobCode());
              request.setAttribute("inpSecJobTitle",
                  ObjEmpInfo.getSecjobtitle() == null ? "" : ObjEmpInfo.getSecjobtitle());
              request.setAttribute("inpSecDeptCode",
                  ObjEmpInfo.getSECDeptCode() == null ? "" : ObjEmpInfo.getSECDeptCode().getId());
              request.setAttribute("inpSecDeptCodeValue", ObjEmpInfo.getSECDeptCode() == null ? ""
                  : ObjEmpInfo.getSECDeptCode().getSearchKey());
              request.setAttribute("inpSecDeptName",
                  ObjEmpInfo.getSECDeptName() == null ? "" : ObjEmpInfo.getSECDeptName());
              request.setAttribute("inpSecSectionCode", ObjEmpInfo.getSECSectionCode() == null ? ""
                  : ObjEmpInfo.getSECSectionCode().getId());
              request.setAttribute("inpSecSectionCodeValue",
                  ObjEmpInfo.getSECSectionCode() == null ? ""
                      : ObjEmpInfo.getSECSectionCode().getSearchKey());
              request.setAttribute("inpSecSectionName",
                  ObjEmpInfo.getSECSectionName() == null ? "" : ObjEmpInfo.getSECSectionName());
              request.setAttribute("inpSecLocation",
                  ObjEmpInfo.getSECLocation() == null ? "" : ObjEmpInfo.getSECLocation());
              if (ObjEmpInfo.getSECStartdate() != null) {
                String date = df.format(ObjEmpInfo.getSECStartdate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpSecStartDate", date);
              } else {
                request.setAttribute("inpSecStartDate", "");
              }
              if (ObjEmpInfo.getSECEnddate() != null) {
                String date = df.format(ObjEmpInfo.getSECEnddate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpSecEndDate", date);
              } else {
                request.setAttribute("inpSecEndDate", "");
              }

              request.setAttribute("inpSecEmpNo", ObjEmpInfo.getSECEmploymentNumber() == null ? ""
                  : ObjEmpInfo.getSECEmploymentNumber());
              if (ObjEmpInfo.getSECDecisionDate() != null) {
                String date = df.format(ObjEmpInfo.getSECDecisionDate());
                date = dateYearFormat.format(df.parse(date));
                date = UtilityDAO.convertTohijriDate(date);
                request.setAttribute("inpSecDecisionDate", date);
              } else {
                request.setAttribute("inpSecDecisionDate", "");
              }
              request.setAttribute("inpSecDecisionNo",
                  ObjEmpInfo.getSECDecisionNo() == null ? "" : ObjEmpInfo.getSECDecisionNo());
              request.setAttribute("inpgovAgency", ObjEmpInfo.getToGovernmentAgency());
            }
          }
          request.setAttribute("cancelHiring",
              dao.employmentCancelHiring(employeeId, vars.getClient()));
          dispatch = request
              .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employement/Employment.jsp");
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
