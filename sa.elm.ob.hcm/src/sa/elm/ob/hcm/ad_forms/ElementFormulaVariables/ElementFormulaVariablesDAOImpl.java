package sa.elm.ob.hcm.ad_forms.ElementFormulaVariables;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;

/**
 * This process class used for ElementFormulaVariablesDAO Implementation
 */
public class ElementFormulaVariablesDAOImpl implements ElementFormulaVariablesDAO {
  private static Logger log4j = Logger.getLogger(ElementFormulaVariablesDAOImpl.class);

  public List<ElementFormulaVariablesVO> getElementFormulaVariables(VariablesSecureApp vars) {
    String sql = "";
    String elementFormulaRefID = "9F89D0D15A8447DB9C43B28D06649204";
    List<ElementFormulaVariablesVO> ls = new ArrayList<ElementFormulaVariablesVO>();
    ElementFormulaVariablesVO elementFormulaVariablesVO = null;
    try {

      if (vars.getLanguage().equals("ar_SA")) {
        sql = "select coalesce(tr.name,list.name) as name ,list.value ,coalesce(tr.description,list.description)  ";
      } else {
        sql = "select list.name ,list.value,list.description ";
      }
      sql = sql
          + " from ad_ref_list list left join ad_ref_list_trl tr on list.ad_ref_list_id = tr.ad_ref_list_id where ad_reference_id= ? "
          + "  order by list.seqno ";
      SQLQuery Query = OBDal.getInstance().getSession().createSQLQuery(sql);
      Query.setParameter(0, elementFormulaRefID);
      if (Query.list().size() > 0) {
        for (Object o : Query.list()) {
          Object[] row = (Object[]) o;
          elementFormulaVariablesVO = new ElementFormulaVariablesVO();
          elementFormulaVariablesVO.setSearchKey(row[1] != null ? row[1].toString() : "");
          elementFormulaVariablesVO.setName(row[0] != null ? row[0].toString() : "");
          elementFormulaVariablesVO.setDescription(row[2] != null ? row[2].toString() : "");
          ls.add(elementFormulaVariablesVO);
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in getElementFormulaVariables", e);
      return ls;
    } finally {

    }
    return ls;
  }

}
