/*
 *************************************************************************
 * All Rights Reserved.
 * Contributor(s): Gopalakrishnan
 *************************************************************************
 */

var xmlhttp = new getXMLObject(),
    currentTab = 'EMPCTRCT';
var searchFlag = 0,
    onSearch = 0,
    onDelete = 0,
    gridW, gridH;
var formId = "748014FEDECF44D3BA89EDAB65573FE7";
var salaryGrid = jQuery("#SalaryList"),
    lastActiveType = 'Y',
    CheckPeriod = 'N',
    checkDate = 'N',
    checkStartDate = 'N';
var url = contextPath + "/ContractAjax?action=getGrade";
var changesFlag = 0;
$('#inpContractType').val(contractType);
$('#inpDurationType').val(durationType);

$(document).ready(function () {
	alert("Gopal Testing");
  if ($('#inpEmploymentGrpId').val() != "") {
    $('#jqgrid').show();
    $('#SalaryGroup').show();
  }
  if ($('#inpEmploymentGrpId').val() == '') {
    $('#inpissueDecision').hide();
  }
  if ($('#inpTrxStatus').val() == ' Issued ') {
    $("#inpGrade").next().attr("disabled", "disabled");
    $("#inpGrade").next().next().children().attr("disabled", "disabled");
    document.getElementById("inpGrade").setAttribute("disabled", "true");
    document.getElementById("inpJobNo").setAttribute("disabled", "true");
    document.getElementById("inpContractType").setAttribute("disabled", "true");
    document.getElementById("inpContractNo").setAttribute("disabled", "true");
    document.getElementById("inpDuration").setAttribute("disabled", "true");
    document.getElementById("inpDurationType").setAttribute("disabled", "true");
    document.getElementById("inpJobDescription").setAttribute("disabled", "true");
    document.getElementById("inpContractDesc").setAttribute("disabled", "true");
    document.getElementById("inpletterNo").setAttribute("disabled", "true");
    document.getElementById("inpDecisionNo").setAttribute("disabled", "true");
    document.getElementById("inpLettrDate").setAttribute("disabled", "true");
    document.getElementById("inpDecisionDate").setAttribute("disabled", "true");
    document.getElementById("inpStartDate").setAttribute("disabled", "true");
    document.getElementById("inpAnnualBalance").setAttribute("disabled", "true");
    document.getElementById("inpissueDecision").setAttribute("style", "display: none;");
  }

});

$.getJSON(url, function (result) {

}).done(function (data) {

  $("#inpGrade").find("option").remove().end();
  $("#inpGrade").append("<option value='0' selected>select</option>");
  //	$("#inpOrg").append("<option value='0'>*</option>");
  $.each(data, function (i, item) {
    $("#inpGrade").append("<option value='" + item.gradeId + "'>" + item.gradeCode + "</option>");
    if (gradeId === item.gradeId) $('#inpGrade').next().val(item.gradeCode);
  });
  if (gradeId != null && gradeId != '' && gradeId != 'null') $('#inpGrade').val(gradeId);

});

/*
 * load Job No
 */
var url = contextPath + "/ContractAjax?action=getJobNo&gradeId=" + gradeId;
$.getJSON(url, function (result) {

}).done(function (data) {

  $("#inpJobNo").find("option").remove().end();

  $("#inpJobNo").append("<option value='0' selected>select</option>");
  $.each(data, function (i, item) {
    $("#inpJobNo").append("<option value='" + item.jobId + "'>" + item.jobNo + "</option>");
  });
  if (jobNo != null && jobNo != '' && jobNo != 'null') $("#inpJobNo").val(jobNo);

});

function generateJob(gradeId) {
  //load Job No
  var url = contextPath + "/ContractAjax?action=getJobNo&gradeId=" + gradeId;
  $.getJSON(url, function (result) {

  }).done(function (data) {
    $("#inpJobNo").find("option").remove().end();

    $("#inpJobNo").append("<option value='0' selected>select</option>");
    $.each(data, function (i, item) {
      $("#inpJobNo").append("<option value='" + item.jobId + "'>" + item.jobNo + "</option>");
    });
    if (jobNo != null && jobNo != '' && jobNo != 'null') {
      $("#inpJobNo").val(jobNo);
    }

  });
}

