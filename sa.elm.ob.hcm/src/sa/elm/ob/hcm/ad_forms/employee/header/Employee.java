package sa.elm.ob.hcm.ad_forms.employee.header;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.Currency;

import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ad_forms.employee.dao.EmployeeDAO;
import sa.elm.ob.hcm.ad_forms.employee.vo.EmployeeVO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAO;
import sa.elm.ob.hcm.ad_process.assignedOrReleasePosition.AssingedOrReleaseEmpInPositionDAOImpl;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class Employee extends HttpSecureAppServlet {

  /**
   * Employee form details
   */
  private static final long serialVersionUID = 1L;
  private static final String TMP_DIR_PATH = "/tmp";
  private static final String DESTINATION_DIR_PATH = "/files";
  private File tmpDir = null, destinationDir = null;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;

    tmpDir = new File(TMP_DIR_PATH);
    if (!tmpDir.isDirectory()) {
      new File(TMP_DIR_PATH).mkdir();
    }

    String realPath = getServletContext().getRealPath(DESTINATION_DIR_PATH);
    destinationDir = new File(realPath);
    if (!destinationDir.isDirectory()) {
      new File(realPath).mkdir();
    }
  }

  @SuppressWarnings("unchecked")
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    EmployeeDAO dao = null;
    Connection con = null;
    RequestDispatcher dispatch = null;
    VariablesSecureApp vars = null;
    EmployeeVO vo = null;
    String bpartnerSeqName = "DocumentNo_C_BPartner";
    AssingedOrReleaseEmpInPositionDAO assingedOrReleaseEmpInPositionDAO = new AssingedOrReleaseEmpInPositionDAOImpl();
    List<Currency> currencyList = null;
    List<Category> categoryList = null;
    List<EmploymentInfo> employInfoList = null;
    try {
      OBContext.setAdminMode();
      String action = (request.getParameter("inpAction") == null ? ""
          : request.getParameter("inpAction"));
      String employeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));
      String empCategory = (request.getParameter("inpempCategory") == null ? ""
          : request.getParameter("inpempCategory"));

      String employeeaddId = (request.getParameter("inpAddressId") == null ? ""
          : request.getParameter("inpAddressId"));
      String nextTab = (request.getParameter("inpNextTab") == null ? ""
          : request.getParameter("inpNextTab"));
      String empstatus = (request.getParameter("inpEmpStatus") == null ? ""
          : request.getParameter("inpEmpStatus"));
      String inpEmployeeStatus = (request.getParameter("inpEmployeeStatus") == null ? ""
          : request.getParameter("inpEmployeeStatus"));
      log4j.debug("action:" + action);
      Category categoryObj = null;
      Currency currencyObj = null;
      con = getConnection();
      EhcmEmpPerInfo perinfo = null;
      vars = new VariablesSecureApp(request);
      dao = new EmployeeDAO(con);

      // save operation
      if (request.getParameter("SubmitType") != null
          && (request.getParameter("SubmitType").equals("Save")
              || request.getParameter("SubmitType").equals("SaveGrid")
              || request.getParameter("SubmitType").equals("SaveNew"))) {

        // if we are saving other than cancel record then insert into employee perinfo table
        if (request.getParameter("inpStatus") != null
            && !request.getParameter("inpStatus").equals("C")) {

          // getting currency value for saudi arabia
          OBQuery<Currency> curr = OBDal.getInstance().createQuery(Currency.class, " id='317'");
          curr.setMaxResult(1);
          currencyList = curr.list();
          if (currencyList.size() > 0) {
            currencyObj = currencyList.get(0);
          }

          // insert or update the employee details and get the employee perinfo object
          perinfo = dao.insertOrUpdateEmployee(request, employeeId, vars);
          employeeId = perinfo.getId();

          // update arabic full name
          dao.updateArabicFullName(employeeId);

          // update business partner details after done issue decision
          if (StringUtils.isNotEmpty(employeeId)) {
            dao.updateBusinessPartnerDetails(perinfo, request.getParameter("inpSalText"));
          }

          // redirect tab
          if (nextTab.equals("")) {
            if (request.getParameter("SubmitType").equals("Save")) {
              action = "EditView";
              request.setAttribute("inpEmployeeId",
                  (perinfo.getId() == null ? "" : perinfo.getId()));
              if (perinfo.getGradeClass() != null) {
                if (perinfo.getGradeClass().isContract()) {
                  request.setAttribute("inpempCategory", "Y");
                } else {
                  request.setAttribute("inpempCategory", "");
                }
              } else {
                request.setAttribute("inpempCategory", "");
              }

            } else if (request.getParameter("SubmitType").equals("SaveNew")) {
              action = "EditView";
              employeeId = "";
              request.setAttribute("inpEmployeeId", "");
              request.setAttribute("inpempCategory", "");
            } else if (request.getParameter("SubmitType").equals("SaveGrid")) {
              action = "GridView";
              employeeId = "";
              request.setAttribute("inpEmployeeId", "");
              request.setAttribute("inpempCategory", "");
            }
          } else {
            ServletContext context = this.getServletContext();
            if (!nextTab.equals("") && !nextTab.equals("EMP")) {
              String redirectStr = dao.redirectStr(nextTab, employeeId, empstatus,
                  inpEmployeeStatus);
              response.sendRedirect(context.getContextPath() + redirectStr);
            }
          }

          request.setAttribute("savemsg", "Success");
        }

        // if we are saving cancek record then insert into empstatus table
        if (request.getParameter("inpStatus") != null
            && request.getParameter("inpStatus").equals("C")) {
          ehcmempstatus empstatusObj = dao.insertOrUpdateEmpStatus(request, vars);
          empstatus = empstatusObj.getId();
          employeeId = request.getParameter("inpExEmployeeId");
          request.setAttribute("savemsg", "Success");
        }
      }

      // issue decision process
      if (request.getParameter("inpAction") != null
          && request.getParameter("inpAction").equals("IssueDecision")) {

        // other than cancel record doing for issue decision need to insert a record in business
        // partner
        if (request.getParameter("inpStatus") != null
            && !request.getParameter("inpStatus").equals("C")) {
          EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);

          if (request.getParameter("inpSalText").equals("HE")
              || request.getParameter("inpSalText").equals("HC")
              || request.getParameter("inpSalText").equals("HA")) {

            OBQuery<Category> cat = OBDal.getInstance().createQuery(Category.class,
                " as e where e.ehcmCategorytype='EMP' and e.client.id=:clientId ");
            cat.setNamedParameter("clientId", vars.getClient());
            cat.setMaxResult(1);
            categoryList = cat.list();
            if (categoryList.size() > 0) {
              categoryObj = categoryList.get(0);
            }
            // insert business partner
            dao.insertBusinessPartner(vars, person, categoryObj, currencyObj, con, bpartnerSeqName);

          }
          // update employee status as "Issued" and employment status as "Active"
          person.setStatus("I");
          person.setEmploymentStatus("AC");
          person.setDecisiondate(new java.util.Date());
          OBDal.getInstance().save(person);
          OBDal.getInstance().flush();

          // maintain employee history in position details.
          if (person != null) {
            employInfoList = person.getEhcmEmploymentInfoList();
            if (employInfoList.size() > 0) {
              EmploymentInfo objEmplyment = employInfoList.get(0);
              dao.updatePositionOnIssueDecision(objEmplyment, person, vars);
            }
          }

          // insert business mission category
          dao.insertBusinessMissionCategory(person);

          // insert into emp table
          // int count = dao.insertEmpLeave(person);

        }

        // if we doing issue decision for cancel record then inactive the business partner and
        // relase the postion
        if (request.getParameter("inpStatus") != null
            && request.getParameter("inpStatus").equals("C")) {
          ehcmempstatus cancelEmp = OBDal.getInstance().get(ehcmempstatus.class,
              request.getParameter("inpEmployeeId"));
          EhcmEmpPerInfo issuedEmp = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              request.getParameter("inpExEmployeeId"));

          // update employement &employee endd ate for issued record while cancelling and relase the
          // position
          dao.updateEmployeeEnddateAndPositionReleaseInCancel(employeeId, vars, issuedEmp,
              cancelEmp);
          // inactive the business partner
          dao.inActiveTheBusinessPartner(request.getParameter("inpExEmployeeId"), vars);

        }
        HttpSession httpSession = request.getSession();
        request.setAttribute("issuemsg", "Issue Decision");
        if (request.getParameter("inpStatus") != null
            && request.getParameter("inpStatus").equals("C"))
          request.setAttribute("inpEmployeeId", empstatus);
        else
          request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));

        request.setAttribute("OrganizationList", Utility.getAccessibleOrgByList(vars));
        request.setAttribute("inpTitleList", dao.getTitleType(vars.getClient()));
        request.setAttribute("inpEmpCategory", dao.getEmpCategory(vars.getClient()));
        request.setAttribute("inpempCategory", (empCategory == null ? "" : empCategory));
        request.setAttribute("inpEmployeeCurrentStatus", dao.getEmployeeStatus());
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employee/EmployeeList.jsp");
      }

      else if (action.equals("EditView")) {
        if (employeeId != null && employeeId != "" && !employeeId.equals("null")) {
          ehcmempstatusv view = OBDal.getInstance().get(ehcmempstatusv.class, employeeId);
          EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              view.getEhcmEmpPerinfo().getId());
          // EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          log4j.debug("empstatus:" + request.getParameter("inpEmpStatus"));
          log4j.debug("inpStatus:" + request.getParameter("inpStatus"));
          log4j.debug("inpEmployeeStatus:" + request.getParameter("inpEmployeeStatus"));

          if (request.getParameter("inpEmpStatus") != null) {
            request.setAttribute("inpEmpStatus", request.getParameter("inpEmpStatus").toString());
          }

          if (request.getParameter("inpEmployeeStatus") != null) {
            if (request.getParameter("inpEmployeeStatus").equals("C")
                || request.getParameter("inpEmployeeStatus").equals("TE")) {
              request.setAttribute("inpEmployeeStatus",
                  request.getParameter("inpEmployeeStatus").toString());
              employeeId = empstatus;

            }
            vo = dao.getEmpEditList(employeeId,
                request.getParameter("inpEmployeeStatus").toString());
          }

          /*
           * else if (request.getParameter("inpStatus") != null) { vo =
           * dao.getEmpEditList(employeeId, request.getParameter("inpStatus").toString()); } else if
           * (request.getParameter("inpissued") != null) { vo = dao.getEmpEditList(employeeId,
           * request.getParameter("inpissued").toString()); }
           */
          request.setAttribute("inpEmployeeId", employeeId);
          if (person.getGradeClass() != null) {
            if (person.getGradeClass().isContract()) {
              request.setAttribute("inpempCategory", "Y");
            } else {
              request.setAttribute("inpempCategory", "");
            }
          } else
            request.setAttribute("inpempCategory", "");
          request.setAttribute("inpSalutation", vo.getActTypeId() == null ? "" : vo.getActTypeId());
          request.setAttribute("inpEmpCat",
              vo.getGradeclassId() == null ? "" : vo.getGradeclassId());
          request.setAttribute("inpStartDate", vo.getStartdate() == null ? "" : vo.getStartdate());
          request.setAttribute("inpHireDate", vo.getHiredate() == null ? "" : vo.getHiredate());
          request.setAttribute("inpGovHireDate",
              vo.getGovhiredate() == null ? "" : vo.getGovhiredate());
          request.setAttribute("inpEndDate", vo.getEnddate() == null ? "" : vo.getEnddate());
          request.setAttribute("inpMcsLetterNo", vo.getLetterno() == null ? "" : vo.getLetterno());
          request.setAttribute("inpMcsLetterDate",
              vo.getLetterdate() == null ? "" : vo.getLetterdate());
          request.setAttribute("inpDecisionNo",
              vo.getDecisionno() == null ? "" : vo.getDecisionno());
          request.setAttribute("inpDecisionDate",
              vo.getDecisiondate() == null ? "" : vo.getDecisiondate());
          request.setAttribute("inpTitle", vo.getTitleId() == null ? "" : vo.getTitleId());
          request.setAttribute("inpGen", vo.getGender() == null ? "" : vo.getGender());
          request.setAttribute("inpEngFName", vo.getEmpName() == null ? "" : vo.getEmpName());
          request.setAttribute("inpAraFName",
              vo.getEmpArabicName() == null ? "" : vo.getEmpArabicName());
          request.setAttribute("inpEngFourthName",
              vo.getFourthName() == null ? "" : vo.getFourthName());
          request.setAttribute("inpAraFourthName",
              vo.getArbfourthName() == null ? "" : vo.getArbfourthName());

          request.setAttribute("inpEngFatName", vo.getFatName() == null ? "" : vo.getFatName());
          request.setAttribute("inpAraFatName",
              vo.getArbfatName() == null ? "" : vo.getArbfatName());
          request.setAttribute("inpEngFamName",
              vo.getFamilyName() == null ? "" : vo.getFamilyName());
          request.setAttribute("inpAraFamName",
              vo.getArbfamilyName() == null ? "" : vo.getArbfamilyName());
          request.setAttribute("inpEngGraFatName",
              vo.getGradfatName() == null ? "" : vo.getGradfatName());
          request.setAttribute("inpAraGraFatName",
              vo.getArbgradfatName() == null ? "" : vo.getArbgradfatName());
          request.setAttribute("inpNat", vo.getNationalId() == null ? "" : vo.getNationalId());

          request.setAttribute("inpEmpNo", vo.getEmpNo() == null ? "" : vo.getEmpNo());
          request.setAttribute("inpNatIdf",
              vo.getNationalCode() == null ? "" : vo.getNationalCode());
          request.setAttribute("inpDoj", vo.getDob() == null ? "" : vo.getDob());
          request.setAttribute("inpCountryId", vo.getCountryId() == null ? "" : vo.getCountryId());
          request.setAttribute("inpCountryName",
              vo.getCountryId() == null ? "" : dao.countryName(vo.getCountryId()));
          request.setAttribute("inpCityId", vo.getCityId() == null ? "" : vo.getCityId());
          request.setAttribute("inpCityName",
              vo.getCityId() == null ? "" : dao.cityName(vo.getCityId()));
          request.setAttribute("inpRelId", vo.getReligionId() == null ? "" : vo.getReligionId());
          request.setAttribute("inpMarStat",
              vo.getMaritalstauts() == null ? "" : vo.getMaritalstauts());

          request.setAttribute("inpBlodTy", vo.getBloodtype() == null ? "" : vo.getBloodtype());
          request.setAttribute("inpTownBirth",
              vo.getTownofbirth() == null ? "" : vo.getTownofbirth());
          request.setAttribute("inpMobno", vo.getMobno() == null ? "" : vo.getMobno());
          request.setAttribute("inpOff", vo.getOffice() == null ? "" : vo.getOffice());
          request.setAttribute("inpHomeNo", vo.getHomeno() == null ? "" : vo.getHomeno());
          request.setAttribute("inpWorkNo", vo.getWorkno() == null ? "" : vo.getWorkno());
          request.setAttribute("inpLoc", vo.getLocation() == null ? "" : vo.getLocation());
          request.setAttribute("inpHeight", vo.getHeight() == null ? "" : vo.getHeight());
          request.setAttribute("inpWeight", vo.getWeight() == null ? "" : vo.getWeight());
          request.setAttribute("inpEmail", vo.getEmail() == null ? "" : vo.getEmail());
          request.setAttribute("inpstatus", vo.getStatus() == null ? "" : vo.getStatus());
          request.setAttribute("inpEmpCategory", dao.getEmpCategory(vars.getClient()));
          request.setAttribute("inpActionType", dao.getactionType(vars.getClient(), null, null));
          request.setAttribute("inpNationalList", dao.getNationality(vars.getClient()));
          request.setAttribute("inpReligionList", dao.getReligion(vars.getClient()));
          request.setAttribute("inpTitleList", dao.getTitleType(vars.getClient()));
          request.setAttribute("today", date);
          request.setAttribute("inpCivimg", vo.getCivimg() == null ? "" : vo.getCivimg());
          request.setAttribute("inpWrkimg", vo.getWrkimg() == null ? "" : vo.getWrkimg());
          request.setAttribute("inpmary", vo.getMarrieDate() == null ? "" : vo.getMarrieDate());
          List<EmployeeVO> actls = dao.getactionType(vars.getClient(), vo.getActTypeId(), null);
          for (EmployeeVO vo1 : actls) {
            request.setAttribute("inpActionTypeList",
                vo1.getActTypeName() == null ? "" : vo1.getActTypeName());
            request.setAttribute("inpSalText",
                vo1.getActTypeValue() == null ? "" : vo1.getActTypeValue());

            if (vo.getStatus().equals("C"))
              request.setAttribute("inpPersonType", person.getPersonType().getCancelPersontype());
            else if (vo.getStatus().equals("TE")) {
              request.setAttribute("inpPersonType", person.getPersonType().getCancelPersontype());
            } else
              request.setAttribute("inpPersonType",
                  vo1.getPersonType() == null ? "" : vo1.getPersonType());
            break;
          }

          request.setAttribute("inpStatus", request.getParameter("inpStatus"));
          request.setAttribute("inpEmployeeStatus", request.getParameter("inpEmployeeStatus"));
          request.setAttribute("inpExEmployeeId",
              dao.getStarteDate(vars.getClient(), vo.getEmpNo(), false));
          request.setAttribute("inpAddressId", employeeaddId);

          EmployeeVO clientage = dao.getagevalue(vars.getClient());
          request.setAttribute("inpminage", clientage.getActive());
          request.setAttribute("inpmaxage", clientage.getCategoryId());
          request.setAttribute("inpExtendService", dao.checkExtendServiceExist(employeeId));
          request.setAttribute("cancelHiring",
              dao.checkEmploymentStatusCancel(vars.getClient(), employeeId));
          request.setAttribute("Hiringdecision",
              dao.checkHiringDecisionStatus(vars.getClient(), employeeId));

          request.setAttribute("inpEmpCurrentStatus",
              dao.getEmployeeStatus(
                  (request.getAttribute("inpExEmployeeId") != null
                      ? request.getAttribute("inpExEmployeeId").toString() : employeeId),
                  vars.getLanguage()));
          dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employee/Employee.jsp");

        } else if (employeeId == null || employeeId == "" || employeeId.equals("null")) {
          DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
          SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
          String date = df.format(new Date());
          date = dateYearFormat.format(df.parse(date));
          date = UtilityDAO.convertTohijriDate(date);
          request.setAttribute("inpEmployeeId", employeeId);
          request.setAttribute("inpempCategory", empCategory);
          request.setAttribute("inpSalutation", "");
          request.setAttribute("inpEmpCat", "");
          request.setAttribute("inpStartDate", "");
          request.setAttribute("inpHireDate", "");
          request.setAttribute("inpGovHireDate", "");
          request.setAttribute("inpEndDate", "");
          request.setAttribute("inpMcsLetterNo", "");
          request.setAttribute("inpMcsLetterDate", "");
          request.setAttribute("inpDecisionNo", "");
          request.setAttribute("inpDecisionDate", "");
          request.setAttribute("inpTitle", "");
          request.setAttribute("inpGen", "");
          request.setAttribute("inpEngFName", "");
          request.setAttribute("inpAraFName", "");
          request.setAttribute("inpEngFourthName", "");
          request.setAttribute("inpAraFourthName", "");

          request.setAttribute("inpEngFatName", "");
          request.setAttribute("inpAraFatName", "");
          request.setAttribute("inpEngFamName", "");
          request.setAttribute("inpAraFamName", "");
          request.setAttribute("inpEngGraFatName", "");
          request.setAttribute("inpAraGraFatName", "");
          request.setAttribute("inpNat", "");

          request.setAttribute("inpEmpNo", "");
          request.setAttribute("inpNatIdf", "");
          request.setAttribute("inpDoj", "");
          request.setAttribute("inpCountryId", "");
          request.setAttribute("inpCityId", "");
          request.setAttribute("inpRelId", "");
          request.setAttribute("inpMarStat", "");

          request.setAttribute("inpBlodTy", "");
          request.setAttribute("inpTownBirth", "");
          request.setAttribute("inpMobno", "");
          request.setAttribute("inpOff", "");
          request.setAttribute("inpHomeNo", "");
          request.setAttribute("inpWorkNo", "");
          request.setAttribute("inpLoc", "");
          request.setAttribute("inpHeight", "");
          request.setAttribute("inpWeight", "");
          request.setAttribute("inpEmail", "");
          request.setAttribute("inpEmpCurrentStatus", "");

          request.setAttribute("inpEmpCategory", dao.getEmpCategory(vars.getClient()));
          request.setAttribute("today", date);
          request.setAttribute("inpActionType", dao.getactionType(vars.getClient(), null, null));
          request.setAttribute("inpNationalList", dao.getNationality(vars.getClient()));
          request.setAttribute("inpReligionList", dao.getReligion(vars.getClient()));
          request.setAttribute("inpTitleList", dao.getTitleType(vars.getClient()));
          request.setAttribute("inpstatus", "UP");

          List<EmployeeVO> ls = dao.getDefaultCountry(vars.getClient());
          for (EmployeeVO vo1 : ls) {
            if (vo1.getIsdefault().equals("Y")) {
              request.setAttribute("inpCountryId", vo1.getCountryId());
              request.setAttribute("inpCountryName", dao.countryName(vo1.getCountryId()));

              break;
            }
          }

          List<EmployeeVO> actls = dao.getactionType(vars.getClient(), null, null);
          for (EmployeeVO vo1 : actls) {
            /*
             * request.setAttribute("inpActionTypeList", vo1.getActTypeName());
             * request.setAttribute("inpPersonType", vo1.getPersonType());
             */
            break;
          }
          EmployeeVO clientage = dao.getagevalue(vars.getClient());
          request.setAttribute("inpminage", clientage.getActive());
          request.setAttribute("inpmaxage", clientage.getCategoryId());
          dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employee/Employee.jsp");
        }
      } else if (action.equals("") || action.equals("GridView")) {
        HttpSession httpSession = request.getSession();
        httpSession.setAttribute("Employee_ChildOrg", Utility.getAccessibleOrg(vars));
        request.setAttribute("inpEmployeeId", (employeeId == null ? "" : employeeId));
        request.setAttribute("OrganizationList", Utility.getAccessibleOrgByList(vars));
        request.setAttribute("inpTitleList", dao.getTitleType(vars.getClient()));
        request.setAttribute("inpEmpCategory", dao.getEmpCategory(vars.getClient()));
        request.setAttribute("inpempCategory", (empCategory == null ? "" : empCategory));
        request.setAttribute("inpEmployeeCurrentStatus", dao.getEmployeeStatus());
        dispatch = request
            .getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employee/EmployeeList.jsp");
      } else if (action.equals("Cancel")) {
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat dateYearFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(new Date());
        date = dateYearFormat.format(df.parse(date));
        date = UtilityDAO.convertTohijriDate(date);
        String actTypeId = "";

        List<EmployeeVO> actls = dao.getactionType(vars.getClient(), null, "CAH");
        for (EmployeeVO vo1 : actls) {
          actTypeId = vo1.getActTypeId();
          break;
        }
        vo = dao.getEmpEditList(employeeId, "I");
        request.setAttribute("inpEmployeeId", "");
        request.setAttribute("inpSalutation", actTypeId);
        request.setAttribute("inpEmpCat", vo.getGradeclassId());
        request.setAttribute("inpStartDate", vo.getStartdate());
        request.setAttribute("inpHireDate", vo.getHiredate());
        request.setAttribute("inpGovHireDate", vo.getGovhiredate());
        request.setAttribute("inpEndDate", vo.getEnddate());
        request.setAttribute("inpMcsLetterNo", null);
        request.setAttribute("inpMcsLetterDate", null);
        request.setAttribute("inpDecisionNo", null);
        request.setAttribute("inpDecisionDate", null);
        request.setAttribute("inpTitle", vo.getTitleId());
        request.setAttribute("inpGen", vo.getGender());
        request.setAttribute("inpEngFName", vo.getEmpName());
        request.setAttribute("inpAraFName", vo.getEmpArabicName());
        request.setAttribute("inpEngFourthName", vo.getFourthName());
        request.setAttribute("inpAraFourthName", vo.getArbfourthName());

        request.setAttribute("inpEngFatName", vo.getFatName());
        request.setAttribute("inpAraFatName", vo.getArbfatName());
        request.setAttribute("inpEngFamName", vo.getFamilyName());
        request.setAttribute("inpAraFamName", vo.getArbfamilyName());
        request.setAttribute("inpEngGraFatName", vo.getGradfatName());
        request.setAttribute("inpAraGraFatName", vo.getArbgradfatName());
        request.setAttribute("inpNat", vo.getNationalId());

        request.setAttribute("inpEmpNo", vo.getEmpNo());
        request.setAttribute("inpNatIdf", vo.getNationalCode());
        request.setAttribute("inpDoj", vo.getDob());
        request.setAttribute("inpCountryId", vo.getCountryId());
        request.setAttribute("inpCountryName", dao.countryName(vo.getCountryId()));
        request.setAttribute("inpCityId", vo.getCityId());
        request.setAttribute("inpCityName", dao.cityName(vo.getCityId()));
        request.setAttribute("inpRelId", vo.getReligionId());
        request.setAttribute("inpMarStat", vo.getMaritalstauts());

        request.setAttribute("inpBlodTy", vo.getBloodtype());
        request.setAttribute("inpTownBirth", vo.getTownofbirth());
        request.setAttribute("inpMobno", vo.getMobno());
        request.setAttribute("inpOff", vo.getOffice());
        request.setAttribute("inpHomeNo", vo.getHomeno());
        request.setAttribute("inpWorkNo", vo.getWorkno());
        request.setAttribute("inpLoc", vo.getLocation());
        request.setAttribute("inpHeight", vo.getHeight());
        request.setAttribute("inpWeight", vo.getWeight());
        request.setAttribute("inpEmail", vo.getEmail());
        request.setAttribute("inpstatus", "C");
        request.setAttribute("inpEmpCategory", dao.getEmpCategory(vars.getClient()));
        request.setAttribute("inpActionType", dao.getactionType(vars.getClient(), null, null));
        /*
         * request.setAttribute("inpActionType", dao.getactionType(vars.getClient(), actTypeId,
         * null));
         */request.setAttribute("inpNationalList", dao.getNationality(vars.getClient()));
        request.setAttribute("inpReligionList", dao.getReligion(vars.getClient()));
        request.setAttribute("inpTitleList", dao.getTitleType(vars.getClient()));
        request.setAttribute("today", date);
        request.setAttribute("inpCivimg", vo.getCivimg());
        request.setAttribute("inpWrkimg", vo.getWrkimg());
        request.setAttribute("inpMcsLetterNo", "");
        request.setAttribute("inpDecisionNo", "");
        request.setAttribute("inpMcsLetterDate", "");
        request.setAttribute("inpDecisionDate", "");
        request.setAttribute("inpEmpCurrentStatus", "");
        request.setAttribute("inpmary", vo.getMarrieDate());

        // JSONObject ls = dao.getCountry(vars.getClient());
        // for (EmployeeVO vo1 : ls) {
        // request.setAttribute("inpCity", dao.getCity(vars.getClient(), vo.getCountryId()));
        // break;
        // }
        List<EmployeeVO> exitact = dao.getactionType(vars.getClient(), vo.getActTypeId(), null);
        for (EmployeeVO voex : exitact) {
          request.setAttribute("inpPersonType", voex.getCancelpersontype());
          break;
        }

        List<EmployeeVO> canact = dao.getactionType(vars.getClient(), actTypeId, null);
        for (EmployeeVO vo1 : canact) {
          request.setAttribute("inpActionTypeList", vo1.getActTypeName());
          break;
        }
        request.setAttribute("inpExEmpActType", vo.getActTypeId());
        request.setAttribute("inpExEmployeeId",
            dao.getStarteDate(vars.getClient(), vo.getEmpNo(), false));
        EmployeeVO clientage = dao.getagevalue(vars.getClient());
        request.setAttribute("inpminage", clientage.getActive());
        request.setAttribute("inpmaxage", clientage.getCategoryId());
        request.setAttribute("inpEmployeeStatus", "C");
        dispatch = request.getRequestDispatcher("../web/sa.elm.ob.hcm/jsp/employee/Employee.jsp");
      }
      OBDal.getInstance().commitAndClose();
    } catch (final Exception e) {
      dispatch = request.getRequestDispatcher("../web/jsp/ErrorPage.jsp");
      log4j.error("Error in Employee : ", e);
      OBDal.getInstance().rollbackAndClose();
    } finally {
      try {
        con.close();
        OBContext.restorePreviousMode();
        if (dispatch != null) {
          response.setContentType("text/html; charset=UTF-8");
          response.setCharacterEncoding("UTF-8");
          dispatch.include(request, response);
        } else
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (final Exception e) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        log4j.error("Error in Employee : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }
}