package sa.elm.ob.hcm.ad_forms.MedicalInsurance.vo;

/**
 * 
 * @author Priyanka Ranjan 17-03-2018
 *
 */
public class MedicalInsuranceVO {
  private String searchkey;
  private String name;
  private String dependents;
  private String insuranceCompanyName;
  private String insuranceSchema;
  private String memberShipNo;
  private String startDate;
  private String endDate;
  private String employee;
  private String insuranceCategory;
  private String insuranceCategoryKey;
  private String insuranceSchemaId;
  private String dependentId;
  private String relationship;

  public String getInsuranceSchemaId() {
    return insuranceSchemaId;
  }

  public void setInsuranceSchemaId(String insuranceSchemaId) {
    this.insuranceSchemaId = insuranceSchemaId;
  }

  public String getInsuranceCategoryKey() {
    return insuranceCategoryKey;
  }

  public void setInsuranceCategoryKey(String insuranceCategoryKey) {
    this.insuranceCategoryKey = insuranceCategoryKey;
  }

  public String getSearchkey() {
    return searchkey;
  }

  public String getDependents() {
    return dependents;
  }

  public void setDependents(String dependents) {
    this.dependents = dependents;
  }

  public String getInsuranceCompanyName() {
    return insuranceCompanyName;
  }

  public void setInsuranceCompanyName(String insuranceCompanyName) {
    this.insuranceCompanyName = insuranceCompanyName;
  }

  public String getInsuranceSchema() {
    return insuranceSchema;
  }

  public void setInsuranceSchema(String insuranceSchema) {
    this.insuranceSchema = insuranceSchema;
  }

  public String getMemberShipNo() {
    return memberShipNo;
  }

  public void setMemberShipNo(String memberShipNo) {
    this.memberShipNo = memberShipNo;
  }

  public String getStartDate() {
    return startDate;
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

  public String getEmployee() {
    return employee;
  }

  public void setEmployee(String employee) {
    this.employee = employee;
  }

  public String getInsuranceCategory() {
    return insuranceCategory;
  }

  public void setInsuranceCategory(String insuranceCategory) {
    this.insuranceCategory = insuranceCategory;
  }

  public void setSearchkey(String searchkey) {
    this.searchkey = searchkey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRelationship() {
    return relationship;
  }

  public void setRelationship(String relationship) {
    this.relationship = relationship;
  }

  public String getDependentId() {
    return dependentId;
  }

  public void setDependentId(String dependentId) {
    this.dependentId = dependentId;
  }

}
