package sa.elm.ob.hcm.modulescript;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;
/**
 * 
 * @author Gowtham
 *
 */
//This script is used to update payment type column value as elementgroup (foreign key).
public class EmpSecondmentPaytype extends ModuleScript {
  PreparedStatement ps=null;
  ResultSet rs = null, rs1=null,rs2=null ;
  public void execute() {
    try {
    
      ConnectionProvider cp = getConnectionProvider();
       ps = cp
          .getPreparedStatement("select distinct ad_client_id from ehcm_emp_secondment");
      
       rs = ps.executeQuery();
    
       while(rs.next())
       {
         //chk element group exst for client.
         ps = cp
             .getPreparedStatement("select ehcm_element_group_id from ehcm_element_group where ad_client_id=? limit 1");
         ps.setString(1, rs.getString("ad_client_id"));
         rs1 = ps.executeQuery();
         if(rs1.next()) {
           //if exist update it.
           ps = cp
              .getPreparedStatement("update ehcm_emp_secondment set payment_type =? where ad_client_id=?");
           ps.setString(1, rs1.getString("ehcm_element_group_id"));
           ps.setString(2, rs.getString("ad_client_id"));
           ps.executeUpdate();
         }else {
           //if not exst create new.
           //String seq = SequenceIdData.getUUID();
           
           ps = cp
               .getPreparedStatement("select get_uuid()");
           rs2 = ps.executeQuery();
           rs2.next();
           
           ps = cp
               .getPreparedStatement("insert into ehcm_element_group (ehcm_element_group_id,ad_client_id,ad_org_id,isactive,createdby,updatedby,element_group_code,element_group_name,start_date) " + 
                   "values(?,?,'0','Y','100','100','SAMPLE','Sample',now())");
           ps.setString(1, rs2.getString("get_uuid"));
           ps.setString(2, rs.getString("ad_client_id"));
           ps.executeUpdate();
           
           //update elemtnt group id
           ps = cp
               .getPreparedStatement("update ehcm_emp_secondment set payment_type =? where ad_client_id=? ");
            ps.setString(1, rs2.getString("get_uuid"));
            ps.setString(2, rs.getString("ad_client_id"));
            ps.executeUpdate();
         }
       }
       
    } catch (Exception e) {
      handleError(e);
    }
    finally
    {
      try
      {
        ps.close();
      }
      catch (Exception e) {
        handleError(e);
    }
    }
  }
  @Override
  protected ModuleScriptExecutionLimits getModuleScriptExecutionLimits() {
    return new ModuleScriptExecutionLimits("61076388910A43ECA7DA588EC716E9D9", null, 
        new OpenbravoVersion(0,0,34));
}
}