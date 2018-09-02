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
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import org.openbravo.buildvalidation.BuildValidation;
import org.openbravo.database.ConnectionProvider;  



public class DropEmpAddressPrimaryKeyConstraint extends BuildValidation {
  
  @Override
  public List<String> execute() {
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null, dropst=null;
    ResultSet rs= null;
    ArrayList<String> errors = new ArrayList<String>();
    try {
      st = cp
          .getPreparedStatement(  " SELECT  count(CONSTRAINT_NAME) as total FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS WHERE " + 
              "  CONSTRAINT_NAME='ehcm_emp_address_key' AND TABLE_NAME='ehcm_emp_address' ");
      rs=st.executeQuery();
      if(rs.next()) {
        if(rs.getInt("total") > 0) {
          
          dropst = cp
              .getPreparedStatement(  " ALTER TABLE ehcm_emp_address drop CONSTRAINT ehcm_emp_address_key ");
          dropst.executeUpdate();
        }
      }
    } catch (Exception e) {   
      handleError(e);     
    }finally{ 
      try{   
      if(st!=null){  
        st.close();  
      }
      if(dropst!=null){  
        dropst.close();  
      }
      }catch (Exception e) {
        handleError(e);
      }
      return errors;
  }
}
  
}
