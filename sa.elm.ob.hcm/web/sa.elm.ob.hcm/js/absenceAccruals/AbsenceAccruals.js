var absenceAccrualGrid = jQuery("#absenceAccrualList");


function reloadWindow() {
  submitCommandForm('DEFAULT', false, null, 'AbsenceAccruals', '_self', null, false);
  return false;
}

function onClickRefresh() {
  document.getElementById("inpAction").value = "";
  reloadWindow();
}

function checkDateformat(inpDate) {
  var dateformat = /^(0?[1-9]|[12][0-9]|3[01])[\/\-](0?[1-9]|1[012])[\/\-]\d{4}$/;
  if (inpDate != "") {
    if (inpDate.match(dateformat)) {

    } else {
      OBAlert(invalidDateFormat);
      return false;
    }
    return true;
  }
}

function reloadGrid() {
  absenceGrid.trigger("reloadGrid");
}

$("#inpEmployee").select2(selectBoxAjaxPaging({
  size: "small",
  placeholder: true
}));
$("#inpAbsenceType").select2(selectBoxAjaxPaging({
  size: "small",
  placeholder: true
}));

document.getElementById("select2-inpEmployee-container").style.backgroundColor = "#F5F7F1";
document.getElementById("select2-inpAbsenceType-container").style.backgroundColor = "#F5F7F1";
var absenceGrid = jQuery("#absenceAccrualList");

jQuery(function () {
  absenceGrid.jqGrid({
    direction: direction,
    url: contextPath + '/sa.elm.ob.hcm.ad_process.AbsenceAccrual/AbsenceAccruals?inpAction=getAccrualList',
    colNames: [employee, department, category, absenceType, subtype, fromDate, toDate, entitlement, leaves, netentitlement],
    colModel: [{
      name: 'employee',
      index: 'employee',
      search: false,
      width: 60
    }, {
      name: 'dept',
      index: 'dept',
      search: false,
      width: 60
    }, {
      name: 'category',
      index: 'category',
      search: false,
      hidden: true,
      width: 60
    }, {
      name: 'absence',
      index: 'absence',
      search: false,
      width: 60
    }, {
      name: 'subType',
      index: 'subType',
      sortable: false,
      search: false,
      width: 60
    }, {
      name: 'startdate',
      index: 'startdate',
      sortable: false,
      search: false,
      width: 60
    }, {
      name: 'enddate',
      index: 'enddate',
      sortable: false,
      search: false,
      width: 60
    }, {
      name: 'entitlement',
      index: 'entitlement',
      sortable: false,
      search: false,
      width: 60
    }, {
      name: 'leaves',
      index: 'leaves',
      sortable: false,
      search: false,
      width: 60
    }, {
      name: 'netentitlement',
      index: 'netentitlement',
      sortable: false,
      search: false,
      width: 60
    }




    ],
    rowNum: 50,
    mtype: 'POST',
    rowList: [20, 50, 100, 200, 500],
    pager: '#absenceAccrualPager',
    sortname: 'employee',
    datatype: 'json',
    rownumbers: true,
    recordtext: "View {0} - {1}",
    viewrecords: true,
    viewpages: true,
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
      ChangeJQGridAllRowColor(absenceGrid);
      $("#absenceAccrualGrid").show();
    },

    beforeRequest: function () {
      absenceGrid.setPostDataItem("inpEmployee", document.getElementById("inpEmployee").value);
      absenceGrid.setPostDataItem("inpAbsenceType", document.getElementById("inpAbsenceType").value);
      absenceGrid.setPostDataItem("inpCalculationDate", document.getElementById("inpCalculationDate").value);
      absenceGrid.setPostDataItem("_search", 'true');

    }
  });
  absenceGrid.jqGrid('navGrid', '#absenceAccrualPager', {
    edit: false,
    add: false,
    del: false,
    search: false,
    view: false,
    refresh: false
  }, {}, {}, {}, {});

  absenceGrid.jqGrid('filterToolbar', {
    searchOnEnter: false
  });
  absenceGrid[0].triggerToolbar();
  changeJQGridDisplay("absenceAccrualList", "");
});

function reSizeGrid() {
  if (window.innerWidth) {
    gridW = window.innerWidth - 52;
    gridH = window.innerHeight - 260;
  } else if (document.body) {
    gridW = document.body.clientWidth - 52;
    gridH = document.body.clientHeight - 260;
  }
  if (gridW < 800) gridW = 800;
  if (gridH < 200) gridH = 200;
  absenceGrid.setGridWidth(gridW, true);
  absenceGrid.setGridHeight(gridH, true);
}

function getEmpDetails(value) {
	if(value!="0"){
  var url = contextPath + "/sa.elm.ob.hcm.ad_process.AbsenceAccrual/AbsenceAccruals?inpAction=getEmpDetails&employeeId=" + value;
  //load all fields
  $.ajax({
    url: url,
    type: 'GET',
    dataType: 'json',
    success: function (data) {
      document.getElementById("inpHireDate").value = data.hiredate;
      document.getElementById("inpPersonType").value = data.employeeType;
      document.getElementById("inpSectionCode").value = data.section;
      document.getElementById("inpGrade").value = data.positionGrade;
      document.getElementById("inpJobno").value = data.job;
      document.getElementById("inpEmpGrade").value = data.employmentGrade;
      document.getElementById("inpAssignedDept").value = data.assignedDept;
      document.getElementById("inpEmployeeCategory").value = data.inpEmployeeCategory;
      document.getElementById("inpStatus").options.length = 0;
      $("#inpStatus").append("<option value='" + data.inpempStatusCode + "'>" + data.inpempStatus + "</option>");


    }
  });
	}
	else{
		 document.getElementById("inpHireDate").value = "";
	      document.getElementById("inpPersonType").value = "";
	      document.getElementById("inpSectionCode").value = "";
	      document.getElementById("inpGrade").value = "";
	      document.getElementById("inpJobno").value = "";
	      document.getElementById("inpEmpGrade").value = "";
	      document.getElementById("inpAssignedDept").value = "";
	      document.getElementById("inpEmployeeCategory").value = "";
	      document.getElementById("inpStatus").options.length = 0;

	}
}