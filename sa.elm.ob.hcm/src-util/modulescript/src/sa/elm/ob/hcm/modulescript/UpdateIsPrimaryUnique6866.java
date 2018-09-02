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
 * Contributor(s):  Gokul 10/08/18.
 ************************************************************************
 */
  
package sa.elm.ob.hcm.modulescript;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openbravo.database.ConnectionProvider;  
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;


public class UpdateIsPrimaryUnique6866 extends ModuleScript {
  public void execute() {  
    ResultSet rs=null;
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null;
    PreparedStatement st1 = null;
    try {
      st = cp
          .getPreparedStatement("select count(ehcm_element_group_id),ad_client_id from ehcm_element_group  where isprimary='Y' group by ad_client_id having count(ehcm_element_group_id)>1");
      rs=st.executeQuery();
      while(rs.next()) {
        st1=cp.getPreparedStatement("UPDATE ehcm_element_group set isprimary='N' where ehcm_element_group_id in ( select ehcm_element_group_id from ehcm_element_group where ad_client_id=? limit ?-1)");
            st1.setString(1, rs.getString("ad_client_id"));
            st1.setInt(2, rs.getInt("count"));
        st1.executeUpdate();
      }
    } catch (Exception e) {   
      handleError(e);     
    }finally{ 
      try{   
      if(st!=null){  
        st.close();  
      }
      if(st1!=null) {
        st1.close();
      }
      if(rs!=null) {
        rs.close();
      }
      }catch (Exception e) {
        handleError(e);
      }
  }
}
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("61076388910A43ECA7DA588EC716E9D9",  null, 
        new OpenbravoVersion(0,0,30));
  }
}
