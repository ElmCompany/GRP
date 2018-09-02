package sa.elm.ob.hcm.ad_process.AbsenceAccrual;

import java.sql.Connection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;

import sa.elm.ob.hcm.EHCMAbsenceAccrual;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMDeflookupsTypeLn;
import sa.elm.ob.utility.util.Utility;

public class AbsenceAccrual implements Process {
  private static final Logger log = Logger.getLogger(AbsenceAccrual.class);
  private final OBError obError = new OBError();

  DateFormat dateFormat = Utility.dateFormat;
  DateFormat YearFormat = Utility.YearFormat;

  @Override
  public void execute(ProcessBundle bundle) throws Exception {
    // TODO Auto-generated method stub
    log.debug("Issue the Absence Decision");
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);
    final String absenceaccrualId = (String) bundle.getParams().get("Ehcm_Absence_Accrual_ID")
        .toString();
    EHCMAbsenceAccrual absenceaccrual = OBDal.getInstance().get(EHCMAbsenceAccrual.class,
        absenceaccrualId);
    log.debug("absenceaccrualId:" + absenceaccrualId);

    Connection con = OBDal.getInstance().getConnection();

    int count = 0;

    String calculationDate = null;

    JSONObject availAvailableDaysRes = null;

    AbsenceAccrualDAO absenceAccrualDAOImpl = new AbsenceAccrualDAOImpl();

    List<EHCMAbsenceType> absenceTypeList = new ArrayList<EHCMAbsenceType>();
    List<EHCMDeflookupsTypeLn> absenceSubTypeList = null;
    try {
      OBContext.setAdminMode(true);

      absenceAccrualDAOImpl.deletePrevAbsenceAccrual(absenceaccrualId);

      if (absenceaccrual.getCalculationDate() != null) {
        calculationDate = YearFormat.format(absenceaccrual.getCalculationDate());
      }

      absenceTypeList = absenceAccrualDAOImpl.getAccrualList(absenceaccrual);
      log.debug("absenceTypeList:" + absenceTypeList.size());
      if (absenceTypeList.size() > 0) {
        for (EHCMAbsenceType abstyp : absenceTypeList) {
          if (abstyp.getGender() != null
              && !abstyp.getGender().equals(absenceaccrual.getEhcmEmpPerinfo().getGender())) {
            continue;
          }
          if (abstyp.isSubtype()) {
            absenceSubTypeList = absenceAccrualDAOImpl.getAbsenceSubTypeListFromRefLookup(abstyp);
            for (EHCMDeflookupsTypeLn subType : absenceSubTypeList) {
              availAvailableDaysRes = absenceAccrualDAOImpl.getAvailableAndAvaileddays(con,
                  absenceaccrual, abstyp, calculationDate, null, true, subType.getId());

              count = absenceAccrualDAOImpl.calStartDateEndDateAndInsertAbsAccuralDetails(
                  availAvailableDaysRes, calculationDate, abstyp, absenceaccrual, subType);
            }
          } else {
            availAvailableDaysRes = absenceAccrualDAOImpl.getAvailableAndAvaileddays(con,
                absenceaccrual, abstyp, calculationDate, null, true, null);
            count = absenceAccrualDAOImpl.calStartDateEndDateAndInsertAbsAccuralDetails(
                availAvailableDaysRes, calculationDate, abstyp, absenceaccrual, null);

            log.debug("count" + count);
          }
        }
      }
      if (count > 0) {
        obError.setType("Success");
        obError.setTitle("Success");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_AbsenceAccural_ComSuccess"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
      } else {
        obError.setType("Error");
        obError.setTitle("Error");
        obError.setMessage(OBMessageUtils.messageBD("EHCM_AbsAccuralNotComSuccess"));
        bundle.setResult(obError);
        OBDal.getInstance().flush();
      }

    } catch (Exception e) {
      bundle.setResult(obError);
      log.error("exception :", e);
      OBDal.getInstance().rollbackAndClose();
      final OBError error = OBMessageUtils.translateError(bundle.getConnection(), vars,
          vars.getLanguage(), OBMessageUtils.messageBD("HB_INTERNAL_ERROR"));
      bundle.setResult(error);
    } finally {
      OBContext.restorePreviousMode();
    }
  }
}