/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
*/
var xmlhttp = new getXMLObject(), 
currentTab = 'Dependent',currentWindow='EMP';
var searchFlag = 0, onSearch = 0, onDelete = 0, gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var dependentGrid = jQuery("#DependentsList"), lastActiveType = 'Y';
jQuery(function() {
   dependentGrid.jqGrid({
	   direction :direction,
       url : contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.ajax/DependentsAjax?action=getDependentList',
       colNames : [ DependentName,realtionship,age,gender,startDate,endDate,nationdalId,phoneNo,loc],
       colModel : [
                   {	name : 'name',
                	    index:'name',
                		width :250
                    },
                   
                   {
                    	name : 'rel',
                    	index : 'rel',
                    	stype : 'select',
                    	width : 100,
                    	searchoptions : {
                    		sopt : [ 'eq' ],
                    		value : '0:'+select,
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
                   name : 'age',
                   index : 'age',
                   width : 90
               },
               
               {
                   name : 'gender',
                   index : 'gender',
                   stype : 'select',
                   width : 90,
                     searchoptions : {
                       sopt : [ 'eq' ],
                       value : '0:'+select+';M:'+male+';F:'+female,
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
                                	   dependentGrid[0].triggerToolbar();
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
                                	 dependentGrid[0].triggerToolbar();
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
                   name : 'nationdalId',
                   index : 'nationdalId',
                   width : 90
               },
               {
                   name : 'phoneno',
                   index : 'phoneno',
                   width : 90
               },
               {
            	   name:'location',
            	   index:'location',
            	   width :100
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
       onSelectRow : function(id) {
    	   ChangeJQGridSelectRowColor(document.getElementById("inpDependentId").value, id);
           var rowData = dependentGrid.getRowData(id);
           document.getElementById("inpDependentId").value = id;
           enableButton("true");
       },
       ondblClickRow : function(id) {
    	   var rowData = dependentGrid.getRowData(id);
           document.getElementById("inpDependentId").value = id;
           onClickEditView();
       },
       beforeRequest : function() {
    	   dependentGrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
    	   if ("" + dependentGrid.getPostDataItem("_search") == "true") {
               if ("" + dependentGrid.getPostDataItem("startdate") != "") {
                   var date = "" + dependentGrid.getPostDataItem("startdate");
                   var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                   if (!validformat.test(date))
                	   dependentGrid.setPostDataItem("startdate", "");
               }
               if ("" + dependentGrid.getPostDataItem("_search") == "true") {
                   if ("" + dependentGrid.getPostDataItem("enddate") != "") {
                       var date = "" + dependentGrid.getPostDataItem("enddate");
                       var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
                       if (!validformat.test(date))
                    	   dependentGrid.setPostDataItem("enddate", "");
                   }
               }
               dependentGrid.setPostDataItem("startdate_s", document.getElementById("gs_startdate_s").value);
               dependentGrid.setPostDataItem("enddate_s", document.getElementById("gs_enddate_s").value);
           }
    	   dependentGrid.setPostDataItem("_search", 'true');
       },
       onSortCol : function(index, columnIndex, sortOrder) {
      
       },
       onPaging : function() {
         
       },
       loadComplete : function() {
    	   var idValue = document.getElementById("inpDependentId").value;
    	   var rel=document.getElementById("gs_rel").value;
    	   dependentGrid.setSelection(idValue);
    	   ChangeJQGridAllRowColor(dependentGrid);
    	   $("#jqgrid").show();
    	   //load relation
    	   var url = contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.ajax/DependentsAjax?action=getRelationShip&inpEmployeeId='+document.getElementById("inpEmployeeId").value;
    		$.getJSON(url, function(result){
    			
    		}).done(function(data){

    			$("#gs_rel")
    	   		.find("option")
    	   		.remove()
    	   		.end();
    			
    			$("#gs_rel").append("<option value='0' selected>select</option>");
    			$.each( data, function( i, item ) {
    				$("#gs_rel").append("<option value='"+item.relValue+"'>"+item.rel+"</option>");
    		      });
    				$("#gs_rel").val(rel);
    		});
       }
   });
   dependentGrid.jqGrid('navGrid', '#pager', {
       edit : false,
       add : false,
       del : false,
       search : false,
       view : false
   }, {}, {}, {}, {});
  
   dependentGrid.jqGrid('filterToolbar', {
       searchOnEnter : false
   });
   dependentGrid[0].triggerToolbar();
   changeJQGridDisplay("DependentsList", "inpEmployeeId");
}); 
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
	  if (type == "SD" && document.getElementById("gs_startdate").value != "") dependentGrid[0].triggerToolbar();
	  if (type == "ED" && document.getElementById("gs_enddate").value != "") dependentGrid[0].triggerToolbar();
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
    dependentGrid.setGridWidth(gridW, true);
    dependentGrid.setGridHeight(gridH, true);
}
function onClickEditView() {
    if (document.getElementById('buttonEdition').className == 'Main_ToolBar_Button' && document.getElementById("inpDependentId").value != "") {
        document.getElementById("inpAction").value = "EditView";
        submitCommandForm('DEFAULT', true, null, 'Dependents', '_self', null, true);
        return false;
    }
}
function onClickNew() {
	if( document.getElementById('linkButtonNew').className == 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_New_disabled')
    {
       return false;
    }	
    document.getElementById("inpAction").value = "EditView";
    document.getElementById("inpDependentId").value ="";
    submitCommandForm('DEFAULT', true, null, 'Dependents', '_self', null, true);
    return false;
}
function onClickRefresh() {
    document.getElementById("inpAction").value = "GridView";
    resetTab();
    reloadTab('Dependent');
}
function resetTab() {
   resetByTab(document.getElementById("TabDependent"));
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
    if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpDependentId").value != "") {
        OBAsk(confirmdelete, function(result) {
            if (result) {

                xmlhttp = new getXMLObject();
                if (xmlhttp) {
                    var urlPath = "&inpDependentId=" + document.getElementById("inpDependentId").value;

                    xmlhttp.open("GET", contextPath+'/sa.elm.ob.hcm.ad_forms.dependents.ajax/DependentsAjax?action=DeleteDependent' + urlPath, true);
                    xmlhttp.onreadystatechange = function() {
                        if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
                            var resource = xmlhttp.responseXML.getElementsByTagName("DeleteDependent");
                            var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
                            if (response == 'false') {
                                displayMessage("E", error, deletefailure)
                            }
                            else {
                                displayMessage("S", success, deletesuccess);
                                document.getElementById("inpDependentId").value = "";
                                dependentGrid.trigger("reloadGrid");
                            }
                        }
                    };
                    xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
                    xmlhttp.send(null);
                }
            }
            else
                document.getElementById(document.getElementById("inpDependentId").value).focus();
        });
    }
}