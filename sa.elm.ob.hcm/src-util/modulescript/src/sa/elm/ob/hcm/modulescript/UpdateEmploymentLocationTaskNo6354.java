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
 * @author Priyanka Ranjan 06-04-2018
 *
 */

// Update Location with address1 in Employment Information in Employee detail

public class UpdateEmploymentLocationTaskNo6354 extends ModuleScript {
  public void execute() {  
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null;
    try {
      st = cp
          .getPreparedStatement(" update c_location set address1='Address1' where address1 is null ");
      st.executeUpdate();
      st = cp
          .getPreparedStatement(" update escm_location set location_name = (select loc.address1 from escm_location locat join " + 
              " c_location loc on locat.c_location_id = loc.c_location_id where locat.escm_location_id = escm_location.escm_location_id) where location_name is null ");
      st.executeUpdate();
      st = cp
          .getPreparedStatement(" update ehcm_employment_info set location=( select loc.address1  from ehcm_employment_info info " + 
              " join ehcm_position pos on pos.ehcm_position_id = info.ehcm_position_id " + 
              " join ad_org org on org.ad_org_id = pos.department_id " + 
              " join escm_location locat on locat.escm_location_id= org.EM_Ehcm_Escm_Loc " + 
              " join c_location loc on loc.c_location_id = locat.c_location_id  " + 
              " where info.ehcm_employment_info_id=ehcm_employment_info.ehcm_employment_info_id) " + 
              " where (location='' or location is null) ");
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
        new OpenbravoVersion(0,0,6));
  }
}
