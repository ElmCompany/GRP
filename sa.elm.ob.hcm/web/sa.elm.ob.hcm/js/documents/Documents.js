function reSizeGrid() {
  if (window.innerWidth) {
    gridW = window.innerWidth - 52;
    gridH = window.innerHeight - 241;
  } else if (document.body) {
    gridW = document.body.clientWidth - 52;
    gridH = document.body.clientHeight - 241;
  }
  if (gridW < 800) gridW = 800;
  if (gridH < 200) gridH = 200;
}
$( document ).ready(function() {
    if($('#inpEmployeeIsActive').val()=="false"){
    	$("#inpDocType").prop("disabled", true);
    	$("#inpIsOriginal").prop("disabled", true);
    //	$("#inpFile").prop("disabled", true);
    $('#inpIssuedDate').prop('readonly',true);
    $('#inpValidDate').prop('readonly',true);
    }
});
/*function upload(){
    $("#inpFile").click();
    var filename = $('input[type=file]')[0].files[0].name;
    $("#inpFileName").val(filename);
    var file=$('input[type=file]')[0].files[0];
    alert(file);
}*/
/*
function downloadAttachment(attchmentFileId) {
    attchmentFileId = attchmentFileId.split("_");
    var url = "&inpAttachmentId=" + attchmentFileId[0];
    url += "&inpAttachmentFileId=" + attchmentFileId[1];
    document.getElementById("DownloadAttachmentFrame").src = '<%= request.getContextPath() %>/com.qualiantech.hr.ad_forms.employeesetup.experience.header/Attachment?act=DownloadFile' + url;
}*/

function download() {
  $("#DownloadAttachmentFrame").click();
  var employeeId = $("#inpEmployeeId").val();
  var documentId = $("#inpDocumentId").val();
  document.getElementById("DownloadAttachmentFrame").src = contextPath + "/sa.elm.ob.hcm.ad_forms.documents.ajax/DocumentsAjax?inpAction=Download&inpEmployeeId=" + employeeId + "&inpDocumentId=" + documentId + "&inpFileName=" + $('#inpFileName').val() + "&inpPath=" + $('#inpPath').val();
}

function onClickRefresh() {
  document.getElementById("inpAction").value = "EditView";
  reloadTab('DOC');

  if (changesFlag == 1) OBAsk('<%= Resource.getProperty("hcm.changedvaluessave", lang) %>', function (result) {
    if (result) {
      if (checkValidData()) {
        document.getElementById("SubmitType").value = "Save";
        document.getElementById("inpAction").value = "EditView";
        reloadTab(currentTab);
      }
    } else {
      document.getElementById("inpAction").value = "EditView";
      reloadTab(currentTab);
    }
  });
  else reloadTab('DOC');
}

function resetTab() {

  resetByTab(document.getElementById("TabDocument"));
}

function resetByTab(tab) {
  tab.className = "tabNotSelected";
  //image.className = "imageNotSelected";
  tab.parentNode.replaceChild(tab.cloneNode(true), tab);
  $('#' + tab.id).attr('onmouseover', "").attr('onmouseout', "").attr('onclick', "");
}

function enableTabs() {
  var nextLink = document.getElementById("NextLink");
  nextLink.style.opacity = '1';
  nextLink.style.filter = 'alpha(opacity=100)';
  nextLink.style.cursor = 'pointer';
  if (nextLink.attachEvent) nextLink.attachEvent('onclick', function () {
    reloadWindow('ID')
  });
  else nextLink.addEventListener('click', function () {
    reloadWindow('ID')
  }, false);

  var tab = null,
      image = null;

  tab = document.getElementById("TabEmployee");
  image = document.getElementById("ImgEmpInfo");
  tab.className = "tabSelected";
  image.className = "imageSelected";
  if (tab.attachEvent) {
    tab.attachEvent('onclick', function () {
      reloadWindow('ID')
    });
    tab.attachEvent('onmouseover', function () {
      setTabClass(document.getElementById("TabEmployee"), true);
    });
    tab.attachEvent('onmouseout', function () {
      setTabClass(document.getElementById("TabEmployee"), false);
    });
  } else tab.addEventListener('click', function () {
    reloadWindow('ID')
  }, false);

  tab = document.getElementById("TabDependent");
  image = document.getElementById("ImgDependent");
  tab.className = "tabSelected";
  image.className = "imageSelected";
  if (tab.attachEvent) {
    tab.attachEvent('onclick', function () {
      reloadWindow('Dependent')
    });
    tab.attachEvent('onmouseover', function () {
      setTabClass(document.getElementById("TabDependent"), true);
    });
    tab.attachEvent('onmouseout', function () {
      setTabClass(document.getElementById("TabDependent"), false);
    });
  } else tab.addEventListener('click', function () {
    reloadWindow('Dependent')
  }, false);
  tab = document.getElementById("TabEmpInfo");
  image = document.getElementById("ImgDependent");
  tab.className = "tabSelected";
  image.className = "imageSelected";
  if (tab.attachEvent) {
    tab.attachEvent('onclick', function () {
      reloadTab('EMPINF')
    });
    tab.attachEvent('onmouseover', function () {
      setTabClass(document.getElementById("TabEmpInfo"), true);
    });
    tab.attachEvent('onmouseout', function () {
      setTabClass(document.getElementById("TabEmpInfo"), false);
    });
  } else tab.addEventListener('click', function () {
    reloadTab('EMPINF')
  }, false);

}

function setTabClass(tab, type) {
  if (type == true) tab.className = "tabSelectedHOVER";
  else tab.className = "tabSelected";
}


