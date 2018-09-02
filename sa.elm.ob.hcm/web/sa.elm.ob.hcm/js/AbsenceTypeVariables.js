/**
 * 
 * @author 17-05-2018
 */


(function() {
	var buttonProps = {
		action : function() {
			showVariables();
		},
		buttonType : 'ehcm_abstype_variables',
		prompt : OB.I18N.getLabel('EHCM_AbsTypeVar_Info'),
		updateState : function() {
		}
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,'B9E71506C1AD451C86A94846D212DB60');
	function showVariables() {
		OB.Layout.ClassicOBCompatibility.Popup.open('EHCM_Absence_Type', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_forms.absenceTypeVariables/AbsenceTypeVariables")
				+ "?Command=DEFAULT&action=showAbsenceTypeVariables", '', null, false, false, true);
	}
}());
