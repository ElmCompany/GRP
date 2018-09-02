/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
/**
 * divya - 09-08-2018
 */
package sa.elm.ob.hcm.modulescript;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class InsertPosEmpHistory6797 extends ModuleScript {
  public void execute() {
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = cp.getPreparedStatement(" delete from ehcm_posemp_history ");
       st.executeUpdate();
      
      st = cp.getPreparedStatement(" select ehcm_insertposemphist() ");
      rs = st.executeQuery();
    } catch (Exception e) {
      handleError(e);
    } finally {
      try {
        if (st != null) {
          st.close();
          rs.close();
        }
      } catch (Exception e) {
        handleError(e);
      }
    }
  }

  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("61076388910A43ECA7DA588EC716E9D9", null,
        new OpenbravoVersion(0, 0, 31));
  }
}
