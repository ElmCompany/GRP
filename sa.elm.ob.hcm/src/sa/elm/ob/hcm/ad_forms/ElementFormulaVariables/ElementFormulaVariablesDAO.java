package sa.elm.ob.hcm.ad_forms.ElementFormulaVariables;

import java.util.List;

import org.openbravo.base.secureApp.VariablesSecureApp;

/**
 * This interface is used for Element Formula Variables
 */
public interface ElementFormulaVariablesDAO {

  /**
   * get the list of Element Formula Variables
   * 
   * @param vars
   * @return
   * @throws Exception
   */
  List<ElementFormulaVariablesVO> getElementFormulaVariables(VariablesSecureApp vars) throws Exception;

}
