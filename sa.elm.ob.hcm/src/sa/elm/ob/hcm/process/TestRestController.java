package sa.elm.ob.hcm.process;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

	@RequestMapping(value = "/openerp/hr/employees", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List <Employee>> getAllEmployees(ModelMap model) {
		
		List <Employee> employees = new ArrayList<Employee> ();
		
		
		return new ResponseEntity<List<Employee>>(employees,HttpStatus.OK);

		

	}
}
