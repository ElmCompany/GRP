/**
 * 
 * @author Rashika VS on 25-06-2018
 */

(function() {
	var view;
	var grid;
	var recordId, documentNo = '';
	var tabId, windowId;
	var buttonProps = {
			action : function() {

				view = this.view;
				grid = view.viewGrid;				
				windowId = view['windowId'];
				tabId = view['tabId'];
				recordId = view.viewGrid.getSelectedRecord().id;  
				if(windowId=='796272C70BFE4201BBDC848C8F487FAA'){
					documentNo=view.viewGrid.getSelectedRecord().decisionNo;	
				}
				showVariables();
			},
			buttonType : 'ehcm_print_pdf',
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
				//disabling the print icon if the decision status is under processing
				var decisionStatus=grid.getSelectedRecord().decisionStatus;
				if (decisionStatus=="UP"){
					this.setDisabled(true);
				}else{
					this.setDisabled(false);
				}				

			}
	};

	function showVariables() {
		var action = "", wi;
		if(windowId=='796272C70BFE4201BBDC848C8F487FAA'){ //Business Mission
			wi = OB.Layout.ClassicOBCompatibility.Popup.open('EHCM_Emp_BusinessMission', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_reports.printreport/PrintReport")
					+ "?Command=DEFAULT"+"&pageType=WAD&inpRecordId=" + recordId + "&inpTabId=" + tabId + "&inpWindowID=" + windowId + "&documentNo=" + documentNo , '', null, false, false, true);
		} else if(windowId=='C8154257AADE418D8387C2319B85D762'){ //Absence Decision
			wi = OB.Layout.ClassicOBCompatibility.Popup.open('AbsenceDecision', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_reports.printreport/PrintReport")
					+ "?Command=DEFAULT"+"&pageType=WAD&inpRecordId=" + recordId + "&inpTabId=" + tabId + "&inpWindowID=" + windowId + "&documentNo="+documentNo , '', null, false, false, true);
		} else if(windowId=='DCC32BEEF53841FF9A5B1F1585156930'){//Employment Certificate
			wi = OB.Layout.ClassicOBCompatibility.Popup.open('CertificationLetter', 725, 550, OB.Utilities.applicationUrl("/sa.elm.ob.hcm.ad_reports.printreport/PrintReport")
					+ "?Command=DEFAULT"+"&pageType=WAD&inpRecordId=" + recordId + "&inpTabId=" + tabId + "&inpWindowID=" + windowId + "&documentNo="+documentNo , '', null, false, false, true);
		}
		if(windowId=='796272C70BFE4201BBDC848C8F487FAA' || windowId=='C8154257AADE418D8387C2319B85D762') wi.close();
	}
	 // register the print button for the PO Receipt tab, Material Issue Request and Return transaction Tab
	  // the first parameter is a unique identification so that one button can not be registered multiple times.
	OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 400,['E1FA7F1000E74C41AE4683D596C1FD7A', '076B159D222E4EEB85C70B3FEE6B22F6', '9FBF2EEF58D443EA9A403FB8D7A6DB5C']);
}());
