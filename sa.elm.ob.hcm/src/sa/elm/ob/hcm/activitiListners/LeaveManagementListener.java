package sa.elm.ob.hcm.activitiListners;

import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.openbravo.activiti.ActivitiConstants;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import sa.elm.ob.hcm.EHCMAbsenceAttendance;
import sa.elm.ob.hcm.EHCMAbsenceType;
import sa.elm.ob.hcm.EHCMAbsenceTypeAccruals;
import sa.elm.ob.hcm.dao.leave.LeaveManagementDAO;

@Component
public class LeaveManagementListener implements TaskListener, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @SuppressWarnings("static-access")
  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.applicationContext = context;

  }

  @Override
  public void notify(DelegateTask delegateTask) {

    LeaveManagementDAO leaveManagementDAO = applicationContext.getBean(LeaveManagementDAO.class);

    TaskEntity taskEntity = (TaskEntity) delegateTask;
    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    boolean approved = (boolean) variablesMap.get("requestApproved");
    if (approved) {
      try {
        String id = variablesMap.get(ActivitiConstants.TARGET_IDENTIFIER).toString();
        String absenceType = variablesMap.get("leaveType").toString();
        EHCMAbsenceAttendance absenceAttendanceOB = leaveManagementDAO.getAbsenceAttendanceById(id);
        EHCMAbsenceType absenceTypeOB = leaveManagementDAO.findAbsenceType(absenceType);
        EHCMAbsenceTypeAccruals absenceAccuralOB = leaveManagementDAO
            .getAccural(absenceAttendanceOB);

        leaveManagementDAO.createLeave(absenceAttendanceOB, absenceTypeOB, absenceAccuralOB);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

  }
}