//Load Salary Details Grid
var salaryUrl = "&inpEmploymentGrpId=" + document.getElementById("inpEmploymentGrpId").value;
salaryUrl += "&inpEmployeeId=" + document.getElementById("inpEmployeeId").value;
jQuery(function () {
  salaryGrid.jqGrid({
    url: contextPath + '/ContractAjax?action=GetSalaryDetails' + salaryUrl,
    colNames: ['Element Name', 'Value', 'Percentage'],
    colModel: [{
      name: 'element',
      index: 'element',
      width: 130,
      editable: true,
      editoptions: {
        readonly: "readonly",
        defaultValue: 'Sample'
      },
      searchoptions: {
        clearSearch: true,
        attr: {
          size: 90,
          maxlength: 90,
          style: "width:300px;margin-top:1px;"
        }
      }
    }, {
      name: 'value',
      index: 'value',
      width: 40,
      editable: true,
      formatter: 'integer'
    }, {
      name: 'Percentage',
      index: 'Percentage',
      width: 30,
      editable: true,
      formatter: 'integer'
    }],
    rowNum: 50,
    mtype: 'POST',
    rowList: [20, 50, 100, 200, 500],
    pager: '#pager',
    sortname: 'value',
    datatype: 'xml',
    rownumbers: true,
    viewrecords: true,
    sortorder: "asc",
    scrollOffset: 17,
    editurl: contextPath + '/ContractAjax?action=SaveSalaryDetails' + salaryUrl,
    loadComplete: function () {
      $("#pager_left").css("width", "");
      if ($('#inpTrxStatus').val() == ' Issued ') {
        $("#del_SalaryList").addClass('ui-state-disabled');
        $("#refresh_SalaryList").addClass('ui-state-disabled');
        $("#SalaryList_iladd").addClass('ui-state-disabled');
        $("#SalaryList_iledit").addClass('ui-state-disabled');
      }
    }
/*        beforeSubmit : function(postData) {
        	alert($("#inpEmploymentGrpId").val());
        	if($("#inpEmploymentGrpId").val()==""){
        		alert("error");
         	}
        }*/
  });
  salaryGrid.jqGrid('navGrid', '#pager', {
    edit: false,
    add: false,
    del: true,
    search: false,
    view: false
  }, {}, {}, {}, {});
  salaryGrid.jqGrid('inlineNav', '#pager');

  salaryGrid.jqGrid('filterToolbar', {
    searchOnEnter: false
  });
  salaryGrid[0].triggerToolbar();
  changeJQGridDisplay("SalaryList", "inpEmploymentGrpId");
});

function hideMessage() {
  $("#messageBoxID").hide();
}

function enableForm() {
  changesFlag = 1;
  hideMessage();
  if ($("#inpContractType").val() != '0' && $("#inpContractNo").val() != '' && $("#inpStartDate").val() != '' && $("#inpDuration").val() != '' && $("#inpDurationType").val() != '0' && $("#inpDurationType").val() != '0' && $("#inpletterNo").val() != '' && $("#inpLettrDate").val() != '') {
    enableSaveButton("true");
  } else {
    enableSaveButton("false");
  }
}

function enableSaveButton(flag) {
  if (flag == 'true' && document.getElementById('linkButtonSave_Relation').className != 'Main_ToolBar_Button') {
    document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation';
    document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New';
    document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save';
    $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button");
  } else if (flag == 'false') {
    document.getElementById('buttonSave_Relation').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_Relation_disabled';
    document.getElementById('buttonSave_New').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_New_disabled';
    document.getElementById('buttonSave').className = 'Main_ToolBar_Button_Icon Main_ToolBar_Button_Icon_Save_disabled';
    $('#linkButtonSave_Relation, #linkButtonSave_New, #linkButtonSave').attr("class", "Main_ToolBar_Button_disabled");
  }
}

function reSizeGrid() {
  if (window.innerWidth) {
    gridW = window.innerWidth - 52;
    gridH = window.innerHeight - 241;
  } else if (document.body) {
    gridW = document.body.clientWidth - 52;
    gridH = document.body.clientHeight - 241;
  }
  if (onSearch == 1) {
    gridH = gridH - 23;
    if (navigator.userAgent.toLowerCase().indexOf("webkit") != -1) gridH++;
  } else if (parseInt(document.getElementById("client").scrollHeight) + 77 > parseInt(document.body.clientHeight)) gridW = gridW - 14;
  if (gridW < 800) gridW = 500;
  if (gridH < 200) gridH = 100;
  salaryGrid.setGridWidth(540, true);
  salaryGrid.setGridHeight(130, true);
}


