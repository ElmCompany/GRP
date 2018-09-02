var xmlhttp = new getXMLObject(),
    currentTab = 'DOC',
    currentWindow = 'DOC';
var rowval = '';
var employeeId = $("#inpEmployeeId").val();
var searchFlag = 0,
    onSearch = 0,
    onDelete = 0,
    gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var documentgrid = jQuery("#DocumentsList"), lastActiveType = 'Y', emailUserGrid = jQuery("#EmailUsersList");
var idCount=0;
var jsonSelUsers = {};
jsonSelUsers.List = [];

jQuery(function () {
  documentgrid.jqGrid({
    direction: direction,
    url: contextPath + '/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=getDocumentList&inpEmployeeId=' + employeeId,
    colNames: [docname, doctype, submitteddate, validdate, isOriginal, ''],
    colModel: [
/* {
      name: 'checkboxgrid',
      width: 25,
      index: 'CanDo',
      editable: true,
      edittype: 'checkbox',
      editoptions: {
        value: "True:False"
      },
      formatter: "checkbox",
      formatoptions: {
        disabled: false
      },
      stype: 'checkbox',
      sortable: false


    },*/
    {
      name: 'docname',
      index: 'docname',
      width: 100
    }, {
      name: 'doctype',
      index: 'doctype',
      stype: 'select',
      width: 90,
      searchoptions: {
        sopt: ['eq'],
        value: '0:'+select,
        dataInit: function (e) {
          e.className = "Combo";
          e.style.padding = "0";
          e.style.margin = "2px 0 0";
          e.style.width = "95%";
          e.style.height = "18px";
        }
      }
    }, {
      name: 'issueddate',
      index: 'issueddate',
      width: 40,
      searchoptions: {
        dataInit: function (e) {
          $(e).calendarsPicker({
            calendar: $.calendars.instance('ummalqura'),
            dateFormat: 'dd-mm-yyyy',
            showTrigger: '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
            changeMonth: true,
            changeYear: true,
            onClose: function (dateText, inst) {
              if (dateText != "") documentgrid[0].triggerToolbar();
            }
          });
          e.style.width = "75%";
          setTimeout("$('#gs_issueddate').before('<select onchange=\"searchList(\\\'SD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_issueddate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');", 10);
        }
      }
    }, {
      name: 'validdate',
      index: 'validdate',
      width: 40,
      searchoptions: {
        dataInit: function (e) {
          $(e).calendarsPicker({
            calendar: $.calendars.instance('ummalqura'),
            dateFormat: 'dd-mm-yyyy',
            showTrigger: '<img src="../web/sa.elm.ob.utility/js/jquery.calendars.package-2.0.2/calendar.gif" alt="Popup" class="trigger">',
            changeMonth: true,
            changeYear: true,
            onClose: function (dateText, inst) {
              if (dateText != "") documentgrid[0].triggerToolbar();
            }
          });
          e.style.width = "75%";
          setTimeout("$('#gs_validdate').before('<select onchange=\"searchList(\\\'VD\\\');\" style=\"width: 40%; padding: 0px; margin: 2px 0px 0px; height: 17px;\" class=\"Combo\" id=\"gs_validdate_s\"><option value=\"=\">" + equalto + "</option><option value=\">=\">" + greaterthanequalto + "</option><option value=\"<=\">" + lessthanequalto + "</option></select>');", 10);
        }
      }
    },
    {
        name: 'isOriginal',
        index: 'isOriginal',
        stype: 'select',
        width: 90,
        searchoptions: {
          sopt: ['eq'],
          value: '0:'+select+';O:'+original+';D:'+duplicate,
          dataInit: function (e) {
            e.className = "Combo";
            e.style.padding = "0";
            e.style.margin = "2px 0 0";
            e.style.width = "95%";
            e.style.height = "18px";
          }
        }
      },
      
      {
      name: 'download',
      index: 'download',
      width: 30,
      sortable: false,
      stype: '',
      fixed: true,
      formatter: function () {
        return "<img name='downloadimg' id='downloadimg' style='height:65%;width:65%' src='../web/sa.elm.ob.hcm/images/download.ico' alt='download' />";
      },
    },

    ],
    mtype: 'POST',
    rowNum: 50,    
    rowList: [20, 50, 100, 200, 500],
    pager: '#pager',
    datatype: 'xml',
    rownumbers: true,
    viewrecords: true,
    shrinkToFit: true,
    autowidth: true,
    sortname: 'docname',    
    sortorder: "asc",
    scrollOffset: 17,
    //selRowId = documentgrid.jqGrid ('getGridParam', 'selrow'),
    //celValue = documentgrid.jqGrid ('getCell', selRowId, 'checkboxgrid'),
    multiselect: true,
    onSelectRow: function (id, status) {
      onChangeField();
      var ids = "" + jQuery("#DocumentsList").jqGrid('getGridParam', 'selarrrow');
      var idList = ids.split(",");
      var idListLength = idList.length;
      jQuery('#DocumentsList').editRow(id, true);
      if(status){
          enableButton("true");
         }else{
        	 enableButton("false");
         }
      var rowData = documentgrid.getRowData(id);
      if(status){
      document.getElementById("inpDocumentId").value = id;
      document.getElementById("inpAddressId").value = rowData['addressid'];
      }
      else{
    	  document.getElementById("inpDocumentId").value ="";
    	  document.getElementById("inpAddressId").value = "";
      }
     

      /*if(status==true){
    	  document.getElementById("Email_Button").style.display="";
          document.getElementById("spacebar").style.display="none";
      }else if(status==false){
    	  document.getElementById("Email_Button").style.display="none";
          document.getElementById("spacebar").style.display="";
      }*/
      
    
      //for deletion id array
      if (status == true) {
    	  ChangeJQGridSelectMultiRowColor(id, "S");
    	  rowval = rowval + "[" + id + "]";
    	  
    	  if(idListLength==idCount)
    	  {
    		  document.getElementById("cb_DocumentsList").checked = true;
    	  }
    	
      } else {
        rowval = rowval.replace("[" + id + "]", '');
        ChangeJQGridSelectMultiRowColor(id, "US");
        jQuery('#DocumentsList').restoreRow(id);
        
       
         /* if(document.getElementById("cb_EquipmentList").checked = true)
			document.getElementById("cb_DocumentsList").checked = false;*/
      }

/*   // alert("idListLength"+idListLength+"status"+status);
        alert(document.getElementById("cb_DocumentsList").checked);*/
      if (idListLength > 1) {
        document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
        document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
       
      } else if (idListLength == 1 && status) {
    	 enableButton("true");

      }	  
    },
    ondblClickRow: function (id) {
      var rowData = documentgrid.getRowData(id);
      document.getElementById("inpDocumentId").value = id;
      onClickEditView();
    },
    beforeRequest: function () {
      "" + documentgrid.jqGrid('getGridParam', 'postData')['_search']
    
      if ("" + documentgrid.getPostDataItem("_search") == "true" && onDelete == 0) {
    	 
        document.getElementById("inpDocumentId").value = "";
      }

      documentgrid.setPostDataItem("inpEmployeeId", document.getElementById("inpEmployeeId").value);
      if (onDelete == 0) documentgrid.setPostDataItem("DeleteEmployee", "");

      if ("" + documentgrid.getPostDataItem("_search") == "true") {
        if ("" + documentgrid.getPostDataItem("issueddate") != "") {
          var date = "" + documentgrid.getPostDataItem("issueddate");
          var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
          if (!validformat.test(date)) documentgrid.setPostDataItem("issueddate", "");
        }
        if ("" + documentgrid.getPostDataItem("issueddate") != "") {
          var validformat = /^\d{1,2}\-\d{1,2}\-\d{4}$/
          var date = "" + documentgrid.getPostDataItem("issueddate");
          if (!validformat.test(date)) documentgrid.setPostDataItem("validdate", "");
        }

        documentgrid.setPostDataItem("issueddate_s", document.getElementById("gs_issueddate_s").value);
        documentgrid.setPostDataItem("validdate_s", document.getElementById("gs_validdate_s").value);

      }
      documentgrid.setPostDataItem("_search", 'true');

    },
    afterInsertRow: function (rowid, data) {
      $("td:eq(7)", "#" + rowid).click(function () {
        $("#DownloadAttachmentFrame").click();
        var employeeId = $("#inpEmployeeId").val();
        var documentId = $("#inpDocumentId").val();
        document.getElementById("DownloadAttachmentFrame").src = contextPath + "/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?" + "inpAction=DownloadGrid&inpEmployeeId=" + employeeId + "&inpDocumentId=" + rowid + "";
      });
    },
    onSortCol: function (index, columnIndex, sortOrder) {

    },
    onPaging: function () {

    },
    loadComplete: function () {
      var idList = documentgrid.getDataIDs();
      idCount = idList.length;
      var idValue = document.getElementById("inpDocumentId").value;
      var doctyp = document.getElementById("gs_doctype").value;
      $("#gs_doctype").find("option").remove().end();
      $("#gs_doctype").append(doctypels1);
      $("#gs_doctype").val(doctyp);
      		
      ChangeJQGridAllRowColor(documentgrid);
      $("#LoadingContent").hide();
      $("#jqgrid").show();
      if (idCount > 0) documentgrid.trigger("resize");
      setTimeout(function () {
        if (document.getElementById("inpDocumentId").value.length == 32) {
          documentgrid.closest(".ui-jqgrid-bdiv").scrollTop(22 * (documentgrid.getInd(document.getElementById("inpDocumentId").value) - 2));
        }
      }, 100);
      enableButton("false");
    }
  });
  documentgrid.jqGrid('navGrid', '#pager', {
    edit: false,
    add: false,
    del: false,
    search: false,
    view: false,
    beforeRefresh: function () {
      document.getElementById("gs_issueddate").value = '=';
      document.getElementById("gs_validdate").value = '=';

      if (onSearch == 1) {
        documentgrid[0].clearToolbar();
        documentgrid[0].toggleToolbar();
      }
      onSearch = 0;
      searchFlag = 0;
      reSizeGrid();
    }
  }, {}, {}, {}, {});

  documentgrid.jqGrid('filterToolbar', {
    searchOnEnter: false
  });
  
  //if selectall checkbox delete button is enable 
  $('#cb_DocumentsList').change(function() {
      if($(this).is(":checked")) {
      	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
      	 document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
      }    
    else{
    	  document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
          document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
      }
     
  });
  
  
  documentgrid[0].triggerToolbar();
  
  emailUserGrid.jqGrid({
	    direction: direction,
	    url: contextPath + '/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=getUsersList',
	    colNames: [ empName, emailId],
	    colModel: [ 
	                {
						name: 'employee',
						index: 'employee'	      
	                },
	                {
					    name: 'email',
					    index: 'email'					    
					},
	              ],
	    
	    mtype: 'POST',	    
	    pager: '#emailpager',	    
	    datatype: 'xml',
	    rownumbers: true,
	    viewrecords: true,
	    shrinkToFit: true,
	    autowidth: true,
	    sortname: 'employee',
	    sortorder: "asc",
	    multiselect: true ,	
	    pgbuttons:false,
	    pgtext:'',
	    
	    onSelectRow: function (id, status) {
	    		
	      var rowData = emailUserGrid.getRowData(id);
	      var ids = ""+emailUserGrid.jqGrid('getGridParam','selarrrow');
          var idList=ids.split(",");
          var idListLength=idList.length;
          if(ids.indexOf(",")==0)
              idListLength--;
	   		
          if(status==true)
          {
              ChangeJQGridSelectMultiRowColor(id, "S");
            
              var canAdd = true, data = jsonSelUsers.List;
              for ( var i in data) {
                  if (('' + data[i].id) == id) {
                      canAdd = false;
                      break;
                  }
              }
              if (canAdd) {
                  var len = jsonSelUsers.List.length;
                  jsonSelUsers.List[len] = {};
                  var rowData = emailUserGrid.getRowData(id);
                  jsonSelUsers.List[len]['EmployeeName'] = rowData['employee'];                
                  jsonSelUsers.List[len]['EmailId'] = rowData['emailId']; 
                  jsonSelUsers.List[len]['id'] = id;
                  
              }               
          }   
          else if (status==false)
          {
              ChangeJQGridSelectMultiRowColor(id, "US");
              findAndRemoveJSONObj(jsonSelUsers.List, 'id', id);
          }
	    },
	    onSelectAll:  function(id,status)
        {
            var ids = ""+id
            var idList=ids.split(",");
            if(idList.indexOf(",")==0)
                i=1;
            else
                i=0;
            for(var i=i;i<idList.length;i++)
            {
                if(status==true)
                {
                	
                    ChangeJQGridSelectMultiRowColor(idList[i], "S");
                    var canAdd = true, data = jsonSelUsers.List;
                      for ( var p in data) {
                        if (('' + data[p].id) == idList[i]) {
                            canAdd = false;
                            break;
                        }
                    } 
                    if (canAdd) { 
                        var len = jsonSelUsers.List.length;
                        jsonSelUsers.List[len] = {};
                        
                        var rowData = emailUserGrid.getRowData(idList[i]);
                        jsonSelUsers.List[len]['EmployeeName'] = rowData['employee'];                
                        jsonSelUsers.List[len]['EmailId'] = rowData['emailId'];                                
                        jsonSelUsers.List[len]['id'] = idList[i];                      
                     } 
                }   
                else if (status==false)
                {
                    ChangeJQGridSelectMultiRowColor(idList[i], "US");       
                    findAndRemoveJSONObj(jsonSelUsers.List, 'id', idList[i]);
                }
            }	            
        },
        loadComplete:function()
	    {		
        	$('input[type="checkbox"]').click(function(){
        		jQuery("#DocumentsList").setSelection($(this).parent().parent().attr('id'));
        		});
	    	reSizeGrid();
	    	$("#LoadingContent").hide();
	    	$("#emailjqgrid").show();
	    	ChangeJQGridAllRowColor(emailUserGrid);
	    	var idList = emailUserGrid.getDataIDs();
    		var idCount = idList.length;  
    		
    		var jsonObjString = JSON.stringify(jsonSelUsers);      
            for ( var i = 0; i < idCount; i++) { 
            	 if (jsonObjString.indexOf(idList[i]) >= 0){                    
                    emailUserGrid.setSelection(idList[i]);
            	 }
            } 
	    },
	}); 
  	emailUserGrid.jqGrid('navGrid','#emailpager',{edit:false,add:false,del:false,search:false,view: false,beforeRefresh:function(){			
		if(onSearch == 1)
		{
			emailUserGrid[0].clearToolbar();
			emailUserGrid[0].toggleToolbar();
		}
		jsonSelUsers.List=[];
		onSearch = 0;searchFlag =0;			
		emailUserGrid.trigger("reloadGrid");
		}
	},{ },{ },{ }, { });
  	emailUserGrid.jqGrid('navButtonAdd',"#emailpager",
	{
		caption:search,
		buttonicon :'ui-icon-search',
		onClickButton:function()
		{ 
			emailUserGrid[0].toggleToolbar();
			if(searchFlag == 0) onSearch = 1;
			else { onSearch = 0; emailUserGrid[0].clearToolbar(); }			
			if(searchFlag == 0) searchFlag = 1;
			else searchFlag = 0;
		}  
	});
  	emailUserGrid.jqGrid('navButtonAdd',"#emailpager",
	{
		caption:reset,
		buttonicon :'ui-icon-refresh',
		onClickButton:function()
		{ 	
			emailUserGrid[0].clearToolbar();
		} 
	});
	emailUserGrid.jqGrid('filterToolbar',{searchOnEnter: false});
	emailUserGrid[0].triggerToolbar();
	emailUserGrid[0].toggleToolbar() ;
	reSizeGrid();
  
  	changeJQGridDisplay("DocumentsList", "inpDocumentId");
});

