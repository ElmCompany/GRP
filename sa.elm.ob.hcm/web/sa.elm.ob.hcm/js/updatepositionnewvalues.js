OB.EHCM = {};
OB.EHCM.OnChangeFunctions = {};

OB.EHCM.OnChangeFunctions.Updateposition_values = function(item, view, form, grid) {
		
	var callback = function(response, data, request) {
		
		if(data.Value=='UGPO'){
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();			
		}else if(data.Value=='UGREPO'){	
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			form.getItem('nEWJobName').enable();
			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='UGFRPO'){
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='DGPO'){
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='DGREPO'){
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			form.getItem('nEWJobName').enable();
			
			form.getItem('nEWEhcmJobs').disable();			
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='DGFRPO'){
			form.getItem('nEWEhcmGrade').enable();
			form.getItem('nEWJobNo').enable();
			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='TRPO'){
			form.getItem('nEWDepartment').enable();
			form.getItem('nEWDeptname').enable();
			form.getItem('nEWSection').enable();
			form.getItem('nEWSectionname').enable();
			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();			
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();			
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='RCPO'){
			form.getItem('nEWEhcmJobs').enable();
			form.getItem('nEWJobName').enable();
			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='RCTRPO'){
			form.getItem('nEWEhcmJobs').enable();
			form.getItem('nEWJobName').enable();
			form.getItem('nEWDepartment').enable();
			form.getItem('nEWDeptname').enable();
			form.getItem('nEWSection').enable();
			form.getItem('nEWSectionname').enable();
			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();			
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='RCFRPO'){
			form.getItem('nEWEhcmJobs').enable();
			form.getItem('nEWJobName').enable();
			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='FRPO'){			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}else if(data.Value=='HOPO'){			
			form.getItem('nEWEhcmGrade').disable();
			form.getItem('nEWJobNo').disable();
			form.getItem('nEWEhcmJobs').disable();
			form.getItem('nEWJobName').disable();
			form.getItem('nEWDepartment').disable();
			form.getItem('nEWDeptname').disable();
			form.getItem('nEWSection').disable();
			form.getItem('nEWSectionname').disable();
			form.getItem('year').disable();
			form.getItem('budgetDate_dateTextField').disable();
			form.getItem('mOFDecisionNo').disable();
			form.getItem('mOFDecisionDate_dateTextField').disable();
		}
	};
	  
	// do a server side call and on return call the callback
	OB.RemoteCallManager.call('sa.elm.ob.hcm.ad_callouts.UpdatePosNewValue', {value: item.getValue()}, {}, callback);
}
OB.OnChangeRegistry.register('64B17ACFD7D742CABBB0A5AB3816860E', 'Transaction Type',OB.EHCM.OnChangeFunctions.Updateposition_values, 'EHCM_Updateposition_values');
OB.EHCM.OnChangeFunctions.Updateposition_values.sort = 20;

