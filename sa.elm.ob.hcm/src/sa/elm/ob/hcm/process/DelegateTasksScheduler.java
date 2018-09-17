package sa.elm.ob.hcm.process;

import java.util.Date;
import java.util.List;

import org.activiti.engine.task.Task;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import sa.elm.ob.hcm.EhcmApprovalDelegation;
import sa.elm.ob.hcm.selfservice.dao.delegate.DelegateTasksDAO;
import sa.elm.ob.hcm.util.ActivitiProcess;

public class DelegateTasksScheduler extends DalBaseProcess {
  /**
   * This Process Class is responsible to delegate employee's tasks when he goes in leave
   * 
   */
  static int counter = 0;

  private ProcessLogger logger;

  @Autowired
  DelegateTasksDAO delegateTasksDAO;

  @Autowired
  ActivitiProcess activiti;

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();

    logger.log("Starting background product transaction Loop " + counter + "\n");

    try {

      OBContext.setAdminMode(true);

      String query = "as e where e.enabled='Y' order by creationDate desc";
      List<EhcmApprovalDelegation> delegations = OBDal.getInstance()
          .createQuery(EhcmApprovalDelegation.class, query).list();

      Date today = new Date();

      if (delegations.size() <= 0)
        logger.log("There are no records = " + delegations.size() + "\n");

      for (EhcmApprovalDelegation delegate : delegations) {
        if (today.compareTo(delegate.getFromDate()) >= 0) {
          if (today.compareTo(delegate.getToDate()) > 0)
            delegateTasksDAO.delete(delegate);
          else {

            List<Task> tasks = activiti.getTaskAssignedToUser(delegate.getFromEmployee().getId());
            for (Task task : tasks) {
              activiti.assignTaskToUser(delegate.getToEmployee().getId(), task.getId());
            }

          }
        }
      }

    } catch (Exception e) {
      // catch any possible exception and throw it as a Quartz
      // JobExecutionException
      throw new JobExecutionException(e.getMessage(), e);
    }
  }
}
