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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.utility.Image;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Category;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.geography.City;
import org.openbravo.model.common.geography.Country;

import sa.elm.ob.hcm.EhcmActiontype;
import sa.elm.ob.hcm.EhcmAddNationality;
import sa.elm.ob.hcm.EhcmEmpPerInfo;
import sa.elm.ob.hcm.EhcmPosition;
import sa.elm.ob.hcm.EhcmReligion;
import sa.elm.ob.hcm.EhcmTitletype;
import sa.elm.ob.hcm.EmploymentInfo;
import sa.elm.ob.hcm.ehcmempstatus;
import sa.elm.ob.hcm.ehcmempstatusv;
import sa.elm.ob.hcm.ehcmgradeclass;
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
      Category CatId = null;
      Currency CurId = null;
      con = getConnection();
      EhcmEmpPerInfo perinfo = null;
      vars = new VariablesSecureApp(request);
      dao = new EmployeeDAO(con);
      if (request.getParameter("SubmitType") != null
          && (request.getParameter("SubmitType").equals("Save")
              || request.getParameter("SubmitType").equals("SaveGrid")
              || request.getParameter("SubmitType").equals("SaveNew"))) {
        if (request.getParameter("inpStatus") != null
            && !request.getParameter("inpStatus").equals("C")) {
          OBQuery<Currency> curr = OBDal.getInstance().createQuery(Currency.class, " id='317'");
          if (curr.list().size() > 0) {
            for (Currency currs : curr.list()) {
              CurId = currs;
              break;
            }
          }
          // need to insert a record in employee personal info table
          if (employeeId.equals("")) {
            perinfo = OBProvider.getInstance().get(EhcmEmpPerInfo.class);
            perinfo.setNationalityIdentifier(request.getParameter("inpNatIdf").toString());
            perinfo.setSearchKey(request.getParameter("inpEmpNo"));
            perinfo.setCountry(OBDal.getInstance().get(Country.class,
                request.getParameter("inpCountry").toString()));
            perinfo.setCity(
                OBDal.getInstance().get(City.class, request.getParameter("inpCity").toString()));
            perinfo.setEhcmAddnationality(OBDal.getInstance().get(EhcmAddNationality.class,
                request.getParameter("inpNat").toString()));
            perinfo.setEhcmReligion(OBDal.getInstance().get(EhcmReligion.class,
                request.getParameter("inpRel").toString()));
          } else {
            perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          }
          if (employeeId.equals("")) {
            perinfo.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
            perinfo.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            perinfo.setCreationDate(new java.util.Date());
            perinfo.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          }

          perinfo.setUpdated(new java.util.Date());
          perinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));

          /* cat detail */
          perinfo.setEhcmActiontype(
              OBDal.getInstance().get(EhcmActiontype.class, request.getParameter("inpSalutation")));
          if (request.getParameter("inpEmpCat") != null)
            perinfo.setGradeClass(
                OBDal.getInstance().get(ehcmgradeclass.class, request.getParameter("inpEmpCat")));
          if (request.getParameter("inpStartDate") != null
              && request.getParameter("inpStartDate") != "")
            perinfo.setStartDate(
                dao.convertGregorian(request.getParameter("inpStartDate").toString()));
          if (request.getParameter("inpEndDate") != null
              && request.getParameter("inpEndDate") != "")
            perinfo.setEndDate(dao.convertGregorian(request.getParameter("inpEndDate").toString()));
          if (request.getParameter("inpHireDate") != null
              && request.getParameter("inpHireDate") != "")
            perinfo
                .setHiredate(dao.convertGregorian(request.getParameter("inpHireDate").toString()));
          if (request.getParameter("inpGovHireDate") != null
              && request.getParameter("inpGovHireDate") != "")
            perinfo.setGovhiredate(
                dao.convertGregorian(request.getParameter("inpGovHireDate").toString()));
          else
            perinfo.setGovhiredate(null);
          if (request.getParameter("inpMcsLetterDate") != null
              && request.getParameter("inpMcsLetterDate") != "")
            perinfo.setMcsletterdate(
                dao.convertGregorian(request.getParameter("inpMcsLetterDate").toString()));
          else
            perinfo.setMcsletterdate(null);
          perinfo.setMcsletterno(request.getParameter("inpMcsLetterNo").toString());
          if (request.getParameter("inpDecisionDate") != null
              && request.getParameter("inpDecisionDate") != "")
            perinfo.setDecisiondate(
                dao.convertGregorian(request.getParameter("inpDecisionDate").toString()));
          perinfo.setDecisionno(request.getParameter("inpDecisionNo").toString());

          /* name details */
          perinfo.setEhcmTitletype(
              OBDal.getInstance().get(EhcmTitletype.class, request.getParameter("inpTitle")));
          perinfo.setGender(request.getParameter("inpGen").toString());
          perinfo.setGrandfathername(request.getParameter("inpEngGraFatName").toString());
          perinfo.setArbgrafaname(request.getParameter("inpAraGraFatName").toString());

          perinfo.setArabicfamilyname(
              Utility.unescapeHTML(request.getParameter("inpAraFamName").toString()));
          perinfo.setFamilyname(request.getParameter("inpEngFamName").toString());

          perinfo
              .setArabicname(Utility.unescapeHTML(request.getParameter("inpAraFName").toString()));
          perinfo.setName(request.getParameter("inpEngFName"));

          perinfo.setArabicfatname(
              Utility.unescapeHTML(request.getParameter("inpAraFatName").toString()));
          perinfo.setFathername(request.getParameter("inpEngFatName").toString());

          perinfo.setArbfouname(
              Utility.unescapeHTML(request.getParameter("inpAraFourthName").toString()));
          perinfo.setFourthname(request.getParameter("inpEngFourthName"));

          /* contact details */
          perinfo.setMobno(request.getParameter("inpMobno"));
          perinfo.setHomeno(request.getParameter("inpHomeNo"));
          perinfo.setWorkno(request.getParameter("inpWorkNo"));
          perinfo.setOfficename(request.getParameter("inpOff"));
          perinfo.setEmail(request.getParameter("inpEmail"));
          perinfo.setLocation(request.getParameter("inpLoc"));
          if (!StringUtils.isEmpty(request.getParameter("inpmary"))) {
            perinfo
                .setMarrieddate(dao.convertGregorian(request.getParameter("inpmary").toString()));
          }
          /* image details */
          Image civImg = null;
          String mimetype = request.getParameter("inpcivfiletype");

          String civ = request.getParameter("inpcivfilebyte");
          String wrk = request.getParameter("inpwrkfilebyte");

          String[] civparts = civ.split("base64,");
          String[] wrkparts = wrk.split("base64,");

          if (civparts.length > 1) {
            byte[] civbytes = Base64.decodeBase64(civparts[1]);
            civImg = OBProvider.getInstance().get(Image.class);
            civImg.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
            civImg.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            civImg.setName(request.getParameter("inpcivfilename"));
            civImg.setBindaryData(civbytes);
            civImg.setWidth(new Long(200));
            civImg.setHeight(new Long(200));
            civImg.setMimetype(mimetype);
            OBDal.getInstance().save(civImg);
            OBDal.getInstance().flush();
            perinfo.setCIVAdImage(civImg);

          }
          if (wrkparts.length > 1) {
            Image wrkImg = null;
            byte[] wrkbytes = Base64.decodeBase64(wrkparts[1]);

            wrkImg = OBProvider.getInstance().get(Image.class);
            wrkImg.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
            wrkImg.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            wrkImg.setName(request.getParameter("inpwrkfilename"));
            wrkImg.setBindaryData(wrkbytes);
            wrkImg.setWidth(new Long(200));
            wrkImg.setHeight(new Long(200));
            wrkImg.setMimetype(mimetype);
            OBDal.getInstance().save(wrkImg);
            OBDal.getInstance().flush();
            perinfo.setWorkAdImage(wrkImg);

          }

          /* personal details */
          perinfo.setDob(dao.convertGregorian(request.getParameter("inpDoj").toString()));
          perinfo.setHeight(request.getParameter("inpHeight").toString());
          perinfo.setWeight(request.getParameter("inpWeight").toString());
          perinfo.setMarialstatus(request.getParameter("inpMarStat").toString());
          perinfo.setBloodtype(request.getParameter("inpBlodTy").toString());
          perinfo.setTownbirth(request.getParameter("inpTownBirth").toString());
          if (request.getParameter("inpStatus") != null
              && request.getParameter("inpStatus").equals("C"))
            perinfo.setStatus("C");
          else if (request.getParameter("inpStatus") != null
              && request.getParameter("inpStatus").equals("I"))
            perinfo.setStatus("I");
          else
            perinfo.setStatus("UP");
          if (request.getParameter("inpStatus") != null
              && !request.getParameter("inpStatus").equals("C"))
            perinfo.setPersonType(OBDal.getInstance().get(EhcmActiontype.class,
                request.getParameter("inpSalutation")));
          else if (request.getParameter("inpStatus") != null
              && request.getParameter("inpStatus").equals("C")) {
            EhcmEmpPerInfo experinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class,
                request.getParameter("inpExEmployeeId"));
            perinfo.setPersonType(experinfo.getEhcmActiontype());
          }

          OBDal.getInstance().save(perinfo);
          OBDal.getInstance().flush();
          employeeId = perinfo.getId();

          // update arabic full name
          dao.updateArabicFullName(employeeId);
          // update employement enddate
          if (request.getParameter("inpSalText").equals("HE")) {

            OBQuery<BusinessPartner> bp = OBDal.getInstance().createQuery(BusinessPartner.class,
                " ehcmEmpPerinfo.id='" + perinfo.getId() + "'");
            if (bp.list().size() > 0) {
              for (BusinessPartner bpart : bp.list()) {
                bpart.setSearchKey(perinfo.getSearchKey());
                bpart.setName(perinfo.getArabicfullname());
                bpart.setName2(perinfo.getArabicname());
                bpart.setEfinIdentityname("NID");
                bpart.setEfinNationalidnumber(perinfo.getNationalityIdentifier());
                bpart.setEhcmEmpPerinfo(perinfo);
                bpart.setEfinNationality(
                    OBDal.getInstance().get(Country.class, perinfo.getCountry().getId()));
                bpart.setEhcmProcessing(true);
                OBDal.getInstance().save(bpart);
                OBDal.getInstance().flush();
              }
            }
          }
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

        if (request.getParameter("inpStatus") != null
            && request.getParameter("inpStatus").equals("C")) {

          perinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              request.getParameter("inpExEmployeeId"));
          OBQuery<ehcmempstatus> duplicate = OBDal.getInstance().createQuery(ehcmempstatus.class,
              " ehcmEmpPerinfo.id='" + request.getParameter("inpExEmployeeId") + "'");

          duplicate.setMaxResult(1);
          if (duplicate.list().size() > 0) {
            ehcmempstatus employeestatus = duplicate.list().get(0);

            employeestatus.setUpdated(new java.util.Date());
            employeestatus.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            employeestatus.setDecisionno(request.getParameter("inpDecisionNo").toString());
            if (request.getParameter("inpStartDate") != null
                && request.getParameter("inpStartDate") != "")
              employeestatus.setStartDate(
                  dao.convertGregorian(request.getParameter("inpStartDate").toString()));
            if (request.getParameter("inpEndDate") != null
                && request.getParameter("inpEndDate") != "")
              employeestatus
                  .setTodate(dao.convertGregorian(request.getParameter("inpEndDate").toString()));

            employeestatus.setStatus(request.getParameter("inpStatus").toString());
            OBDal.getInstance().save(employeestatus);
            // OBDal.getInstance().flush();
            // OBDal.getInstance().commitAndClose();
            empstatus = employeestatus.getId();
            employeeId = request.getParameter("inpExEmployeeId");

          } else {
            int st = 0;
            ehcmempstatus ehcmempstatus = null;
            ehcmempstatus = dao.insertempstatus(perinfo,
                request.getParameter("inpStartDate").toString(),
                request.getParameter("inpEndDate").toString(),
                request.getParameter("inpDecisionNo"));
            empstatus = ehcmempstatus.getId();
            employeeId = request.getParameter("inpExEmployeeId");
          }
          request.setAttribute("savemsg", "Success");
        }
      }

      // /save close

      if (request.getParameter("inpAction") != null
          && request.getParameter("inpAction").equals("IssueDecision")) {
        if (request.getParameter("inpStatus") != null
            && !request.getParameter("inpStatus").equals("C")) {
          EhcmEmpPerInfo person = OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId);
          if (request.getParameter("inpSalText").equals("HE")
              || request.getParameter("inpSalText").equals("HSP")
              || request.getParameter("inpSalText").equals("HC")
              || request.getParameter("inpSalText").equals("HA")
              || request.getParameter("inpSalText").equals("HP")) {
            OBQuery<Category> cat = OBDal.getInstance().createQuery(Category.class,
                " name='Employee'");
            if (cat.list().size() > 0) {
              for (Category cats : cat.list()) {
                CatId = cats;
                break;
              }
            }
            BusinessPartner partner = OBProvider.getInstance().get(BusinessPartner.class);
            partner.setClient(OBDal.getInstance().get(Client.class, vars.getClient()));
            partner.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            partner.setCreationDate(new java.util.Date());
            partner.setCreatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            partner.setUpdated(new java.util.Date());
            partner.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            partner.setSearchKey(person.getSearchKey());
            partner.setName(person.getArabicname().concat(" ").concat(person.getArabicfatname())
                .concat(" ").concat(person.getArbgrafaname()));
            // partner.setName2(person.getFourthname());
            partner.setName2(person.getArabicname());
            partner.setBusinessPartnerCategory(CatId);
            partner.setEmployee(true);
            partner.setCustomer(true);
            partner.setVendor(true);
            partner.setEfinIdentityname("NID");
            partner.setCurrency(CurId);
            partner.setEfinNationalidnumber(person.getNationalityIdentifier());
            partner.setEfinNationality(
                OBDal.getInstance().get(Country.class, person.getCountry().getId()));
            partner.setEhcmEmpPerinfo(person);
            partner.setEfinDocumentno(
                Utility.getSequenceNo(con, vars.getClient(), bpartnerSeqName, false));
            OBDal.getInstance().save(partner);
            OBDal.getInstance().flush();

          }
          person.setStatus("I");
          person.setEmploymentStatus("AC");
          person.setDecisiondate(new java.util.Date());
          OBDal.getInstance().save(person);
          OBDal.getInstance().flush();

          // update employee name in Position
          if (person != null && person.getEhcmEmploymentInfoList().size() > 0) {
            for (EmploymentInfo objEmplyment : person.getEhcmEmploymentInfoList()) {
              EhcmPosition objPosition = OBDal.getInstance().get(EhcmPosition.class,
                  objEmplyment.getPosition().getId());

              /*
               * Task No.6797 objPosition
               * .setAssignedEmployee(OBDal.getInstance().get(EmployeeView.class, person.getId()));
               */
              objEmplyment.setDecisionDate(person.getDecisiondate());
              OBDal.getInstance().save(objEmplyment);
              // OBDal.getInstance().flush();

              // insert position employee history
              assingedOrReleaseEmpInPositionDAO.insertPositionEmployeeHisotry(
                  objEmplyment.getClient(), objEmplyment.getOrganization(),
                  objEmplyment.getEhcmEmpPerinfo(), null, objEmplyment.getStartDate(),
                  objEmplyment.getEndDate(), objEmplyment.getDecisionNo(),
                  objEmplyment.getDecisionDate(), objEmplyment.getPosition(), vars, null, null,
                  null);
            }
          }

          // insert into emp table
          // int count = dao.insertEmpLeave(person);

        }

        if (request.getParameter("inpStatus") != null
            && request.getParameter("inpStatus").equals("C")) {

          EhcmEmpPerInfo experinfo = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              request.getParameter("inpExEmployeeId"));
          EhcmEmpPerInfo perinfocan = OBDal.getInstance().get(EhcmEmpPerInfo.class,
              request.getParameter("inpExEmployeeId"));
          experinfo.setUpdated(new java.util.Date());
          experinfo.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
          experinfo.setEnabled(false);
          Date startdate = experinfo.getStartDate();
          Date dateBefore = new Date(perinfocan.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

          if (startdate.compareTo(perinfocan.getStartDate()) == 0)
            experinfo.setEndDate(perinfocan.getStartDate());
          else
            experinfo.setEndDate(dateBefore);
          OBDal.getInstance().save(experinfo);
          OBDal.getInstance().flush();
          // update employement enddate while cancel
          OBQuery<EmploymentInfo> empinfo = OBDal.getInstance().createQuery(EmploymentInfo.class,
              " ehcmEmpPerinfo.id='" + experinfo.getId()
                  + "' and enabled='Y' order by creationDate desc");
          empinfo.setMaxResult(1);
          if (empinfo.list().size() > 0) {
            EmploymentInfo info = empinfo.list().get(0);
            info.setUpdated(new java.util.Date());
            info.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
            info.setEnabled(false);
            Date startdateemp = info.getStartDate();
            Date dateBeforeemp = new Date(
                perinfocan.getStartDate().getTime() - 1 * 24 * 3600 * 1000);

            if (startdateemp.compareTo(perinfocan.getStartDate()) == 0)
              info.setEndDate(perinfocan.getStartDate());
            else
              info.setEndDate(dateBeforeemp);
            OBDal.getInstance().save(info);
            OBDal.getInstance().flush();
            /* release the employee */
            EhcmPosition pos = OBDal.getInstance().get(EhcmPosition.class,
                info.getPosition().getId());
            /*
             * Task No.6797 pos.setAssignedEmployee(null); OBDal.getInstance().save(info);
             * OBDal.getInstance().flush();
             */

            // delete the position employee history
            assingedOrReleaseEmpInPositionDAO.deletePositionEmployeeHisotry(experinfo, pos);

          }

          OBQuery<BusinessPartner> bpar = OBDal.getInstance().createQuery(BusinessPartner.class,
              " ehcmEmpPerinfo.id='" + request.getParameter("inpExEmployeeId") + "'");
          if (bpar.list().size() > 0) {
            for (BusinessPartner bp : bpar.list()) {
              bp.setUpdated(new java.util.Date());
              bp.setUpdatedBy(OBDal.getInstance().get(User.class, vars.getUser()));
              bp.setActive(false);
              OBDal.getInstance().save(bp);
              OBDal.getInstance().flush();
            }
          }

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

          else if (request.getParameter("inpStatus") != null) {
            vo = dao.getEmpEditList(employeeId, request.getParameter("inpStatus").toString());
          } else if (request.getParameter("inpissued") != null) {
            vo = dao.getEmpEditList(employeeId, request.getParameter("inpissued").toString());
          }
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
              dao.getEmployeeStatus((request.getAttribute("inpExEmployeeId") != null
                  ? request.getAttribute("inpExEmployeeId").toString()
                  : employeeId), vars.getLanguage()));
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

        // JSONObject ls = dao.getCountry(vars.getClient());
        // for (EmployeeVO vo1 : ls) {
        // request.setAttribute("inpCity", dao.getCity(vars.getClient(), vo.getCountryId()));
        // break;
        // }
        List<EmployeeVO> exitact = dao.getactionType(vars.getClient(), actTypeId, null);
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