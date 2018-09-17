package sa.elm.ob.hcm.services.selfservicetransactions;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sa.elm.ob.hcm.GenericActivitiData;
import sa.elm.ob.hcm.ad_process.Constants;
import sa.elm.ob.hcm.dao.activiti.SelfServiceTransactionDAO;
import sa.elm.ob.hcm.dto.profile.AddressInformationDTO;
import sa.elm.ob.hcm.dto.profile.DependentInformationDTO;
import sa.elm.ob.hcm.dto.profile.PersonalInformationDTO;

@Service
public class SelfServiceTransactionServiceImpl implements SelfServiceTransactionService {

  @Autowired
  private SelfServiceTransactionDAO commonActivitiDAO;

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public AddressInformationDTO getAddress(String recordId) {

    AddressInformationDTO address = null;

    try {
      GenericActivitiData data = commonActivitiDAO.findTransactionRecord(recordId);
      JSONObject newAddress = new JSONObject(new String(data.getTemporaryData()));
      address = mapper.readValue(newAddress.toString(), AddressInformationDTO.class);

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }

    return address;
  }

  @Override
  public DependentInformationDTO getDependent(String recordId) {

    DependentInformationDTO dependent = null;

    try {
      GenericActivitiData data = commonActivitiDAO.findTransactionRecord(recordId);
      JSONObject newDependent = new JSONObject(new String(data.getTemporaryData()));
      dependent = mapper.readValue(newDependent.toString(), DependentInformationDTO.class);

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }

    return dependent;
  }

  @Override
  public PersonalInformationDTO getPersonalInformation(String recordId) {

    PersonalInformationDTO personalInfo = null;

    try {
      GenericActivitiData data = commonActivitiDAO.findTransactionRecord(recordId);
      JSONObject newPersonalInfo = new JSONObject(new String(data.getTemporaryData()));
      personalInfo = mapper.readValue(newPersonalInfo.toString(), PersonalInformationDTO.class);

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }

    return personalInfo;
  }

  @Override
  public Object getRequestDetails(String recordId) {

    try {
      GenericActivitiData storedData = commonActivitiDAO.findTransactionRecord(recordId);
      JSONObject data = new JSONObject(new String(storedData.getTemporaryData()));
      String type = storedData.getTypeOfActiviti();

      if (type.equals(Constants.UPDATE_ADDRESS)) {
        AddressInformationDTO address = mapper.readValue(data.toString(),
            AddressInformationDTO.class);
        return address;
      } else if (type.equals(Constants.UPDATE_DEPENDENT) || type.equals(Constants.ADD_DEPENDENT)) {
        DependentInformationDTO dependent = mapper.readValue(data.toString(),
            DependentInformationDTO.class);
        return dependent;
      } else if (type.equals(Constants.UPDATE_PERSONAL_INFORMATION)) {
        PersonalInformationDTO personalInfo = mapper.readValue(data.toString(),
            PersonalInformationDTO.class);
        return personalInfo;
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new OBException(e.getMessage());
    }
    return null;
  }

}
