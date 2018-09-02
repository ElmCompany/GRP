package sa.elm.ob.hcm.selfservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sa.elm.ob.hcm.selfservice.dto.delegate.DelegateTasksDTO;
import sa.elm.ob.hcm.selfservice.exceptions.BusinessException;
import sa.elm.ob.hcm.selfservice.service.delegate.DelegateTasksService;

@RestController
@RequestMapping("openerp/hr/delegate")
public class DelegateTasksController {

  @Autowired
  private DelegateTasksService delegateTasksService;

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<Boolean> create(@RequestBody DelegateTasksDTO delegateTasksDTO) {
    delegateTasksService.save(delegateTasksDTO);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(method = RequestMethod.PUT)
  public ResponseEntity<Boolean> update(@RequestBody DelegateTasksDTO delegateTasksDTO)
      throws BusinessException {
    delegateTasksService.update(delegateTasksDTO);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

  @RequestMapping(value = "/{username}", method = RequestMethod.GET)
  public ResponseEntity<List<DelegateTasksDTO>> getAllByUsername(@PathVariable String username) {
    List<DelegateTasksDTO> allDelegations = delegateTasksService.getAllByUsername(username);
    return new ResponseEntity<List<DelegateTasksDTO>>(allDelegations, HttpStatus.OK);
  }

  @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
  public ResponseEntity<Boolean> delete(@PathVariable String id) throws BusinessException {
    delegateTasksService.delete(id);
    return new ResponseEntity<Boolean>(true, HttpStatus.OK);
  }

}
