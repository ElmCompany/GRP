package sa.elm.ob.hcm.ad_forms.absenceTypeVariables;

import java.util.List;

import org.openbravo.base.secureApp.VariablesSecureApp;

/**
 * This process class used for Absence Type Variables Implementation
 * 
 * @author divya -17-05-2017
 *
 */
public interface AbsenceTypeVariablesDAO {

  /**
   * get the list of absence rules variables
   * 
   * @param vars
   * @return
   * @throws Exception
   */
  List<AbsenceTypeVariablesVO> getAbsenceTypeVariables(VariablesSecureApp vars) throws Exception;

}
