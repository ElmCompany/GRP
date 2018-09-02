package sa.elm.ob.hcm.dto.manager;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class EmpBusinessMissionManagerDTO extends GenericDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4607285417840661701L;

    private String empNo;
    private String empName;
    private String missionCategory;
    private String startDate;
    private String endDate;
    private Integer balance;
    private String originalDecisionNo;

    public String getOriginalDecisionNo() {
	return originalDecisionNo;
    }

    public void setOriginalDecisionNo(String originalDecisionNo) {
	this.originalDecisionNo = originalDecisionNo;
    }

    public String getEmpNo() {
	return empNo;
    }

    public void setEmpNo(String empNo) {
	this.empNo = empNo;
    }

    public String getEmpName() {
	return empName;
    }

    public void setEmpName(String empName) {
	this.empName = empName;
    }

    public String getMissionCategory() {
	return missionCategory;
    }

    public void setMissionCategory(String missionCategory) {
	this.missionCategory = missionCategory;
    }

    public String getStartDate() {
	return startDate;
    }

    public Integer getBalance() {
	return balance;
    }

    public void setBalance(Integer balance) {
	this.balance = balance;
    }

    public void setStartDate(String startDate) {
	this.startDate = startDate;
    }

    public String getEndDate() {
	return endDate;
    }

    public void setEndDate(String endDate) {
	this.endDate = endDate;
    }

}
