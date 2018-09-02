package sa.elm.ob.hcm.ad_forms.documents.vo;

public class DocumentsVO {
  private String id;
  private String doctype;
  private String issueddate;
  private String validdate;
  private String isoriginal;
  private String filename;
  private String path;
  private String Status;
  private String Code;
  private String Name;
  private String Doctypact;
  private String DoctypId;
  private String userName;
  private String userId;
  private String emailId;
  private int page;
  private int count;
  private int totalPages;
  private String message;
  private String result;

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public String getDoctypId() {
    return DoctypId;
  }

  public void setDoctypId(String doctypId) {
    DoctypId = doctypId;
  }

  public String getCode() {
    return Code;
  }

  public void setCode(String code) {
    Code = code;
  }

  public String getName() {
    return Name;
  }

  public void setName(String name) {
    Name = name;
  }

  public String getDoctypact() {
    return Doctypact;
  }

  public void setDoctypact(String doctypact) {
    Doctypact = doctypact;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDoctype() {
    return doctype;
  }

  public void setDoctype(String doctype) {
    this.doctype = doctype;
  }

  public String getIssueddate() {
    return issueddate;
  }

  public void setIssueddate(String issueddate) {
    this.issueddate = issueddate;
  }

  public String getValiddate() {
    return validdate;
  }

  public void setValiddate(String validdate) {
    this.validdate = validdate;
  }

  public String getIsoriginal() {
    return isoriginal;
  }

  public void setIsoriginal(String isoriginal) {
    this.isoriginal = isoriginal;
  }

  public String getStatus() {
    return Status;
  }

  public void setStatus(String status) {
    Status = status;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEmailId() {
    return emailId;
  }

  public void setEmailId(String emailId) {
    this.emailId = emailId;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

}