function onClickRefresh() {
  hideMessage();
  document.getElementById("inpAction").value = "EditView";
  if (changesFlag == 1 && $("#inpContractType").val() != '0' && $("#inpContractNo").val() != '' && $("#inpStartDate").val() != '' && $("#inpDuration").val() != '' && $("#inpDurationType").val() != '0' && $("#inpDurationType").val() != '0' && $("#inpletterNo").val() != '' && $("#inpLettrDate").val() != '') OBAsk(changevalueask, function (result) {
    if (result) {
      if (preValidation()) {
        reloadTabSave(currentTab);
      }
    } else {
      reloadTab(currentTab);
    }
  });
  else reloadTab('EMPCTRCT');
}

function reloadTabSave(tab) {
  var url = "";
  document.getElementById("inpNextTab").value = tab;
  document.getElementById("SubmitType").value = "Save";
  document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract' + url;
  document.frmMain.submit();
}

function preValidation() {
  var hireDate = $("#inpHireDate").val(),
      startDate = $("#inpStartDate").val(),
      todayDate = $("#inptodayDate").val();

  var fdate = hireDate.split("-");
  var fromdate = new Date();

  fromdate.setDate(fdate[0]);
  fromdate.setMonth(fdate[1] - 1);
  fromdate.setFullYear(fdate[2]);


  var tdte = startDate.split("-");
  var todate = new Date();

  todate.setDate(tdte[0]);
  todate.setMonth(tdte[1] - 1);
  todate.setFullYear(tdte[2]);

  var strTodaydte = todayDate.split("-");
  var datetoday = new Date();

  datetoday.setDate(strTodaydte[0]);
  datetoday.setMonth(strTodaydte[1] - 1);
  datetoday.setFullYear(strTodaydte[2]);


  if (fromdate.getTime() > todate.getTime()) {
    OBAlert("Start Date should Be greater Than The Hire Date");
    checkDate = 'Y';
  } else {
    checkDate = 'N';
  }


  if (todate.getTime() > datetoday.getTime()) {
    OBAlert("Start Date should Be lesser Than Today");
    checkStartDate = 'Y';
  } else {
    checkStartDate = 'N';
  }



  $.ajax({
    type: 'GET',
    url: contextPath + '/ContractAjax?action=checkPeriod',
    data: {
      inpStartDate: document.getElementById("inpStartDate").value,
      inpEndDate: document.getElementById("inpEndDate").value,
      inpEmployeeId: document.getElementById("inpEmployeeId").value,
      inpEmploymentGrpId: document.getElementById("inpEmploymentGrpId").value
    },
    dataType: 'json',
    async: false
  }).done(function (response) {
    CheckPeriod = response.isExists;
  });
  if (CheckPeriod == 'Y') {
    OBAlert('Contract Alreay Exists for the same period');
    CheckPeriod = 'Y';
  } else {
    CheckPeriod = 'N';
  }
  if (checkDate == 'Y' || CheckPeriod == 'Y' || checkStartDate == 'Y') {
    return false;
  } else {
    return true;
  }
}

function onClickSave(index, type) {
  if (preValidation()) {
    if (index.className != 'Main_ToolBar_Button') return false;
    if (changesFlag == 1) {
      if (type == "Grid") {
        showProcessBar(true, 2);
        document.getElementById("inpAction").value = "GridView";
        document.getElementById("SubmitType").value = "Save";
      }
      if (type == "New") {
        showProcessBar(true, 2);
        document.getElementById("inpAction").value = "EditView";
        document.getElementById("SubmitType").value = "SaveNew";
      }
      if (type == "Save") {
        // showProcessBar(true, 2);
        document.getElementById("inpAction").value = "EditView";
        document.getElementById("SubmitType").value = "Save";

      }
      reloadTab('EMPCTRCT');
    }
  }

}

function onClickGridView() {
  hideMessage();
  document.getElementById("inpAction").value = "GridView";
  if (changesFlag == 1 && $("#inpContractType").val() != '0' && $("#inpContractNo").val() != '' && $("#inpStartDate").val() != '' && $("#inpDuration").val() != '' && $("#inpDurationType").val() != '0' && $("#inpDurationType").val() != '0' && $("#inpletterNo").val() != '' && $("#inpLettrDate").val() != '') OBAsk(changevalueask, function (result) {
    if (result) {
      if (preValidation()) {
        reloadTabSave(currentTab);
      }
    } else {
      reloadTab(currentTab);
    }
  });
  else reloadTab('EMPCTRCT');
}

