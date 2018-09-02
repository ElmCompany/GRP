package sa.elm.ob.hcm.ad_forms.personalpaymentmethod.ajax;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
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

import sa.elm.ob.finance.EfinBank;
import sa.elm.ob.finance.EfinBankBranch;
import sa.elm.ob.hcm.EHCMPersonalPaymethd;
import sa.elm.ob.hcm.EHCMPpmBankdetail;
import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.dao.PersonalPaymentMethodDAO;
import sa.elm.ob.hcm.ad_forms.personalpaymentmethod.vo.PersonalPaymentMethodVO;
import sa.elm.ob.utility.util.Utility;
import sa.elm.ob.utility.util.UtilityDAO;

public class PersonalPaymentMethodAjax extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Connection con = null;
    PersonalPaymentMethodVO vo = null;
    PersonalPaymentMethodDAO dao = null;

    try {
      OBContext.setAdminMode();
      con = getConnection();
      boolean isdefault = false;
      String action = (request.getParameter("action") == null ? ""
          : request.getParameter("action"));
      String PersonalPaymethdId = (request.getParameter("inpehcmPersonalPaymethdId") == null ? ""
          : request.getParameter("inpehcmPersonalPaymethdId"));
      String EmployeeId = (request.getParameter("inpEmployeeId") == null ? ""
          : request.getParameter("inpEmployeeId"));
      String startDate = null;
      String endDate = null;
      String gregorianStartDate = "", gregorianEndDate = "";
      DateFormat yearFormat = sa.elm.ob.utility.util.Utility.YearFormat;
      JSONObject jsonResponse = null;
      // getbankdetaillist
      if (action.equals("getbankdetaillist")) {

        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        dao = new PersonalPaymentMethodDAO(con);
        PersonalPaymentMethodVO VO1 = new PersonalPaymentMethodVO();
        if (searchFlag != null && searchFlag.equals("true")) {

          if (request.getParameter("Bank_Name") != null)
            VO1.setbankname(request.getParameter("Bank_Name").replace("'", "''"));
          if (request.getParameter("Bank_Branch") != null)
            VO1.setbankbranch(request.getParameter("Bank_Branch").replace("'", "''"));
          if (request.getParameter("Account_Name") != null)
            VO1.setaccountname(request.getParameter("Account_Name").replace("'", "''"));
          if (request.getParameter("Account_Number") != null)
            VO1.setaccountnum(request.getParameter("Account_Number").replace("'", "''"));
          if (request.getParameter("Percentage") != null)
            VO1.setpercentage(request.getParameter("Percentage").replace("'", "''"));

          if (!StringUtils.isEmpty(request.getParameter("Start_Date"))) {
            gregorianStartDate = Utility.convertToGregorian(request.getParameter("Start_Date"));
            VO1.setStartdate(request.getParameter("Start_Date_s") + "##" + gregorianStartDate);
          }
          if (!StringUtils.isEmpty(request.getParameter("End_Date"))) {
            gregorianEndDate = Utility.convertToGregorian(request.getParameter("End_Date"));
            VO1.setEnddate(request.getParameter("End_Date_s") + "##" + gregorianEndDate);
          }

          if (!StringUtils.isEmpty(request.getParameter("Default"))
              && !request.getParameter("Default").equals("0")) {
            VO1.setDefaultval(request.getParameter("Default"));
          }

        }
        int totalRecord = dao.getBankdetailCount(vars.getClient(), PersonalPaymethdId, searchFlag,
            VO1);
        log4j.debug("totalRecords:" + totalRecord);
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
        // dao = new PersonalPaymentMethodDAO(con);
        List<PersonalPaymentMethodVO> list = dao.getbankdetaillist(vars.getClient(),
            PersonalPaymethdId, searchFlag, VO1, sortColName, sortColType);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());
        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            PersonalPaymentMethodVO VO = (PersonalPaymentMethodVO) list.get(i);
            xmlData.append("<row id='" + VO.getbankdetailId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getbankname() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getbankbranch() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getaccountname() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getaccountnum() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getpercentage() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getStartdate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getEnddate() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getisdefault() + "]]></cell>");
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

      // SaveBankDetailList
      else if (action.equals("SaveBankDetailList")) {
        int percentage = 0;
        String operation = request.getParameter("oper");
        String employeeId = request.getParameter("inpEmployeeId");
        String bankname = request.getParameter("Bank_Name");
        String bankbranch = request.getParameter("Bank_Branch");
        String accountname = request.getParameter("Account_Name");
        String accountnum = request.getParameter("Account_Number");
        if (request.getParameter("Percentage") != null
            && !request.getParameter("Percentage").equals("")) {
          percentage = Integer.parseInt((request.getParameter("Percentage")));
        }
        String startdate = request.getParameter("Start_Date");
        startDate = UtilityDAO.convertToGregorian(startdate);
        String enddate = request.getParameter("End_Date");
        endDate = UtilityDAO.convertToGregorian(enddate);
        if (request.getParameter("Default") != null
            && request.getParameter("Default").equals("True")) {
          isdefault = true;
        }
        String bankdetailId = request.getParameter("id");

        if (operation.equals("add") || bankdetailId.length() != 32) {

          EHCMPpmBankdetail objLine = OBProvider.getInstance().get(EHCMPpmBankdetail.class);

          objLine.setEhcmPersonalPaymethd(
              OBDal.getInstance().get(EHCMPersonalPaymethd.class, PersonalPaymethdId));
          // objLine.setEhcmEmpPerinfo(OBDal.getInstance().get(EhcmEmpPerInfo.class, employeeId));
          objLine.setEfinBank(OBDal.getInstance().get(EfinBank.class, bankname));
          objLine.setBankBranch(OBDal.getInstance().get(EfinBankBranch.class, bankbranch));
          objLine.setAccountName(accountname);
          objLine.setAccountNumber(accountnum);
          objLine.setPercentage(new BigDecimal(percentage));
          if (startdate != "" && startdate != null) {
            objLine.setStartDate(yearFormat.parse(startDate));
          }
          if (enddate != "" && enddate != null) {
            objLine.setEndDate(yearFormat.parse(endDate));
            objLine.setActive(false);
          }
          objLine.setDefault(isdefault);
          OBDal.getInstance().save(objLine);
          OBDal.getInstance().flush();
        } else if (operation.equals("edit") && bankdetailId.length() == 32) {
          EHCMPpmBankdetail objLine = OBDal.getInstance().get(EHCMPpmBankdetail.class,
              bankdetailId);
          EHCMPersonalPaymethd method = OBDal.getInstance().get(EHCMPersonalPaymethd.class,
              PersonalPaymethdId);
          objLine.setEhcmPersonalPaymethd(method);
          objLine.setEfinBank(OBDal.getInstance().get(EfinBank.class, bankname));
          objLine.setBankBranch(OBDal.getInstance().get(EfinBankBranch.class, bankbranch));
          objLine.setAccountName(accountname);
          objLine.setAccountNumber(accountnum);
          objLine.setPercentage(new BigDecimal(percentage));
          if (startdate != "" && startdate != null) {
            objLine.setStartDate(yearFormat.parse(startDate));
          }
          if (enddate != "" && enddate != null) {
            objLine.setEndDate(yearFormat.parse(endDate));
            objLine.setActive(false);
          }
          objLine.setDefault(isdefault);
          OBDal.getInstance().save(objLine);
          OBDal.getInstance().flush();
        } else if (operation.equals("del")) {
          OBDal.getInstance()
              .remove(OBDal.getInstance().get(EHCMPpmBankdetail.class, bankdetailId));
        }
      }
      // getPaymentMethodList
      else if (action.equals("GetPaymentMethodList")) {

        String sortColName = request.getParameter("sidx");
        String sortColType = request.getParameter("sord");
        String searchFlag = request.getParameter("_search");
        int rows = Integer.parseInt(request.getParameter("rows"));
        int page = Integer.parseInt(request.getParameter("page"));
        int totalPage = 1;
        int offset = 0;
        dao = new PersonalPaymentMethodDAO(con);
        PersonalPaymentMethodVO VO1 = new PersonalPaymentMethodVO();
        if (searchFlag != null && searchFlag.equals("true")) {

          if (request.getParameter("Payment_Type_Code") != null
              && request.getParameter("Payment_Type_Code") != "")
            VO1.setpaymenttypecode(request.getParameter("Payment_Type_Code").replace("'", "''"));
          if (request.getParameter("Payment_Type_Name") != null
              && request.getParameter("Payment_Type_Name") != "")
            VO1.setpaymenttypename(request.getParameter("Payment_Type_Name").replace("'", "''"));
          if (request.getParameter("Currency") != null && request.getParameter("Currency") != "")
            VO1.setcurrency(request.getParameter("Currency").replace("'", "''"));
          log4j.debug("default :" + request.getParameter("Default"));

          if (!StringUtils.isEmpty(request.getParameter("Default"))
              && !request.getParameter("Default").equals("0")) {
            VO1.setDefaultval(request.getParameter("Default"));
          }

        }
        int totalRecord = dao.getPaymentMethodCount(vars.getClient(), EmployeeId, searchFlag, VO1);
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

        // dao = new PersonalPaymentMethodDAO(con);
        List<PersonalPaymentMethodVO> list = dao.GetPaymentMethodList(vars.getClient(), EmployeeId,
            searchFlag, VO1, sortColName, sortColType);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        StringBuffer xmlData = new StringBuffer("<?xml version='1.0' encoding='utf-8'?><rows>");
        log4j.debug("totalRecord:" + totalRecord + ">> list size" + list.size());

        if (list.size() > 0) {
          for (int i = 0; i < list.size(); i++) {
            PersonalPaymentMethodVO VO = (PersonalPaymentMethodVO) list.get(i);
            xmlData.append("<row id='" + VO.getpersonalpaymentmethodId() + "'>");
            xmlData.append("<cell><![CDATA[" + VO.getpaymenttypecode() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getpaymenttypename() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getcurrency() + "]]></cell>");
            xmlData.append("<cell><![CDATA[" + VO.getDefaultval() + "]]></cell>");
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
      // getpayrollpaymenttypemethodcurrency
      else if (action.equals("getpayrollpaymenttypemethodcurrency")) {
        StringBuffer sb = new StringBuffer();
        dao = new PersonalPaymentMethodDAO(con);
        List<PersonalPaymentMethodVO> list = dao
            .Getpayrollpaymentdetailrecords(request.getParameter("payrollpaytypemethodId"));
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        if (list.size() > 0) {
          sb.append("<GetCurrency>");
          for (int i = 0; i < list.size(); i++) {
            PersonalPaymentMethodVO VO = (PersonalPaymentMethodVO) list.get(i);
            sb.append("<CurrencyList>");
            sb.append("<CurrencyId>" + VO.getcurrencyId() + "</CurrencyId>");
            sb.append("<CurrencyName>" + VO.getcurrency() + "</CurrencyName>");
            sb.append("</CurrencyList>");
          }
          sb.append("</GetCurrency>");
          response.getWriter().write(sb.toString());
        }
      } else if (action.equals("checkPercentageValidation")) {
        try {
          if (!request.getParameter("inpPercentage").equals(null)) {
            BigDecimal percentage = new BigDecimal(
                Integer.parseInt(request.getParameter("inpPercentage")));
            dao = new PersonalPaymentMethodDAO(con);
            boolean validPercentage = dao.checkPercentageValidation(percentage,
                request.getParameter("inppaymentmethodId"), vars.getClient(),
                request.getParameter("inpbankdetailId"));
            jsonResponse = new JSONObject();
            jsonResponse.put("validPercentage", validPercentage);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse.toString());
          }
        } catch (final Exception e) {
          log4j.error("Exception in checkPercentageValidation  : ", e);
        }
      } else if (action.equals("deletePersonalPayment")) {
        dao = new PersonalPaymentMethodDAO(con);
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.getWriter().write("<deletePersonalPayment>");
        response.getWriter()
            .write("<Response>" + dao.deletePersonalPayment(PersonalPaymethdId) + "</Response>");
        response.getWriter().write("</deletePersonalPayment>");
      } else if (action.equals("validaccountnumber")) {
        try {
          dao = new PersonalPaymentMethodDAO(con);
          boolean checkaccountNumber = dao.checkaccountNumber(
              request.getParameter("inpbankdetailId"), request.getParameter("inpaccountNumber"),
              request.getParameter("inpPaymentMethodId"), request.getParameter("inprowid"),
              request.getParameter("inpstartdate"), request.getParameter("inpenddate"));
          jsonResponse = new JSONObject();
          jsonResponse.put("validaccnumber", checkaccountNumber);
          response.setCharacterEncoding("UTF-8");
          response.getWriter().write(jsonResponse.toString());
        } catch (final Exception e) {
          log4j.error("Exception in checkAccountValidation  : ", e);
        }
      } else if (action.equals("Ibanvalidaiton")) {
        try {
          dao = new PersonalPaymentMethodDAO(con);
          String checkibanvalidation = dao
              .checkIbanvalidation(request.getParameter("inpaccountNumber"));
          jsonResponse = new JSONObject();
          jsonResponse.put("checkibanvalidation", checkibanvalidation);
          response.setCharacterEncoding("UTF-8");
          response.getWriter().write(jsonResponse.toString());
        } catch (final Exception e) {
          log4j.error("Exception in IbanValidation  : ", e);
        }
      } else if (action.equals("bankdetails")) {

        dao = new PersonalPaymentMethodDAO(con);
        JSONArray jsonArray = null;
        List<EfinBankBranch> bankdetaillist = null;
        bankdetaillist = dao.getBankBranch(request.getParameter("inpbankId"));
        jsonArray = new JSONArray();
        if (bankdetaillist != null && bankdetaillist.size() > 0) {
          for (EfinBankBranch bankdet : bankdetaillist) {
            jsonResponse = new JSONObject();
            jsonResponse.put("BankId", bankdet.getId());
            jsonResponse.put("Branchcode",
                bankdet.getBranchCode() + " - " + bankdet.getBranchName());
            jsonArray.put(jsonResponse);
          }
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
      }

      else if (action.equals("bankdetailsonLoad")) {

        dao = new PersonalPaymentMethodDAO(con);
        JSONArray jsonArray = null;
        List<EfinBankBranch> bankdetaillist = null;
        bankdetaillist = dao.getBankBranchOnLoad(vars.getClient());
        jsonArray = new JSONArray();
        if (bankdetaillist != null && bankdetaillist.size() > 0) {
          for (EfinBankBranch bankdet : bankdetaillist) {
            jsonResponse = new JSONObject();
            jsonResponse.put("BankId", bankdet.getId());
            jsonResponse.put("Branchcode",
                bankdet.getBranchCode() + " - " + bankdet.getBranchName());
            jsonArray.put(jsonResponse);
          }
        }
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonArray.toString());
      }

      else if (action.equals("checkDefaultPPMAlreadyExist")) {
        StringBuffer sb = new StringBuffer();
        try {
          dao = new PersonalPaymentMethodDAO(con);
          Boolean chk = dao.checkDefaultPersonalPaymentMethodAlreadyExists(
              request.getParameter("inpempId"), request.getParameter("inppersonalpaymethodId"),
              vars.getClient());
          sb.append("<checkDefaultPPMAlreadyExist>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</checkDefaultPPMAlreadyExist>");
        } catch (final Exception e) {
          log4j.error("Exception in PersonalPaymentMethodAjax - checkDefaultPPMAlreadyExist : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      }

      else if (action.equals("checkDefaultPPMAlreadyExistBankDetail")) {
        StringBuffer sb = new StringBuffer();
        try {
          dao = new PersonalPaymentMethodDAO(con);
          Boolean chk = dao.checkDefaultPPMBankDefaultAlreadyExists(
              request.getParameter("inppersonalpaymethodId"), request.getParameter("inpIsDefault"),
              vars.getClient());
          sb.append("<checkDefaultPPMAlreadyExistBankDetail>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</checkDefaultPPMAlreadyExistBankDetail>");
        } catch (final Exception e) {
          log4j.error("Exception in PersonalPaymentMethodAjax - checkDefaultPPMAlreadyExist : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      }

      else if (action.equals("checkPPMAlreadyExist")) {
        StringBuffer sb = new StringBuffer();
        try {
          log4j.debug("Inside checkPPMAlreadyExist");
          dao = new PersonalPaymentMethodDAO(con);
          Boolean chk = dao.checkPersonalPaymentMethodAlreadyExists(
              request.getParameter("inpempId"), request.getParameter("inppersonalpaymethodId"),
              vars.getClient(), request.getParameter("inppaycode"),
              request.getParameter("inppaycurrency"));
          log4j.debug("chk :" + chk);
          sb.append("<checkPPMAlreadyExist>");
          sb.append("<value>" + chk + "</value>");
          sb.append("</checkPPMAlreadyExist>");
        } catch (final Exception e) {
          log4j.error("Exception in PersonalPaymentMethodAjax - checkPPMAlreadyExist : ", e);
        } finally {
          response.setContentType("text/xml");
          response.setCharacterEncoding("UTF-8");
          response.setHeader("Cache-Control", "no-cache");
          response.getWriter().write(sb.toString());
        }
      }

    } catch (final Exception e) {
      log4j.error("Error in PersonalPaymentMethodAjax : ", e);
    } finally {
      OBContext.restorePreviousMode();
      try {
        con.close();
      } catch (final SQLException e) {
        log4j.error("Error in PersonalPaymentMethodAjax : ", e);
      }
    }
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    doPost(request, response);
  }

  public String getServletInfo() {
    return "PersonalPaymentMethodAjax Servlet";
  }

}