function reloadWindow(tab) {
  //hideMessage();
  if (changesFlag == 1 && checkValidData()) OBAsk(changedvaluessave, function (result) {
    if (result) reloadTabSave(tab);
    else {
      reloadTab(tab);
    }
  });
  else reloadTab(tab);
}

function reloadTab(tab) {
  if (tab == 'EMP') {
	  var url= "&inpEmployeeStatus=" + document.getElementById("inpEmployeeStatus").value + "&inpEmpStatus=" + document.getElementById("inpEmpStatus").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView&inpEmployeeId=' + $("#inpEmployeeId").val()+url;
    document.frmMain.submit();
  }
  else if (tab == 'EMPCTRCT') {
	    var url = "";
	    var employeeId = document.getElementById("inpEmployeeId").value;
	    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract?inpAction=GridView&inpEmployeeId=' + employeeId;
	    document.frmMain.submit();
	  } 
  else if (tab == 'EMPQUAL') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'EMPINF') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'Dependent') {
    //document.getElementById("inpAction").value = "GridView";
    //document.getElementById("inpIsActive").value = "";
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpEmployeeId=' + $("#inpEmployeeId").val(), '_self', null, true);
  } else if (tab == 'EMPADD') {
   var employeeId = document.getElementById("inpEmployeeId").value;
   var inpAddressId = document.getElementById("inpAddressId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView&inpEmployeeId=' + employeeId+'&inpAddressId='+inpAddressId;
    document.frmMain.submit();
  } else if (tab == 'DOC') {
    var attach = $("#inpDocumentId").val();
    var employeeId = $("#inpEmployeeId").val();
    var url = '&inpDocType=' + $("#inpDocType").val() + '&inpIssuedDate=' + $("#inpIssuedDate").val() + '&inpFileName=' + $("#inpFileName").val();
    url += '&inpValidDate=' + $("#inpValidDate").val() + '&inpIsOriginal=' + $("#inpIsOriginal").val() ;
    url += '&SubmitType=' + $("#SubmitType").val();
    url += '&inpDocumentId=' + attach;
    url += '&inpEmployeeId=' + employeeId;
    url += '&inpEmpStatus=' + $("#inpEmpStatus").val();
    url += '&inpEmployeeStatus=' + $("#inpEmployeeStatus").val();
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents?inpAction=' + $("#inpAction").val() + url;
    document.frmMain.submit();
/*submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
         return false;*/
  } else if (tab == 'PREEMP') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment?inpEmployeeId=' + $("#inpEmployeeId").val(), '_self', null, true);
    return false;
  } else if (tab == 'Asset') {
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  }
  else if (tab == 'PERPAYMETHOD') {
      submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.personalpaymentmethod.header/PersonalPaymentMethod?inpEmployeeId=' + $("#inpEmployeeId").val(), '_self', null, true);
       return false;
  }
  else if (tab == 'MEDIN') {
	 	 var employeeId=document.getElementById("inpEmployeeId").value;
    	     document.frmMain.action =  contextPath + '/sa.elm.ob.hcm.ad_forms.MedicalInsurance.header/MedicalInsurance?inpAction=GridView&inpEmployeeId='+employeeId;
         document.frmMain.submit();
  }
  return false;
}

function reloadTabSave(tab) {
  if (checkValidData()) {
    var attach = $("#inpDocumentId").val();
    var employeeId = $("#inpEmployeeId").val();
    var url = '&inpDocType=' + $("#inpDocType").val() + '&inpIssuedDate=' + $("#inpIssuedDate").val() + '&inpFileName=' + $("#inpFileName").val();
    url += '&inpValidDate=' + $("#inpValidDate").val() + '&isOriginal=' + $("#inpIsOriginal").val() ;
    url += '&SubmitType=Save';
    url += '&inpDocumentId=' + attach;
    url += '&inpEmployeeId=' + employeeId;
    url += '&inpNextTab=' + tab;
    url += '&inpEmpStatus=' + $("#inpEmpStatus").val();
    url += '&inpEmployeeStatus=' + $("#inpEmployeeStatus").val();
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents?inpAction=' + $("#inpAction").val() + url;
    document.frmMain.submit();
  }
}

function onClickGrid() {
  document.getElementById("inpAction").value = "GridView";
  document.getElementById("inpDocumentId").value = "";

  if (changesFlag == 1) OBAsk(changedvaluessave, function (result) {
    if (result) reloadTab("DOC");
    else {
      reloadTab("DOC");
    }
  });
  else reloadTab('DOC');
}

function onClickSave(index, type) {
  if (index.className != 'Main_ToolBar_Button') return false;
  if (changesFlag == 1 && checkValidData()) {
    if (type == "Grid") {
      document.getElementById("inpAction").value = "GridView";
      document.getElementById("SubmitType").value = "SaveGrid";
      tab = "DOC";
    }
    if (type == "New") {
      document.getElementById("inpAction").value = "EditView";
      document.getElementById("SubmitType").value = "SaveNew";
      tab = "DOC";
    }
    if (type == "Save") {
      document.getElementById("inpAction").value = "EditView";
      document.getElementById("SubmitType").value = "Save";
      tab = "DOC";
    }
    reloadTab(tab);
  }
}

function checkValidData() {
  var rel = $("#inpDocType option:selected").val();
  if ($("#inpDocType option:selected").val() == '0') {
    OBAlert(seldoctype);
    document.getElementById("inpDocType").focus();
    return false;
  }
  var filename = $("#inpFileName").val();
  if (filename == "") {
    OBAlert(selectdoc);
    return false;
  }
  var IssuedDate = document.getElementById("inpIssuedDate").value;
  var isOriginalorDuplicate = document.getElementById('inpIsOriginal').value;
  if(IssuedDate == "" || isOriginalorDuplicate == ""){
      return false;
  }
  
  return true;
}