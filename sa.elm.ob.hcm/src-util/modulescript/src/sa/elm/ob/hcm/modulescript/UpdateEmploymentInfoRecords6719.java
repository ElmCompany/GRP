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
 * @author Priyanka Ranjan  10-07-2018
 *
 */

public class UpdateEmploymentInfoRecords6719 extends ModuleScript {
  public void execute() {  
    ConnectionProvider cp = getConnectionProvider();
    PreparedStatement st = null;
    try {
      // update Job code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set jobcode =(select ehcm_jobs_id from ehcm_jobs where Value=jobcode and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
       // update Department code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info set deptcode =(select ad_org_id from ad_org where Value=deptcode and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
       // update Section code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set sectioncode =(select ad_org_id from ad_org where Value=sectioncode and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
          // update Secondary position grade from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set secposition_grade =(select ehcm_grade_id from ehcm_grade where Value=secposition_grade and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
          // update secondary Job No. from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set secjobno =(select ehcm_position_id from ehcm_position where JOB_No=secjobno and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
          // update secondary Job code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set secjobcode =(select ehcm_jobs_id from ehcm_jobs where Value=secjobcode and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
      st.executeUpdate();
      
          // update secondary Department code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set sec_dept_code =(select ad_org_id from ad_org where Value=sec_dept_code and ad_client_id=ehcm_employment_info.ad_client_id limit 1)  " );
      st.executeUpdate();
      
    // update secondary section code from string to table reference in employment Info
      st = cp
          .getPreparedStatement(" update ehcm_employment_info  set sec_section_code =(select ad_org_id from ad_org where Value=sec_section_code and ad_client_id=ehcm_employment_info.ad_client_id limit 1) " );
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
        new OpenbravoVersion(0,0,26));
  }
}
