OB.EHCM.adClientContract ={};
OB.EHCM.adClientContract.OnMinContractServiceChange = function (item, view, form, grid) {
	var minContract = form.getItem('eHCMMinimumContractService').getValue();
	var numbers = /^[0-9]+(\.[0-9]+)?$/;
 	if (numbers.test(minContract)) {
 		var digits = minContract.split(".");
 		if ( digits.length > 1 ) {
 			minContract = digits[0][0]+'.'+digits[1][0];
 		}
 		else {
 			minContract = digits[0][0];
 		}	
 	}
 	else {
 		minContract = "";
 	}
	form.setItemValue('eHCMMinimumContractService',minContract);
};
OB.EHCM.adClientContract.OnMaxContractServiceChange = function (item, view, form, grid) {
	var maxContract = form.getItem('eHCMMaximumContractService').getValue();
	var numbers = /^[0-9]+(\.[0-9]+)?$/;
 	if (numbers.test(maxContract)) {
 		var digits = maxContract.split(".");
 		if ( digits.length > 1 ) {
 			maxContract = digits[0][0]+'.'+digits[1][0];
 		}
 		else {
 			maxContract = digits[0][0];
 		}	
 	}
 	else {
 		maxContract = "";
 	}
	form.setItemValue('eHCMMaximumContractService',maxContract);
};
