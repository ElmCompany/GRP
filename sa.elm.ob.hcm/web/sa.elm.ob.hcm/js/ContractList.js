/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/
var xmlhttp = new getXMLObject(),currentTab = 'EMPCTRCT';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var contractGrid = jQuery("#ContractList"), lastActiveType = 'Y';

 jQuery(function() {
    contractGrid.jqGrid({
    	direction :direction,
        url : contextPath+'/ContractAjax?action=GetContractList',
        colNames : [ contractType, ContractNo, StartDate,Duration,ExpiryDate,Grade,
                     JobNo, LetterNo,LetterDate,DecisionNo,'Status','ShortStatus'],
        colModel : [
                {
                    name : 'contractType',
                    index : 'contractType',
                    width : 60
                },
                {
                    name : 'contractNo',
                    index : 'contractNo',
                    width : 60
                },
                {
                    name : 'StartDate',
                    index : 'StartDate',
                    align : 'center',
                    width : 100,
                    searchoptions : {
                       dataInit : function(e) {
                           
                           $(e).calendarsPicker({
                               calendar: $.calendars.instance('ummalqura'),
                               dateFormat: 'dd-mm-yyyy',
                               showTrigger:  
                                   '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                               changeMonth : true,
                               changeYear : true,
                               onClose : function(dateText, inst) {
                                   if (dateText != "")
                                	   contractGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_StartDate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_StartDate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                   10);
                       }
                   }
                },
                {
                    name : 'Duration',
                    index : 'Duration',
                    width : 100
                },
                {
                    name : 'expiryDate',
                    index : 'expiryDate',
                    width : 100,
                    align : 'center',
                    searchoptions : {
                       dataInit : function(e) {
                           
                           $(e).calendarsPicker({
                               calendar: $.calendars.instance('ummalqura'),
                               dateFormat: 'dd-mm-yyyy',
                               showTrigger:  
                                   '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                               changeMonth : true,
                               changeYear : true,
                               onClose : function(dateText, inst) {
                                   if (dateText != "")
                                	   contractGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_expiryDate').before('<select onchange=\"searchList(\\\'ED\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_expiryDate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                   10);
                       }
                   }
                },
                {
                    name : 'grade',
                    index : 'grade',
                    width : 60
                },
                {
                    name : 'jobno',
                    index : 'jobno',
                    width : 60
                },
                {
                    name : 'letterNo',
                    index : 'letterNo',
                    width : 100
                },
                {
                    name : 'letterDate',
                    index : 'letterDate',
                    align : 'center',
                    width : 100,
                    searchoptions : {
                       dataInit : function(e) {
                           
                           $(e).calendarsPicker({
                               calendar: $.calendars.instance('ummalqura'),
                               dateFormat: 'dd-mm-yyyy',
                               showTrigger:  
                                   '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
                               changeMonth : true,
                               changeYear : true,
                               onClose : function(dateText, inst) {
                                   if (dateText != "")
                                	   contractGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_letterDate').before('<select onchange=\"searchList(\\\'LD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_letterDate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                   10);
                          
                       }
                   }
                },
                {
                    name: 'decisionNo', index: 'decisionNo', width : 100
                },
                {
                    name: 'status', index: 'status',hidden : true
                },
                {
                    name: 'shortstatus', index: 'shortstatus',hidden : true
                }
                ],
        rowNum : 50,
        mtype: 'POST',
        rowList : [ 20, 50, 100, 200, 500 ],
        pager : '#pager',
        sortname : 'value',
        datatype : 'xml',
        rownumbers : true,
        viewrecords : true,
        sortorder : "asc",
        scrollOffset : 17,
        loadComplete : function() {
        	var idValue = document.getElementById("inpContractId").value;
        	contractGrid.setSelection(idValue);
        	ChangeJQGridAllRowColor(contractGrid);
        	$("#jqgrid").show();
        },
        onSelectRow : function(id) {
           ChangeJQGridSelectRowColor(document.getElementById("inpContractId").value, id);
            var rowData = contractGrid.getRowData(id);
            document.getElementById("inpContractId").value = id;
            enableButton("true");
            if(rowData['shortstatus']=='ISS'){
            	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
          	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
            	$("#cancelButton").show();
            }
            else{
            	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
          	     document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
            	 $("#cancelButton").hide();
            	
            }
        },
        ondblClickRow : function(id) {
        	var rowData = contractGrid.getRowData(id);
            document.getElementById("inpContractId").value = id;
            onClickEditView();
        },
        beforeRefresh : function() {
            document.getElementById("gs_StartDate").value = '=';
            document.getElementById("gs_expiryDate").value = '=';
            document.getElementById("gs_letterDate").value = '=';
            if (onSearch == 1) {
            	contractGrid[0].clearToolbar();
            	contractGrid[0].toggleToolbar();
            }
            onSearch = 0;
            searchFlag = 0;
            reSizeGrid();
        },
        beforeRequest : function() {
        	contractGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
             if ("" + contractGrid.getPostDataItem("_search") == "true") {
                    if ("" + contractGrid.getPostDataItem("StartDate") != "") {
                        var date = "" + contractGrid.getPostDataItem("StartDate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                        	contractGrid.setPostDataItem("StartDate", "");
                    }
                    if ("" + contractGrid.getPostDataItem("expiryDate") != "") {
                        var date = "" + contractGrid.getPostDataItem("expiryDate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                        	contractGrid.setPostDataItem("expiryDate", "");
                    }
                    if ("" + contractGrid.getPostDataItem("letterDate") != "") {
                        var date = "" + contractGrid.getPostDataItem("letterDate");
                        var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                        if (!validformat.test(date))
                        	contractGrid.setPostDataItem("letterDate", "");
                    }
                    contractGrid.setPostDataItem("startdate_s", document.getElementById("gs_StartDate_s").value);
                    contractGrid.setPostDataItem("enddate_s", document.getElementById("gs_expiryDate_s").value);
                    contractGrid.setPostDataItem("letterdate_s", document.getElementById("gs_letterDate_s").value);

             }
             contractGrid.setPostDataItem("_search", 'true');
        }
    });
    contractGrid.jqGrid('navGrid', '#pager', {
        edit : false,
        add : false,
        del : false,
        search : false,
        view : false
    }, {}, {}, {}, {});
   
    contractGrid.jqGrid('filterToolbar', {
        searchOnEnter : false
    });
    contractGrid[0].triggerToolbar();
    changeJQGridDisplay("ContractList", "inpEmployeeId");
}); 
 function searchList(type) {
	  if (type == "LD" && document.getElementById("gs_letterDate").value != "") contractGrid[0].triggerToolbar();
	  if (type == "SD" && document.getElementById("gs_StartDate").value != "") contractGrid[0].triggerToolbar();
	  if (type == "ED" && document.getElementById("gs_expiryDate").value != "") contractGrid[0].triggerToolbar();
	}

function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 252;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 252;
    }
    if (onSearch == 1) {
        gridH = gridH - 23;
        if (navigator.userAgent.toLowerCase().indexOf("webkit") != -1)
            gridH++;
    }
    else if (parseInt(document.getElementById("client").scrollHeight) + 77 > parseInt(document.body.clientHeight))
        gridW = gridW - 14;
    if (gridW < 800)
        gridW = 800;
    if (gridH < 200)
        gridH = 200;
    contractGrid.setGridWidth(gridW, true);
    contractGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if ((document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpContractId").value != "") ||($('#inpIssuance').val()=='I')) {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
        return false;
    }
}
function onClickNew() {
	if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }	
	document.getElementById("inpContractId").value = '';
    document.getElementById("inpAction").value = "EditView";
    submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
    return false;
}

function enableButton(flag) {
    if (flag == "true" && document.getElementById('buttonEdition').className != 'Main_ToolBar_Button') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
    }
    else if (flag == 'false') {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
        document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
    }
}
function onClickDelete() {
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpContractId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpContractId=" + document.getElementById("inpContractId").value;

                    xmlhttp.open("GET", contextPath+'/ContractAjax?action=DeleteContract' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteContract");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure);
                                document.getElementById("inpContractId").value = "";
                                document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
                                document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpContractId").value = "";
                                document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
                                document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
                                contractGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpContractId").value).focus();
        });
    }
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "GridView";
    reloadTab('EMPCTRCT');
}
function reloadWindow(tab) {
    if (tab == 'EMPINF') {
        submitCommandForm('DEFAULT', true, null, 'Employee', '_self', null, true);
        return false;
    }
/*     else (tab == 'EMPI') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/com.qualiantech.hr.ad_forms.employeesetup.identity.header/Identity', '_self', null, true);
        return false;
    }
 */
    return false;
}


function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}
function reloadTab(tab) {
	var url="";
	document.getElementById("inpEmpNo").value=document.getElementById("inpEmpNo").value;
    if (tab == 'EMP') {
    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment' + url;
        document.frmMain.submit();
    }
    else if (tab == 'Dependent') {
   	 var url="";
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
   }
    else if (tab == 'Qualification') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
        return false;
    }
    else if(tab == 'Asset'){
    	submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
        return false;
    }
    else if (tab == 'EMPCTRCT') {
      	 var url="";
           var employeeId=document.getElementById("inpEmployeeId").value;
           document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
           document.frmMain.submit();
      }
    else if (tab == 'EMPADD') {
    	document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
        document.frmMain.submit();
    }
    else if(tab == 'Asset'){
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
        return false;
    }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if (tab == 'DOC') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
         return false;
    }
    else if (tab == 'PERPAYMETHOD') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod', '_self', null, true);
         return false;
    }
    else if (tab == 'MEDIN') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
         return false;
    }
}
//issuance
function cancelContract(){
	OBAsk(askCorrection, function(result) {
		if(result){
			 document.getElementById("SubmitType").value="CancelContract";
			 document.getElementById("inpTrxStatus").value="UP";
			 var url="";
		     var employeeId=document.getElementById("inpEmployeeId").value;
		     document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
		     document.frmMain.submit();	
		}
	     else{
	 		contractGrid.trigger('reloadGrid');
	 	}
	});
}