function reloadFunction(tab) {
  hideMessage();
  if (changesFlag == 1 && $("#inpContractType").val() != '0' && $("#inpContractNo").val() != '' && $("#inpStartDate").val() != '' && $("#inpDuration").val() != '' && $("#inpDurationType").val() != '0' && $("#inpDurationType").val() != '0' && $("#inpletterNo").val() != '' && $("#inpLettrDate").val() != '') OBAsk(changevalueask, function (result) {
    if (result) {
      if (preValidation()) {
        reloadTabSave(tab);
      }
    } else {
      reloadTab(tab);
    }
  });
  else reloadTab(tab);

}

function onClickNew() {
  document.getElementById("inpAction").value = "EditView";
  if (changesFlag == 1 && $("#inpContractType").val() != '0' && $("#inpContractNo").val() != '' && $("#inpStartDate").val() != '' && $("#inpDuration").val() != '' && $("#inpDurationType").val() != '0' && $("#inpDurationType").val() != '0' && $("#inpletterNo").val() != '' && $("#inpLettrDate").val() != '') OBAsk(changevalueask, function (result) {
    if (result) {
      if (preValidation()) {
        reloadTabSave(currentTab);
      }
    } else {
      document.getElementById("inpEmploymentGrpId").value = '';
      document.getElementById("inpAction").value = "EditView";
      submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
      return false;
    }
  });
  else {
    document.getElementById("inpEmploymentGrpId").value = '';
    document.getElementById("inpAction").value = "EditView";
    submitCommandForm('DEFAULT', true, null, 'Contract', '_self', null, true);
    return false;
  }
}

function reloadTab(tab) {
  var url = "";
  if (tab == 'EMP') {
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employee.header/Employee?inpAction=EditView' + url;
    document.frmMain.submit();
  } else if (tab == 'EMPINF') {
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employment.header/Employment' + url;
    document.frmMain.submit();
  } else if (tab == 'Dependent') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.dependents.header/Dependents?inpAction=GridView&inpEmployeeId=' + employeeId;
    document.frmMain.submit();
  } else if (tab == 'Qualification') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.qualification.header/Qualification', '_self', null, true);
    return false;
  } else if (tab == 'EMPADD') {
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.employeeaddress.header/EmployeeAddress?inpAction=EditView';
    document.frmMain.submit();
  } else if (tab == 'EMPCTRCT') {
    var url = "";
    var employeeId = document.getElementById("inpEmployeeId").value;
    document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract' + url;
    document.frmMain.submit();
  } else if (tab == 'Asset') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.asset.header/Asset', '_self', null, true);
    return false;
  } else if (tab == 'PREEMP') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.preemp.header/PreviousEmployment', '_self', null, true);
    return false;
  } else if (tab == 'DOC') {
    submitCommandForm('DEFAULT', true, null, contextPath + '/sa.elm.ob.hcm.ad_forms.documents.header/Documents', '_self', null, true);
    return false;
  }
}
//issuance

function IssueDecision() {


  if ($("#inpContractType").val() == '0' || $("#inpContractNo").val() == '' || $("#inpStartDate").val() == '' || $("#inpDuration").val() == '' || $("#inpDurationType").val() == '0' || $("#inpDurationType").val() == '0' || $("#inpletterNo").val() == '' && $("#inpLettrDate").val() != '') {
    OBAlert("Please Fill all Mandatory Fields to proceed");
    return false;
  } else {
    if (preValidation()) {
      OBAsk(askIssue, function (result) {
        if (result) {

          $.ajax({
            type: 'GET',
            url: contextPath + '/ContractAjax?action=Checkcontractvalidation',
            data: {
              duration: document.getElementById("inpDuration").value,
              inpEmployeeId: document.getElementById("inpEmployeeId").value,
              inpEmploymentGrpId: document.getElementById("inpEmploymentGrpId").value
            },
            dataType: 'json',
            async: false
          }).done(function (response) {

            Checkcontractvalidation = response.isExists;

          });
          if (Checkcontractvalidation == 'Y') {

            var a = document.getElementById("inpminconser").value;

            var b = document.getElementById("inpmaxconser").value;

            OBAlert("Contract service should be inbetween " + $('#inpminconser').val() + " to " + $('#inpmaxconser').val() + " years ");
            CheckPeriod = 'Y';
            return false;
          } else {
            var url = "";
            document.getElementById("SubmitType").value = "IssueDecision";
            document.getElementById("inpTrxStatus").value = "ISS";
            var employeeId = document.getElementById("inpEmployeeId").value;
            document.frmMain.action = contextPath + '/sa.elm.ob.hcm.ad_forms.contract.header/Contract' + url;
            document.frmMain.submit();
          }

        } else {
          return false;
        }
      });

    }
  }

}

