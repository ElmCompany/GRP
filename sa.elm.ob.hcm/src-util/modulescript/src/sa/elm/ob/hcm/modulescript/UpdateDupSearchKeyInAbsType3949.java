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
  
package sa.elm.ob.hcm.modulescript;

import java.sql.PreparedStatement;
import org.openbravo.database.ConnectionProvider;  
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;
/**
 * 
 * @author divya-02-06-2018
 *
 */

public class UpdateDupSearchKeyInAbsType3949 extends ModuleScript {
  public void execute() {  
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null;
    try {
      
      // update duplicate searchkey in AbsenceType
      st = cp
          .getPreparedStatement(" update ehcm_absence_type t1 set value= value || floor(random() * 10 + 1)::int " + 
              " where exists (select 1 from ehcm_absence_type t2 " + 
              " where t2.value=t1.value and t2.created < t1.created )" );
      st.executeUpdate();
    
     
      
    } catch (Exception e) {   
      handleError(e);     
    }finally{ 
      try{   
      if(st!=null){  
        st.close();  
      }
      }catch (Exception e) {
        handleError(e);
      }
  }
}
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("61076388910A43ECA7DA588EC716E9D9",  null, 
        new OpenbravoVersion(0,0,15));
  }
}