function onChangeField() {
  enableButton("true");
}

function download() {
  //alert('download');
  /*$("#DownloadAttachmentFrame").click();
	  var employeeId = $("#inpEmployeeId").val();
	  var documentId = $("#inpDocumentId").val();
	  document.getElementById("DownloadAttachmentFrame").src = contextPath + "/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=Download&inpEmployeeId=" + employeeId + "&inpDocumentId=" + documentId + "&inpFileName=" + $('#inpFileName').val() + "&inpPath=" + $('#inpPath').val();*/
}

function getSelectedRows() {
/*    var searchIDs = $("input[name='checkboxgrid']").prop('checked').val();
        alert(searchIDs);*/

  $('input[type="checkbox"]:checked').each(function () {
  });
}

function searchList(type) {
  if (type == "SD" && document.getElementById("gs_issueddate").value != "") documentgrid[0].triggerToolbar();
  if (type == "VD" && document.getElementById("gs_validdate").value != "") documentgrid[0].triggerToolbar();
}

function onClickEditView() {
  if (document.getElementById("inpDocumentId").value != "") {
    document.getElementById("inpAction").value = "EditView";
    submitCommandForm('DEFAULT', true, null, 'Documents', '_self', null, true);
    return false;
  }
}

function onClickDelete() {
  if (document.getElementById('buttonDelete').className == 'Main_ToolBar_Button' && document.getElementById("inpDocumentId").value != "") {
    OBAsk(confirmdelete, function (result) {
      if (result) {
        xmlhttp = new getXMLObject();
        if (xmlhttp) {
          var urlPath = "&inpDocumentId=" + document.getElementById("inpDocumentId").value + "&rowval=" + rowval;
          xmlhttp.open("GET", contextPath + '/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=deleteDocument' + urlPath, true);

          xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
              var resource = xmlhttp.responseXML.getElementsByTagName("DeleteDocument");
              var response = resource[0].getElementsByTagName("Response")[0].childNodes[0].nodeValue;
              if (response == 'false') {
                displayMessage("E", error, deletefailure);
              } else {
                //onDelete=1;
                displayMessage("S", success, deletesuccess);
                document.getElementById("inpDocumentId").value = "";
                documentgrid.trigger("reloadGrid");
              }
            }
          };
          xmlhttp.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
          xmlhttp.send(null);
        }
      } else document.getElementById(document.getElementById("inpDocumentId").value).focus();
    });

  }

}

