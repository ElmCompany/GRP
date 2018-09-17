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

import sa.elm.ob.hcm.EhcmChangeBank;
import sa.elm.ob.hcm.services.payroll.PayrollInformationService;

@Component
public class ChangeBankDetailsListener implements TaskListener, ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @SuppressWarnings("static-access")
  @Override
  public void setApplicationContext(ApplicationContext context) throws BeansException {
    this.applicationContext = context;

  }

  @Override
  public void notify(DelegateTask delegateTask) {

    PayrollInformationService payrollInformationService = applicationContext
        .getBean(PayrollInformationService.class);

    TaskEntity taskEntity = (TaskEntity) delegateTask;

    @SuppressWarnings("rawtypes")
    Map variablesMap = taskEntity.getVariables();
    String changeBankId = variablesMap.get(ActivitiConstants.TARGET_IDENTIFIER).toString();

    EhcmChangeBank bank = payrollInformationService.getChangeBankById(changeBankId);

    payrollInformationService.completeChangeBankProcess(bank);

  }
}
