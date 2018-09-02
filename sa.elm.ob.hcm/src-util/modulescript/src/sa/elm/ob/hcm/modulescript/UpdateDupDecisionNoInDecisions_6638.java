package sa.elm.ob.hcm.modulescript;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;
import org.openbravo.modulescript.ModuleScriptExecutionLimits;
import org.openbravo.modulescript.OpenbravoVersion;

public class UpdateDupDecisionNoInDecisions_6638 extends ModuleScript {
  PreparedStatement ps=null;
  ResultSet rs = null, rs1 = null;
  String decisionNo=null;
  String id=null;
  int i=0;
  public void execute() {
    try {
    
      ConnectionProvider cp = getConnectionProvider();
       ps = cp
          .getPreparedStatement("select max(decision_no) as decision_no from Ehcm_absence_attendance group by decision_no having count(*) > 1");
      
     rs = ps.executeQuery();
    
       while(rs.next())
       {             decisionNo=UtilSql.getValue(rs, "decision_no");
         
         ps = cp.getPreparedStatement("select Ehcm_absence_attendance_id from  Ehcm_absence_attendance where decision_no=?");
         ps.setString(1, decisionNo);
         rs1 = ps.executeQuery();
         i=0;
         while(rs1.next())
        {
          id=UtilSql.getValue(rs1, "Ehcm_absence_attendance_id");
          ps = cp
              .getPreparedStatement("update Ehcm_absence_attendance set decision_no=decision_no||'_'||" +i+" where Ehcm_absence_attendance_id=?");
          ps.setString(1, id);
          ps.executeUpdate();
          i++;
       }

       }
       
     
    
       ps = cp
           .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_delegation group by decision_no having count(*) > 1");
       
      rs = ps.executeQuery();
     
      while(rs.next())
      {             decisionNo=UtilSql.getValue(rs, "decision_no");
        ps = cp.getPreparedStatement("select ehcm_emp_delegation_id from  ehcm_emp_delegation where decision_no=?");
        ps.setString(1, decisionNo);
        rs1 = ps.executeQuery();
        i=0;
       while(rs1.next())
       {
         id=UtilSql.getValue(rs1, "ehcm_emp_delegation_id");
         ps = cp
             .getPreparedStatement("update ehcm_emp_delegation set decision_no=decision_no||'_'||"+i+" where ehcm_emp_delegation_id=?");
         ps.setString(1, id);
         ps.executeUpdate();
         i++;
      }
      }

      
        ps = cp
            .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_promotion group by decision_no having count(*) > 1");
        
       rs = ps.executeQuery();
       while(rs.next())
       {             decisionNo=UtilSql.getValue(rs, "decision_no");
         ps = cp.getPreparedStatement("select ehcm_emp_promotion_id from  ehcm_emp_promotion where decision_no=?");
         ps.setString(1, decisionNo);
         rs1 = ps.executeQuery();
         i=0;
        while(rs1.next())
        {
          id=UtilSql.getValue(rs1, "ehcm_emp_promotion_id");
          ps = cp
              .getPreparedStatement("update ehcm_emp_promotion set decision_no=decision_no||'_'||"+i+" where ehcm_emp_promotion_id=?");
          ps.setString(1, id);
          ps.executeUpdate();
          i++;
       }
       }
         
         ps = cp
             .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_transfer group by decision_no having count(*) > 1");
         
        rs = ps.executeQuery();
        while(rs.next())
        {             decisionNo=UtilSql.getValue(rs, "decision_no");
          ps = cp.getPreparedStatement("select ehcm_emp_transfer_id from  ehcm_emp_transfer where decision_no=?");
          ps.setString(1, decisionNo);
          rs1 = ps.executeQuery();
          i=0;
         while(rs1.next())
         {
           id=UtilSql.getValue(rs1, "ehcm_emp_transfer_id");
           ps = cp
               .getPreparedStatement("update ehcm_emp_transfer set decision_no=decision_no||'_'||"+i+" where ehcm_emp_transfer_id=?");
           ps.setString(1, id);
           ps.executeUpdate();
           i++;
        }
        }
        
        
          ps = cp
              .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_scholarship group by decision_no having count(*) > 1");
          
         rs = ps.executeQuery();
         while(rs.next())
         {             decisionNo=UtilSql.getValue(rs, "decision_no");
           ps = cp.getPreparedStatement("select ehcm_emp_scholarship_id from  ehcm_emp_scholarship where decision_no=?");
           ps.setString(1, decisionNo);
           rs1 = ps.executeQuery();
           i=0;
          while(rs1.next())
          {
            id=UtilSql.getValue(rs1, "ehcm_emp_scholarship_id");
            ps = cp
                .getPreparedStatement("update ehcm_emp_scholarship set decision_no=decision_no||'_'||"+i+" where ehcm_emp_scholarship_id=?");
            ps.setString(1, id);
            ps.executeUpdate();
            i++;
         }
         }
          
         
         
           ps = cp
               .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_businessmission group by decision_no having count(*) > 1");
           
          rs = ps.executeQuery();
          while(rs.next())
          {             decisionNo=UtilSql.getValue(rs, "decision_no");
            ps = cp.getPreparedStatement("select ehcm_emp_businessmission_id from  ehcm_emp_businessmission where decision_no=?");
            ps.setString(1, decisionNo);
            rs1 = ps.executeQuery();
            i=0;
           while(rs1.next())
           {
             id=UtilSql.getValue(rs1, "ehcm_emp_businessmission_id");
             ps = cp
                 .getPreparedStatement("update ehcm_emp_businessmission set decision_no=decision_no||'_'||"+i+" where ehcm_emp_businessmission_id=?");
             ps.setString(1, id);
             ps.executeUpdate();
             i++;
          }
          }

          
            ps = cp
                .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_secondment group by decision_no having count(*) > 1");
            
           rs = ps.executeQuery();
           while(rs.next())
           {             decisionNo=UtilSql.getValue(rs, "decision_no");
             ps = cp.getPreparedStatement("select ehcm_emp_secondment_id from  ehcm_emp_secondment where decision_no=?");
             ps.setString(1, decisionNo);
             rs1 = ps.executeQuery();
             i=0;
            while(rs1.next())
            {
              id=UtilSql.getValue(rs1, "ehcm_emp_secondment_id");
              ps = cp
                  .getPreparedStatement("update ehcm_emp_secondment set decision_no=decision_no||'_'||"+i+" where ehcm_emp_secondment_id=?");
              ps.setString(1, id);
              ps.executeUpdate();
              i++;
           }
           }
             
           
           
             ps = cp
                 .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_suspension group by decision_no having count(*) > 1");
             
            rs = ps.executeQuery();
            while(rs.next())
            {             decisionNo=UtilSql.getValue(rs, "decision_no");
              ps = cp.getPreparedStatement("select ehcm_emp_suspension_id from  ehcm_emp_suspension where decision_no=?");
              ps.setString(1, decisionNo);
              rs1 = ps.executeQuery();
              i=0;
             while(rs1.next())
             {
               id=UtilSql.getValue(rs1, "ehcm_emp_suspension_id");
               ps = cp
                   .getPreparedStatement("update ehcm_emp_suspension set decision_no=decision_no||'_'||"+i+" where ehcm_emp_suspension_id=?");
               ps.setString(1, id);
               ps.executeUpdate();
               i++;
            }
            }

            
            
              ps = cp
                  .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_termination group by decision_no having count(*) > 1");
              
             rs = ps.executeQuery();
             while(rs.next())
             {             decisionNo=UtilSql.getValue(rs, "decision_no");
               ps = cp.getPreparedStatement("select ehcm_emp_termination_id from  ehcm_emp_termination where decision_no=?");
               ps.setString(1, decisionNo);
               rs1 = ps.executeQuery();
               i=0;
              while(rs1.next())
              {
                id=UtilSql.getValue(rs1, "ehcm_emp_termination_id");
                ps = cp
                    .getPreparedStatement("update ehcm_emp_termination set decision_no=decision_no||'_'||"+i+" where ehcm_emp_termination_id=?");
                ps.setString(1, id);
                ps.executeUpdate();
                i++;
             }
             }

             
             
               ps = cp
                   .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_transfer_self group by decision_no having count(*) > 1");
               
              rs = ps.executeQuery();
              while(rs.next())
              {             decisionNo=UtilSql.getValue(rs, "decision_no");
                ps = cp.getPreparedStatement("select ehcm_emp_transfer_self_id from  ehcm_emp_transfer_self where decision_no=?");
                ps.setString(1, decisionNo);
                rs1 = ps.executeQuery();
                i=0;
               while(rs1.next())
               {
                 id=UtilSql.getValue(rs1, "ehcm_emp_transfer_self_id");
                 ps = cp
                     .getPreparedStatement("update ehcm_emp_transfer_self set decision_no=decision_no||'_'||"+i+" where ehcm_emp_transfer_self_id=?");
                 ps.setString(1, id);
                 ps.executeUpdate();
                 i++;
              }
              }

              
              
                ps = cp
                    .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_extrastep group by decision_no having count(*) > 1");
                
               rs = ps.executeQuery();
               while(rs.next())
               {             decisionNo=UtilSql.getValue(rs, "decision_no");
                 ps = cp.getPreparedStatement("select ehcm_emp_extrastep_id from  ehcm_emp_extrastep where decision_no=?");
                 ps.setString(1, decisionNo);
                 rs1 = ps.executeQuery();
                 i=0;
                while(rs1.next())
                {
                  id=UtilSql.getValue(rs1, "ehcm_emp_extrastep_id");
                  ps = cp
                      .getPreparedStatement("update ehcm_emp_extrastep set decision_no=decision_no||'_'||"+i+" where ehcm_emp_extrastep_id=?");
                  ps.setString(1, id);
                  ps.executeUpdate();
                  i++;
               }}

                
                
                 ps = cp
                     .getPreparedStatement("select max(decision_no) as decision_no from ehcm_extend_service group by decision_no having count(*) > 1");
                 
                rs = ps.executeQuery();
                while(rs.next())
                {             decisionNo=UtilSql.getValue(rs, "decision_no");
                  ps = cp.getPreparedStatement("select ehcm_extend_service_id from  ehcm_extend_service where decision_no=?");
                  ps.setString(1, decisionNo);
                  rs1 = ps.executeQuery();
                  i=0;
                 while(rs1.next())
                 {
                   id=UtilSql.getValue(rs1, "ehcm_extend_service_id");
                   ps = cp
                       .getPreparedStatement("update ehcm_extend_service set decision_no=decision_no||'_'||"+i+" where ehcm_extend_service_id=?");
                   ps.setString(1, id);
                   ps.executeUpdate();
                   i++;
                }}

                  
                 
                  ps = cp
                      .getPreparedStatement("select max(decision_no) as decision_no from ehcm_discipline_action group by decision_no having count(*) > 1");
                  
                 rs = ps.executeQuery();
                 while(rs.next())
                 {             decisionNo=UtilSql.getValue(rs, "decision_no");
                   ps = cp.getPreparedStatement("select ehcm_discipline_action_id from  ehcm_discipline_action where decision_no=?");
                   ps.setString(1, decisionNo);
                   rs1 = ps.executeQuery();
                   i=0;
                  while(rs1.next())
                  {
                    id=UtilSql.getValue(rs1, "ehcm_discipline_action_id");
                    ps = cp
                        .getPreparedStatement("update ehcm_discipline_action set decision_no=decision_no||'_'||"+i+" where ehcm_discipline_action_id=?");
                    ps.setString(1, id);
                    ps.executeUpdate();
                    i++;
                 }}
                  
                  

                   ps = cp
                       .getPreparedStatement("select max(decision_no) as decision_no from ehcm_emp_overtime group by decision_no having count(*) > 1");
                   
                  rs = ps.executeQuery();
                  while(rs.next())
                  {             decisionNo=UtilSql.getValue(rs, "decision_no");
                    ps = cp.getPreparedStatement("select ehcm_emp_overtime_id from  ehcm_emp_overtime where decision_no=?");
                    ps.setString(1, decisionNo);
                    rs1 = ps.executeQuery();
                    i=0;
                   while(rs1.next())
                   {
                     id=UtilSql.getValue(rs1, "ehcm_emp_overtime_id");
                     ps = cp
                         .getPreparedStatement("update ehcm_emp_overtime set decision_no=decision_no||'_'||"+i+" where ehcm_emp_overtime_id=?");
                     ps.setString(1, id);
                     ps.executeUpdate();
                     i++;
                  }
                  }

                   
                   
                    ps = cp
                        .getPreparedStatement("select max(decision_no) as decision_no from ehcm_benefit_allowance group by decision_no having count(*) > 1");
                    
                   rs = ps.executeQuery();
                   while(rs.next())
                   {             decisionNo=UtilSql.getValue(rs, "decision_no");
                     ps = cp.getPreparedStatement("select ehcm_benefit_allowance_id from  ehcm_benefit_allowance where decision_no=?");
                     ps.setString(1, decisionNo);
                     rs1 = ps.executeQuery();
                     i=0;
                    while(rs1.next())
                    {
                      id=UtilSql.getValue(rs1, "ehcm_benefit_allowance_id");
                      ps = cp
                          .getPreparedStatement("update ehcm_benefit_allowance set decision_no=decision_no||'_'||"+i+" where ehcm_benefit_allowance_id=?");
                      ps.setString(1, id);
                      ps.executeUpdate();
                      i++;
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
        new OpenbravoVersion(0,0,20));
}
}