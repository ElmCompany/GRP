OB.EHCMEmployment = {};
OB.EHCMEmployment.OnChangeFunctions = {};
 
OB.EHCMEmployment.OnChangeFunctions.EmploymentChange = function(item, view, form, grid) {

 if(item && item.columnName == 'Ehcm_Emp_Perinfo_ID' && item.getValue()!=null) {
	 //setting null value to  employmentGroup field
	 form.setItemValue('ehcmEmploymentGroup',null); 
	 
} else if(item && item.columnName == 'Ehcm_Employment_Group_ID' && item.getValue()!=null) {
	 //setting null value to employee field
	form.setItemValue('employee',null); 
 }
 
}
//registering the onChange function
OB.OnChangeRegistry.register('142974C9C7934A9A92EDBAB964987026', 'ehcmEmploymentGroup',
		OB.EHCMEmployment.OnChangeFunctions.EmploymentChange, 'OBEHCM_ehcmEmploymentGroup');

OB.OnChangeRegistry.register('142974C9C7934A9A92EDBAB964987026', 'employee',
		OB.EHCMEmployment.OnChangeFunctions.EmploymentChange, 'OBEHCM_employee');
