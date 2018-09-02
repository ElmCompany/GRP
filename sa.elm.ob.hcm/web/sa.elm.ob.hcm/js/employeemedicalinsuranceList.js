/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Priyanka Ranjan 17-03-2018
 *************************************************************************
*/
var xmlhttp = new getXMLObject(), 
currentTab = 'MEDIN',currentWindow='EMP';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var medicalInsuGrid = jQuery("#MedicalInsuranceList"), lastActiveType = 'Y';
var categoryvalue=document.getElementById("strcategory").value;
var dependentvalue=document.getElementById("strdependents").value;
var schemavalue=document.getElementById("strschema").value;

jQuery(function() {
   medicalInsuGrid.jqGrid({
	   direction :direction,
       url : contextPath+'/sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax/MedicalInsuranceAjax?action=getMedicalInsuranceList',
       colNames : [category,dependent,insuanceComName,
    	   schema,membershipNo,startdate,
    	   enddate,insuranceId,
    	   employee],
       colModel : [ 
           {
              	name : 'inscategory',
              	index : 'inscategory',
              	stype : 'select',
              	width : 100,
              	searchoptions : {
              		sopt : [ 'eq' ],
              		value : categoryvalue,
              		dataInit : function(e) {
              			e.className = "Combo gs_org_e";
              			e.style.padding = "0";
              			e.style.margin = "2px 0 0";
              			e.style.width = "95%";
              			e.style.height = "18px";
              		}
              	}  
             }, 
                    {
                    	name : 'dependents',
                    	index : 'dependents',
                    	stype : 'select',
                    	width : 100,
                    	searchoptions : {
                    		sopt : [ 'eq' ],
                    		value : dependentvalue,
                    		dataInit : function(e) {
                    			e.className = "Combo gs_org_e";
                    			e.style.padding = "0";
                    			e.style.margin = "2px 0 0";
                    			e.style.width = "95%";
                    			e.style.height = "18px";
                    		}
                    	}  
                   }, 
                    {
                        name : 'insucompname',
                        index : 'insucompname',
                        width : 90
                    },
                   {
                    	name : 'insuschema',
                    	index : 'insuschema',
                    	stype : 'select',
                    	width : 100,
                    	searchoptions : {
                    		sopt : [ 'eq' ],
                    		value : schemavalue,
                    		dataInit : function(e) {
                    			e.className = "Combo gs_org_e";
                    			e.style.padding = "0";
                    			e.style.margin = "2px 0 0";
                    			e.style.width = "95%";
                    			e.style.height = "18px";
                    		}
                    	}  
                   }, 
             
                   {
                       name : 'memshipno',
                       index : 'memshipno',
                       width : 90
                   },
               
            
            {
             	   name : 'startdate',
                   index : 'startdate',
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
                                	   medicalInsuGrid[0].triggerToolbar();
                               }
                           });
                           e.style.width = "25%";
                           setTimeout(
                                   "$('#gs_startdate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_startdate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                   10);
                          
                       }
                   } 
             },
             {
                 name : 'enddate',
                 index : 'enddate',
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
                                	 medicalInsuGrid[0].triggerToolbar();
                             }
                         });
                         e.style.width = "25%";
                         setTimeout(
                                 "$('#gs_enddate').before('<select onchange=\"searchList(\\\'ED\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_enddate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');",
                                 10);
                        
                     }
                 } 
           },
               {
                   name : 'id',
                   index : 'id',
                   width : 90,
                   hidden : true
               },
               

               {
            	   name:'employee',
            	   index:'employee',
            	   width :100,
            	   hidden : true
               }
              ],
              
              rowNum: 50,
              rowList: [20, 50, 100, 200, 500],
              pager: '#pager',
              sortname: 'insucompname',
              mtype: 'POST',
              datatype: 'json',
              rownumbers: true,
              viewrecords: true,
              sortorder: "asc",
              scrollOffset: 17,
              localReader: {
                repeatitems: false,
                page: function (obj) {
                  return 0;
                }
              },
              jsonReader: {
                repeatitems: false,
                page: function (obj) {
                  return obj.page;
                }
              },
              loadComplete: function () {
                ChangeJQGridAllRowColor(jQuery("#MedicalInsuranceList"));
                $("#jqgrid").show();
                reSizeGrid();
              },
              onSelectRow: function (id) {
                var rowData = jQuery("#MedicalInsuranceList").getRowData(id);
                document.getElementById("inpMedInsId").value = id;
                enableButton("true");
              },
             
            	  
            ondblClickRow : function(id) {
            
           	   var rowData = jQuery("#MedicalInsuranceList").getRowData(id);
               document.getElementById("inpMedInsId").value = id;
               onClickEditView();
           },
              
              
              beforeRequest: function () {
            	  jQuery("#MedicalInsuranceList").setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
                if ("" + jQuery("#MedicalInsuranceList").getPostDataItem("_search") == "true") {
                  if ("" + jQuery("#MedicalInsuranceList").getPostDataItem("startdate") != "") {
                    var date = "" + jQuery("#MedicalInsuranceList").getPostDataItem("startdate");
                    var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                    if (!validformat.test(date)) jQuery("#MedicalInsuranceList").setPostDataItem("startdate", "");
                  }
                  if ("" + jQuery("#MedicalInsuranceList").getPostDataItem("enddate") != "") {
                    var date = "" + jQuery("#MedicalInsuranceList").getPostDataItem("enddate");
                    var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                    if (!validformat.test(date)) jQuery("#MedicalInsuranceList").setPostDataItem("enddate", "");
                  }
                  jQuery("#MedicalInsuranceList").setPostDataItem("startdate_s", document.getElementById("gs_startdate_s").value);
                  jQuery("#MedicalInsuranceList").setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);
                }
                jQuery("#MedicalInsuranceList").setPostDataItem("_search", 'true');

              }
            });
   jQuery("#MedicalInsuranceList").jqGrid('navGrid', '#pager', {
              edit: false,
              add: false,
              del: false,
              search: false,
              view: false,
              beforeRefresh : function() {
                  reSizeGrid();
             }
            }, {}, {}, {}, {});

   medicalInsuGrid.jqGrid('filterToolbar', {
              searchOnEnter: false
            });
   jQuery("#MedicalInsuranceList")[0].triggerToolbar();
          });
          
         
