package sa.elm.ob.hcm.dto.businessTrips;

import java.io.Serializable;

import sa.elm.ob.hcm.selfservice.dto.GenericDTO;

public class BusinessTripRequestDTO extends GenericDTO implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -8091756958587893165L;
  private BusinessPaymentDTO paymentDetails;
  private String letterNo;
  private String letterDate;
  private String missionType;
  private String missionCategory;
  private String fromCountry;
  private String fromCity;
  private String toCountry;
  private String toCity;
  private Integer missionDays;
  private String startDate;
  private String endDate;
  private Integer noOfDaysBefore;
  private Integer noOfDaysAfter;
  private Boolean housingProvided;
  private Boolean foodProvided;
  private Boolean ticketsProvided;
  private Boolean roundTrip;
  private String taskDescription;
  private String orginalDecNo;

  public BusinessPaymentDTO getPaymentDetails() {
    return paymentDetails;
  }

  public void setPaymentDetails(BusinessPaymentDTO paymentDetails) {
    this.paymentDetails = paymentDetails;
  }

  public String getLetterNo() {
    return letterNo;
  }

  public void setLetterNo(String letterNo) {
    this.letterNo = letterNo;
  }

  public String getLetterDate() {
    return letterDate;
  }

  public void setLetterDate(String letterDate) {
    this.letterDate = letterDate;
  }

  public String getMissionType() {
    return missionType;
  }

  public void setMissionType(String missionType) {
    this.missionType = missionType;
  }

  public String getMissionCategory() {
    return missionCategory;
  }

  public void setMissionCategory(String missionCategory) {
    this.missionCategory = missionCategory;
  }

  public String getFromCountry() {
    return fromCountry;
  }

  public void setFromCountry(String fromCountry) {
    this.fromCountry = fromCountry;
  }

  public String getFromCity() {
    return fromCity;
  }

  public void setFromCity(String fromCity) {
    this.fromCity = fromCity;
  }

  public String getToCountry() {
    return toCountry;
  }

  public void setToCountry(String toCountry) {
    this.toCountry = toCountry;
  }

  public String getToCity() {
    return toCity;
  }

  public void setToCity(String toCity) {
    this.toCity = toCity;
  }

  public Integer getMissionDays() {
    return missionDays;
  }

  public void setMissionDays(Integer missionDays) {
    this.missionDays = missionDays;
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

  public Integer getNoOfDaysBefore() {
    return noOfDaysBefore;
  }

  public void setNoOfDaysBefore(Integer noOfDaysBefore) {
    this.noOfDaysBefore = noOfDaysBefore;
  }

  public Integer getNoOfDaysAfter() {
    return noOfDaysAfter;
  }

  public void setNoOfDaysAfter(Integer noOfDaysAfter) {
    this.noOfDaysAfter = noOfDaysAfter;
  }

  public Boolean getHousingProvided() {
    return housingProvided;
  }

  public void setHousingProvided(Boolean housingProvided) {
    this.housingProvided = housingProvided;
  }

  public Boolean getFoodProvided() {
    return foodProvided;
  }

  public void setFoodProvided(Boolean foodProvided) {
    this.foodProvided = foodProvided;
  }

  public Boolean getTicketsProvided() {
    return ticketsProvided;
  }

  public void setTicketsProvided(Boolean ticketsProvided) {
    this.ticketsProvided = ticketsProvided;
  }

  public Boolean getRoundTrip() {
    return roundTrip;
  }

  public void setRoundTrip(Boolean roundTrip) {
    this.roundTrip = roundTrip;
  }

  public String getTaskDescription() {
    return taskDescription;
  }

  public void setTaskDescription(String taskDescription) {
    this.taskDescription = taskDescription;
  }

  public String getOrginalDecNo() {
    return orginalDecNo;
  }

  public void setOrginalDecNo(String orginalDecNo) {
    this.orginalDecNo = orginalDecNo;
  }

}
