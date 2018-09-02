OB.EHCM = {};
OB.EHCM.OnChangeFunctions = {};

OB.EHCM.OnChangeFunctions.Addressstyle_values = function(item, view, form, grid) {
	//set empty values.
	form.setItemValue('addressLine1', '');
	form.setItemValue('addressLine2', '');
	form.setItemValue('district', '');
	form.setItemValue('postBox', '');
	form.setItemValue('postalCode', '');
	form.setItemValue('street', '');
	form.setItemValue('country', '');
	form.setItemValue('city', null);
}

