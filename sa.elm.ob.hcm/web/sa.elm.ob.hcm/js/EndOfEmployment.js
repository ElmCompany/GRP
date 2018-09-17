/**
 * 
 * @author Anup singh on 04-07-2018
 */

(function() {
	var view;
	var recordId;
	var decisionstatus;
	var buttonProps = {
			action : function() {
				view = this.view;
				recordId = view.viewGrid.getSelectedRecord().id;
				decisionstatus=view.viewGrid.getSelectedRecord().decisionStatus;
				console.log("r:"+recordId);
				showVariables();
			},
			buttonType : 'ehcm_endofemployment_printicon',
			prompt : OB.I18N.getLabel('Ehcm_PrintReport_Icon'),  
			updateState : function() {
				var view = this.view, 
				form = view.viewForm,  
				grid = view.viewGrid,
				selectedRecords = grid.getSelectedRecords();
//				disabling the print icon while creating a new record and editing a record
				if(view.isShowingForm && form.isNew) {
					this.setDisabled(true);
				} else if (view.isEditingGrid && grid.getEditForm().isNew) {
					this.setDisabled(true);
				}
				
				else {
						this.setDisabled(selectedRecords.length === 0);
						}
//				disabling the print icon if the decision status is under processing
				if(selectedRecords.length>0){
			   var decisionStatus=grid.getSelectedRecord().decisionStatus; 
				if (decisionStatus=="UP"){
					this.setDisabled(true);
				}else{
					this.setDisabled(false);
				}
				}
				else{
					this.setDisabled(true);
				}
				
				
				if(selectedRecords.length > 1){
					this.setDisabled(true);
					}

			}//22C17CEBAD934BFD8093BE9ADFE2CA65
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,'22C17CEBAD934BFD8093BE9ADFE2CA65');
	function showVariables() {
		console.log("show variables:"+recordId);
		OB.Layout.ClassicOBCompatibility.Popup.open('EHCM_EMP_Termination', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_process.EndofEmployment/EndOfEmploymentPrint")
				+ "?Command=DEFAULT&inpRecordId=" + recordId , '', null, false, false, true).close();
	}
}());