//  medicalInsuGrid[0].triggerToolbar();
//   changeJQGridDisplay("MedicalInsuranceList", "inpEmployeeId");
//});
function enableTabs() {
    var tab = null, image = null;

    tab = document.getElementById("TabEmpInfo");
    image = document.getElementById("ImgEmpInfo");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
        	reloadTab('EMPINF')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabEmpInfo"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabEmpInfo"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
        	reloadTab('EMPINF')
        }, false);
    
    tab = document.getElementById("TabEmployee");
    image = document.getElementById("TabEmployee");
    tab.className = "tabSelected";
    image.className = "imageSelected";
    if (tab.attachEvent) {
        tab.attachEvent('onclick', function() {
            reloadTab('EMP')
        });
        tab.attachEvent('onmouseover', function() {
            setTabClass(document.getElementById("TabEmployee"), true);
        });
        tab.attachEvent('onmouseout', function() {
            setTabClass(document.getElementById("TabEmployee"), false);
        });
    }
    else
        tab.addEventListener('click', function() {
        	reloadTab('EMP')
        }, false);
    
}

function searchList(type) {
	  if (type == "SD" && document.getElementById("gs_startdate").value != "") medicalInsuGrid[0].triggerToolbar();
	  if (type == "ED" && document.getElementById("gs_enddate").value != "") medicalInsuGrid[0].triggerToolbar();
	}
function setTabClass(tab, type) {
    if (type == true)
        tab.className = "tabSelectedHOVER";
    else
        tab.className = "tabSelected";
}
function reSizeGrid() {
    if (window.innerWidth) {
        gridW = window.innerWidth - 52;
        gridH = window.innerHeight - 260;
    }
    else if (document.body) {
        gridW = document.body.clientWidth - 52;
        gridH = document.body.clientHeight - 260;
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
    medicalInsuGrid.setGridWidth(gridW, true);
    medicalInsuGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpMedInsId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'MedicalInsurance', '_self', null, true);
        return false;
    }
}
function onClickNew() {
	if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }
    document.getElementById("inpAction").value = "EditView";
    document.getElementById("inpMedInsId").value ="";
    submitCommandForm('DEFAULT', true, null, 'MedicalInsurance', '_self', null, true);
    return false;
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "GridView";
    resetTab();
    reloadTab('MEDIN');
}
function resetTab() {
   resetByTab(document.getElementById("inpMedInsId"));
 // document.getElementById("inpEmployeeId").value = "";
  document.getElementById("inpIsActive").value = ""; 
}
function resetByTab(tab) {
    tab.className = "tabNotSelected";
    tab.parentNode.replaceChild(tab.cloneNode(true), tab);
    $('#' + tab.id).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
}
function reloadWindow(tab) {
    if (tab == 'EMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee', '_self', null, true);
        return false;
    }
     if (tab == 'Dependent') {
         submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents', '_self', null, true);
         return false;
     }
     if (tab == 'MEDIN') {
         submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
          return false;
     }
    return false;

}
function reloadTab(tab) {
    if (tab == 'EMP') {
    	var url="";
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'EMPINF') {
        var url="";
        var employeeId=document.getElementById("inpEmployeeId").value;
        document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId='+employeeId;
        document.frmMain.submit();
    }
    else if (tab=='EMPADD') {
   	 document.frmMain.action =contextPath+'/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
   	 document.frmMain.submit();
   }
    else if (tab == 'Dependent') {
    	 var url="";
         var employeeId=document.getElementById("inpEmployeeId").value;
         document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId='+employeeId;
         document.frmMain.submit();
    }
    else if (tab == 'EMPQUAL') {
        var url="";
        document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView' + url;
        document.frmMain.submit();
    }
    else if (tab == 'Qualification') {
         submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
         return false;
     }
    else if(tab == 'EMPAsset'){ 
        submitCommandForm('DEFAULT', true, null, contextPath +'/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
        return false;
   }
    else if (tab == 'PREEMP') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
         return false;
    }
    else if (tab == 'EMPCTRCT') {
        var url="";
          var employeeId=document.getElementById("inpEmployeeId").value;
          document.frmMain.action = contextPath+'/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId='+employeeId;
          document.frmMain.submit();
     }
    else if (tab == 'DOC') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
         return false;
    }
    else if (tab == 'PERPAYMETHOD') {
        submitCommandForm('DEFAULT', true, null, contextPath+'/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod', '_self', null, true);
         return false;
    }
    else if (tab == 'MEDIN') {
        submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance', '_self', null, true);
         return false;
    }
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
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpMedInsId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpMedInsId=" + document.getElementById("inpMedInsId").value;

                    xmlhttp.open("GET", contextPath+'/sa.elm.ob.hcm.ad_forms.MedicalInsurance.ajax/MedicalInsuranceAjax?action=deleteMedicalInsurance' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("deleteMedicalInsurance");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure)
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpMedInsId").value = "";
                                medicalInsuGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpMedInsId").value).focus();
        });
    }
}