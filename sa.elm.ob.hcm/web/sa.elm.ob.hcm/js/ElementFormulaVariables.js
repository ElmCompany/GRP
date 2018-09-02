(function() {
	var buttonProps = {
		action : function() {
			showVariables();
		},
		buttonType : 'ehcm_elementformula_variables',
		prompt : OB.I18N.getLabel('EHCM_ElementFormulaVar_Info'),
		updateState : function() {
		}
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,['AA48836CA09448769F88BD363A410824','0B13C1B25244489384341764AF572A1D']);
	
	function showVariables() {
		OB.Layout.ClassicOBCompatibility.Popup.open('EHCM_ElementFormula_Hdr', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_forms.ElementFormulaVariables/ElementFormulaVariables")
				+ "?Command=DEFAULT&action=showElementFormulaVariables", '', null, false, false, true);
	}
}());