function openEmailPopUp()
{	
	if(getDocuments()){
		var w, h;
		if (window.innerWidth)  { w = window.innerWidth - 725; h = window.innerHeight - 217;}
		else if (document.body)  { w = document.body.clientWidth - 200; h = document.body.clientHeight - 217;}
		
		$("#emaildialog").show();	
		$("#emaildialog").dialog({modal:true,position:"center", width: w});
		$(document).scrollTop(100);
	}	
}
function getDocuments(){
	var documents = {};
	documents.Documents = [];
	var docs = ""+documentgrid.jqGrid('getGridParam','selarrrow');
	/*var docs="";
	 $('input[type="checkbox"]:checked').each(function () {
		 docs +=","+ this.value;    
		  });
	docs=docs.substring(1);*/
	
	if(docs=="")
	{			
		OBAlert(selectDocument);
		return false;
	}
	else
	{
		var idList=docs.split(",");
	    for(var i=0;i<idList.length;i++)
	    {
	    	var rowData = documentgrid.getRowData(idList[i]); 
	       	var docId=idList[i];	
		    if(documents.Documents=="" || documents.Documents==null)
			{	       		
		    	documents.Documents[i]={};			    	       			
		    	documents.Documents[i]['DocId']=docId;
			}
		    else
	  		{
		    	documents.Documents[i]={};
		     	documents.Documents[i]['DocId']=docId;
	  		}
       }
	   document.getElementById("inpDocs").value = JSON.stringify(documents);
	   return true;
	}
}
function getUsers(){
	var data = jsonSelUsers.List;
	if (data.length == 0) {
        OBAlert(selectEmp);
        return false;               
    }
    else{       
        for ( var rowId in data) 
        { 	   
       		var empName=data[rowId].EmployeeName;
       		var emailId=data[rowId].EmailId;    		
       		var empId=data[rowId].id;
       		if(emailId==null || emailId==''){
       			OBAlert(emailIdNotDefined +" "+empName);
       	        return false; 
       		}       			
        }
        
        document.getElementById("inpUsers").value = JSON.stringify(jsonSelUsers);
        //jsonSelUsers.List=[];
        $( "#emaildialog" ).dialog("close");
        return true;
    }
}
function emailUsers()
{	
	if(getDocuments() && getUsers()){
		OBConfirm(sureToSendEmail,
				function(result)
				{
		       		if(result)
		       		{		       			
		       			document.getElementById("SubmitType").value="email";		       			
		       			document.getElementById("inpAction").value="GridView";
		       			reloadTab('DOC');
		       		}
				});	
	}
	
}

