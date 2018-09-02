OB.EHCMNotCosted = OB.EHCMNotCosted || {};
OB.EHCMNotCosted.OnChangeFunctions = OB.EHCMNotCosted.OnChangeFunctions || {};

OB.EHCMNotCosted.OnChangeFunctions.elementEligibilityCriteria_notcosted = function(item, view, form, grid) {
	//set empty values.
	form.setItemValue('accountElement', null);
};
