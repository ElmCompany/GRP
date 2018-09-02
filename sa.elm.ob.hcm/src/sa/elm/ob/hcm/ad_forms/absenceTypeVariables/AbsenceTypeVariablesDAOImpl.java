package sa.elm.ob.hcm.ad_forms.absenceTypeVariables;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;

/**
 * This process class used for Absence Type VariablesDAO Implementation
 * 
 * @author divya-17-05-2018
 *
 */
public class AbsenceTypeVariablesDAOImpl {
  private static Logger log4j = Logger.getLogger(AbsenceTypeVariablesDAOImpl.class);

  public List<AbsenceTypeVariablesVO> getAbsenceTypeVariables(VariablesSecureApp vars) {
    String sql = "";
    String AbsenceTypeRefID = "26719DD11D5C40D6B482B58DE913A3C3";
    List<AbsenceTypeVariablesVO> ls = new ArrayList<AbsenceTypeVariablesVO>();
    AbsenceTypeVariablesVO absenceTypeVariablesVO = null;
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
      Query.setParameter(0, AbsenceTypeRefID);
      if (Query.list().size() > 0) {
        for (Object o : Query.list()) {
          Object[] row = (Object[]) o;
          absenceTypeVariablesVO = new AbsenceTypeVariablesVO();
          absenceTypeVariablesVO.setSearchKey(row[1] != null ? row[1].toString() : "");
          absenceTypeVariablesVO.setName(row[0] != null ? row[0].toString() : "");
          absenceTypeVariablesVO.setDescription(row[2] != null ? row[2].toString() : "");
          ls.add(absenceTypeVariablesVO);
        }
      }

    } catch (final Exception e) {
      log4j.error("Exception in getAbsenceTypeVariables", e);
      return ls;
    } finally {

    }
    return ls;
  }

}