function enableButton(flag) {
	  var ids = "" + jQuery("#DocumentsList").jqGrid('getGridParam', 'selarrrow');
	  var idList = ids.split(",");
	  var idListLength = idList.length;
  if (flag == "true" && document.getElementById('buttonEdition').className != 'Main_ToolBar_Button') {
    document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
    document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
    document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
    document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
  } else if (flag == 'false') {
    document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
    document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
   document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
   document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
  }
  
//  var totalrowcount=jQuery("#DocumentsList").jqGrid('getGridParam', 'selarrrow');
//  var rowcount=totalrowcount.length;

  else if(idListLength>0){
	 document.getElementById('buttonEdition').className = 'Main_ToolBar_Button';
	 document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition';
	 document.getElementById('buttonDelete').className = 'Main_ToolBar_Button';
   	 document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete';
  }
  else{
	  document.getElementById('buttonEdition').className = 'Main_ToolBar_Button_disabled';
	  document.getElementById('linkButtonEdition').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Edition_disabled';
	  document.getElementById('buttonDelete').className = 'Main_ToolBar_Button_disabled';
      document.getElementById('linkButtonDelete').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Delete_disabled';
  }
}

function reSizeGrid() {
  if (window.innerWidth) {
    gridW = window.innerWidth - 52;
    gridH = window.innerHeight - 260;
  } else if (document.body) {
    gridW = document.body.clientWidth - 52;
    gridH = document.body.clientHeight - 260;
  }
  if (onSearch == 1) {
    gridH = gridH - 23;
    if (navigator.userAgent.toLowerCase().indexOf("webkit") != -1) gridH++;
  } else if (parseInt(document.getElementById("client").scrollHeight) + 77 > parseInt(document.body.clientHeight)) gridW = gridW - 14;
  if (gridW < 800) gridW = 800;
  if (gridH < 200) gridH = 200;
  documentgrid.setGridWidth(gridW, true);
  documentgrid.setGridHeight(gridH, true);
  
  var w, h;
  if (window.innerWidth)  { w = window.innerWidth - 800; h = window.innerHeight - 430; }
  else if (document.body)  { w = document.body.clientWidth - 56; h = document.body.clientHeight - 300; }		
  emailUserGrid.setGridWidth(w, true);
  emailUserGrid.setGridHeight(h, true);
}
function reloadTab(tab) {
  if (tab == 'EMP') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView', '_self', null, true);
    return false;
  } else if (tab == 'EMPINF') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'EMPADD') {
   
	  document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
    document.frmMain.submit();
  } else if (tab == 'Dependent') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'EMPQUAL') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
    return false;
  } else if (tab == 'PREEMP') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
    return false;
  } else if (tab == 'EMPCTRCT') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'Asset') {
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'DOC') {
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