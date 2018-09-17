/**
 * 
 * @author SOWMIYA on 25-06-2018
 */

(function() {
	var view;
	var recordId;
	var buttonProps = {
			action : function() {
				
				view = this.view;
				recordId = view.viewGrid.getSelectedRecord().id;
				console.log("r:"+recordId);
				downLoadReport();
			},
			buttonType : 'ehcm_overtime_decree_printicon',
			prompt : OB.I18N.getLabel('Ehcm_PrintReport_Icon'),
			updateState : function() {
				var view = this.view,
		        form = view.viewForm,
		        grid = view.viewGrid,
		        selectedRecords = grid.getSelectedRecords();
				this.setDisabled(true);
				
				if (selectedRecords.length > 0 && view.viewGrid.getSelectedRecords()[0].decisionStatus == 'I' && selectedRecords.length < 2){
					this.setDisabled(false);					
				}
			}
	};
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,'E79FBC2CDCF64A0D9ECBA4990E3DEDD0');
	
	function downLoadReport() {
		console.log("show variables:"+recordId);
		OB.Layout.ClassicOBCompatibility.Popup.open('ehcm_emp_overtime', 725, 550, OB.Utilities.applicationUrl("sa.elm.ob.hcm.ad_process.OvertimeDecree/OvertimeDecreePrint")
				+ "?Command=DEFAULT&action=overtimeDecreeVariable"+"&inpRecordId=" + recordId , '', null, false, false, true).close();
	}
}());
