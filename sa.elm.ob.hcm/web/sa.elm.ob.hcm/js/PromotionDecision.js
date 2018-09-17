/**
 * 
 * @author Divyaprakash JS on 04-07-2018
 */

(function() {
	var view;
	var recordId;
	var buttonProps = {
			action : function() {
				
				view = this.view;
				recordId = view.viewGrid.getSelectedRecord().id;
				console.log("r:"+recordId);
				showVariables();
			},
			buttonType : 'ehcm_promotiondecision_printicon',
			prompt : OB.I18N.getLabel('Ehcm_PrintReport_Icon'),
			updateState : function() {
				var view = this.view,
				form = view.viewForm,  
				grid = view.viewGrid,
				selectedRecords = grid.getSelectedRecords();
				//disabling the print icon while creating a new record and editing a record
				if(view.isShowingForm && form.isNew) {
					this.setDisabled(true);
				} else if (view.isEditingGrid && grid.getEditForm().isNew) {
					this.setDisabled(true);
				}
				else {
					this.setDisabled(selectedRecords.length === 0);
				}
				//Enable the print icon if the decision status is Issued
				if(selectedRecords.length>0){
				var decisionStatus=grid.getSelectedRecord().decisionStatus;
				if (decisionStatus=="I"){
					this.setDisabled(false);
				}else{
					this.setDisabled(true);
				}
				}
				else{
					this.setDisabled(true);
				}
			}
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,'19DA64CBD4A744438D207D4F37235ECC');
	
	function showVariables() {
		console.log("show variables:"+recordId);
		OB.Layout.ClassicOBCompatibility.Popup.open('EHCM_Emp_Promotion', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_process.PromotionDecision/PromotionDecisionPrint")
				+ "?Command=DEFAULT&action=PromotionDecisionVariable"+"&inpRecordId=" + recordId , '', null, false, false, true).close();
	}
}());
