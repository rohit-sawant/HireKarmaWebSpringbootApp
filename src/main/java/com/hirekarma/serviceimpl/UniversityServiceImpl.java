package com.hirekarma.serviceimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hirekarma.beans.AdminShareJobToUniversityBean;
import com.hirekarma.beans.AdminSharedJobList;
import com.hirekarma.beans.CampusDriveResponseBean;
import com.hirekarma.beans.StudentResponseToUniversity;
import com.hirekarma.beans.UniversityJobShareToStudentBean;
import com.hirekarma.exception.CampusDriveResponseException;
import com.hirekarma.exception.UniversityException;
import com.hirekarma.exception.UserProfileException;
import com.hirekarma.model.AdminShareJobToUniversity;
import com.hirekarma.model.CampusDriveResponse;
import com.hirekarma.model.Student;
import com.hirekarma.model.University;
import com.hirekarma.model.UniversityJobShareToStudent;
import com.hirekarma.model.UserProfile;
import com.hirekarma.repository.AdminShareJobToUniversityRepository;
import com.hirekarma.repository.CampusDriveResponseRepository;
import com.hirekarma.repository.CorporateRepository;
import com.hirekarma.repository.JobRepository;
import com.hirekarma.repository.ShareJobRepository;
import com.hirekarma.repository.StudentRepository;
import com.hirekarma.repository.UniversityJobShareRepository;
import com.hirekarma.repository.UniversityRepository;
import com.hirekarma.repository.UserRepository;
import com.hirekarma.service.UniversityService;

@Service("universityServiceImpl")
public class UniversityServiceImpl implements UniversityService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UniversityServiceImpl.class);

	@Autowired
	private ShareJobRepository shareJobRepository;

	@Autowired
	private CorporateRepository corporateRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private JobRepository jobRepository;

	@Autowired
	private UniversityRepository universityRepository;

	@Autowired
	private AdminShareJobToUniversityRepository adminShareJobToUniversityRepository;

	@Autowired
	private UniversityJobShareRepository universityJobShareRepository;

	@Autowired
	private CampusDriveResponseRepository campusDriveResponseRepository;

	@Override
	public Map<String, Object> universityResponse(AdminShareJobToUniversityBean jobBean) {

		AdminShareJobToUniversity adminShareJobToUniversity = new AdminShareJobToUniversity();
		Map<String, Object> response = new HashMap<String, Object>();

		try {
			LOGGER.debug("Inside UniversityServiceImpl.universityResponse(-)");

			if (!jobBean.getUniversityResponseStatus() && (jobBean.getRejectionFeedback() == null
					&& jobBean.getRejectionFeedback().equalsIgnoreCase("null")
					&& jobBean.getRejectionFeedback().equals(""))) {
				throw new UserProfileException("Rejected Reason Can't Be Blank !!");
			} else {

				Optional<AdminShareJobToUniversity> optional = shareJobRepository.findById(jobBean.getShareJobId());
				adminShareJobToUniversity = optional.get();

				if (adminShareJobToUniversity != null) {
					if (jobBean.getUniversityResponseStatus() != null) {
						adminShareJobToUniversity.setUniversityResponseStatus(jobBean.getUniversityResponseStatus());
						adminShareJobToUniversity.setRejectionFeedback(jobBean.getRejectionFeedback());
						adminShareJobToUniversity.setUpdatedOn(new Timestamp(new java.util.Date().getTime()));

						shareJobRepository.save(adminShareJobToUniversity);
					} else {
						throw new UniversityException("Response Value Can't Be Null !!");
					}

				}
			}

			response.put("result", adminShareJobToUniversity);
			LOGGER.info("Data Updated Successfully In UniversityServiceImpl.universityResponse(-)");

		} catch (Exception e) {

			LOGGER.info("Data Updatation Failed In UniversityServiceImpl.universityResponse(-)" + e);
			throw e;
		}

		return response;
	}

	@Override
	public Map<String, Object> shareJobStudent(UniversityJobShareToStudentBean universityJobShareToStudentBean)
			throws Exception {
		System.out.println("JobId:"+universityJobShareToStudentBean.getJobId());
		UniversityJobShareToStudent universityJobShareToStudent = null;
		UserProfile userProfile = null;
		List<UniversityJobShareToStudent> list = new ArrayList<UniversityJobShareToStudent>();
		Long count = 0L;

		Map<String, Object> response = new HashMap<String, Object>();

		List<Long> studentIdList = null;

		if ((String.valueOf(universityJobShareToStudentBean.getBatchId()).equalsIgnoreCase("null")
				|| universityJobShareToStudentBean.getBatchId() == null)
				&& (String.valueOf(universityJobShareToStudentBean.getBranchId()).equalsIgnoreCase("null")
						|| universityJobShareToStudentBean.getBranchId() == null)
				&& (String.valueOf(universityJobShareToStudentBean.getCgpaId()).equalsIgnoreCase("null")
						|| universityJobShareToStudentBean.getCgpaId() == null)) {

			studentIdList = studentRepository.getStudentList();
		} else {
			studentIdList = studentRepository.getStudentList(universityJobShareToStudentBean.getBatchId(),
					universityJobShareToStudentBean.getBranchId(), universityJobShareToStudentBean.getCgpaId());
		}

		System.out.println(universityJobShareToStudentBean.getBatchId()
				+ "***********************************\n\nTotal List \n\n\n" + studentIdList
				+ "\n\n*************************************");
		Long jobId = jobRepository.getJobById(universityJobShareToStudentBean.getJobId());

		try {
			LOGGER.debug("Inside UniversityServiceImpl.shareJobStudent(-)");

			if (jobId != 0) {

				if (studentIdList.size() != 0) {
					String[] chunks1 = universityJobShareToStudentBean.getToken().split(" ");
					String[] chunks = chunks1[1].split("\\.");
					Base64.Decoder decoder = Base64.getUrlDecoder();

					String payload = new String(decoder.decode(chunks[1]));
					JSONParser jsonParser = new JSONParser();
					Object obj = jsonParser.parse(payload);

					JSONObject jsonObject = (JSONObject) obj;

					String email = (String) jsonObject.get("sub");

					userProfile = userRepository.findByEmail(email, "university");

					if (userProfile != null) {

						if (universityJobShareToStudentBean != null) {

							for (int i = 0; i < studentIdList.size(); i++) {
								count++;
								universityJobShareToStudent = new UniversityJobShareToStudent();
								universityJobShareToStudent.setJobId(universityJobShareToStudentBean.getJobId());
								universityJobShareToStudent.setUniversityId(userProfile.getUserId());
								universityJobShareToStudent.setJobStatus(true);
								universityJobShareToStudent.setStudentId(studentIdList.get(i));
								universityJobShareToStudent.setCreatedBy("Biswa");
								universityJobShareToStudent.setCreatedOn(new Timestamp(new java.util.Date().getTime()));

								universityJobShareRepository.save(universityJobShareToStudent);
								list.add(universityJobShareToStudent);
							}

							LOGGER.info("Data Updated Successfully In UniversityServiceImpl.shareJobStudent(-)");
						}

						response.put("shareJob", list);
						response.put("totalSharedJob", count);
					} else {
						throw new UserProfileException("Invalid Token !!");
					}
				} else {
					throw new UserProfileException("Can't Found Any Student With This Filter !!");
				}
			} else {
				throw new UserProfileException("Can't Found Any Job !!");
			}

		} catch (Exception e) {
			LOGGER.info("Data Updatation Failed In UniversityServiceImpl.shareJobStudent(-)" + e);
			throw e;
		}

		return response;
	}

	@Override
	public CampusDriveResponseBean campusDriveRequest(CampusDriveResponseBean campus, String jwtToken)
			throws Exception {
		CampusDriveResponseBean driveResponseBean = new CampusDriveResponseBean();
		CampusDriveResponse driveResponse = null;
		University university = new University();
		try {
			LOGGER.debug("Inside UniversityServiceImpl.campusDriveRequest(-)");

			String[] chunks1 = jwtToken.split(" ");
			String[] chunks = chunks1[1].split("\\.");
			Base64.Decoder decoder = Base64.getUrlDecoder();

			String payload = new String(decoder.decode(chunks[1]));
			JSONParser jsonParser = new JSONParser();
			Object obj = jsonParser.parse(payload);

			JSONObject jsonObject = (JSONObject) obj;

			String email = (String) jsonObject.get("sub");

			university = universityRepository.findByEmail(email);
			Optional<?> optional = null;

			if (university != null) {

				optional = corporateRepository.findById(campus.getCorporateId());

				if (optional.isPresent()) {

					optional = null;
					optional = jobRepository.getJobDetails(campus.getJobId(), campus.getCorporateId());
//					optional = jobRepository.findById(campus.getJobId());

					if (optional.isPresent()) {

						Long campusList = campusDriveResponseRepository.findSharedCampus(university.getUniversityId(),
								campus.getCorporateId(), campus.getJobId());
						System.out.println("*********\n\n\n" + campusList + "\n\n\n************");

						Object object = shareJobRepository.getRequestVerificationDetails(university.getUniversityId(),
								campus.getJobId(), campus.getCorporateId());

						if (object != null) {

							if (campusList == 0) {

								driveResponse = new CampusDriveResponse();

								driveResponse.setJobId(campus.getJobId());
								driveResponse.setCorporateId(campus.getCorporateId());
								driveResponse.setUniversityId(university.getUniversityId());
								driveResponse.setUniversityAskedOn(new Timestamp(new java.util.Date().getTime()));
								driveResponse.setCorporateResponse(false);
								driveResponse.setUniversityAsk(true);

								campusDriveResponseRepository.save(driveResponse);

								BeanUtils.copyProperties(driveResponse, driveResponseBean);

								LOGGER.info(
										"Data Inserted Successfully In UniversityServiceImpl.campusDriveRequest(-)");
							} else {
								throw new CampusDriveResponseException(
										"Already Request Sended To This Perticular Corporate !!");
							}
						} else {
							throw new CampusDriveResponseException(
									"No Shared Job Found From This Corporate  To Your University !!");
						}
					} else {
						throw new CampusDriveResponseException("Job Details Not Found With This Corporate!!");
					}
				} else {
					throw new CampusDriveResponseException("Corporate Details Not Found!!");
				}
			} else {
				throw new CampusDriveResponseException("Something went wrong !! Try Later...");
			}

		} catch (Exception e) {

			LOGGER.info("Data Insertation Failed In UniversityServiceImpl.campusDriveRequest(-)" + e);
			throw e;
		}
		return driveResponseBean;
	}

	@Override
	public List<?> seeShareJobList(String token) throws Exception {

		String[] chunks1 = token.split(" ");
		String[] chunks = chunks1[1].split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();

		String payload = new String(decoder.decode(chunks[1]));
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(payload);

		JSONObject jsonObject = (JSONObject) obj;

		String email = (String) jsonObject.get("sub");

		Long id = universityRepository.findIdByEmail(email);
		AdminSharedJobList adminSharedJobList = null;
		List<AdminSharedJobList> SharedJobList = new ArrayList<AdminSharedJobList>();
		try {
			if (id != null) {
				List<Object[]> list = adminShareJobToUniversityRepository.getJobDetailsByUniversityId(id);

				if (list.size() != 0) {
					for (Object[] obj1 : list) {
						adminSharedJobList = new AdminSharedJobList();

						adminSharedJobList.setUniversityResponseStatus(String.valueOf(obj1[0]));
						adminSharedJobList.setShareJobId(String.valueOf(obj1[1]));
						adminSharedJobList.setJobTitle((String) obj1[2]);
						adminSharedJobList.setCategory((String) obj1[3]);
						adminSharedJobList.setJobType((String) obj1[4]);
						adminSharedJobList.setWfhCheckbox((Boolean) obj1[5]);
						adminSharedJobList.setSkills((String) obj1[6]);
						adminSharedJobList.setCity((String) obj1[7]);
						adminSharedJobList.setOpenings(String.valueOf(obj1[8]));
						adminSharedJobList.setSalary(String.valueOf(obj1[9]));
						adminSharedJobList.setAbout((String) obj1[10]);
						adminSharedJobList.setDescription((String) obj1[11]);

						SharedJobList.add(adminSharedJobList);
					}

				} else {
					throw new UniversityException("You Don't Have Any Shared Job !!");
				}
			} else {
				throw new UniversityException("University Data Not Found !!");
			}
		} catch (Exception e) {
			throw e;
		}

		return SharedJobList;
	}

	@Override
	public List<?> studentDetails(String token) throws Exception {
		String[] chunks1 = token.split(" ");
		String[] chunks = chunks1[1].split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();

		String payload = new String(decoder.decode(chunks[1]));
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(payload);

		JSONObject jsonObject = (JSONObject) obj;

		String email = (String) jsonObject.get("sub");
		List<Student> studentList = new ArrayList<Student>();
		List<University> university = new ArrayList<University>();

		university = universityRepository.getDetailsByEmail1(email);

		try {
			if (university.size() == 1) {
				studentList = studentRepository.getStudentListForUniversity(university.get(0).getUniversityId());

				if (studentList != null) {

				} else {
					throw new UniversityException("No Student Found In Your University !!");
				}
			} else {
				throw new UniversityException("Something Went Wrong !! Duplicte Data Found !!");
			}
		} catch (Exception e) {
			throw e;
		}

		return studentList;
	}

	@Override
	public List<?> seeShareJobListToStudent(String token) throws Exception {
		String[] chunks1 = token.split(" ");
		String[] chunks = chunks1[1].split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();

		String payload = new String(decoder.decode(chunks[1]));
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(payload);

		JSONObject jsonObject = (JSONObject) obj;

		String email = (String) jsonObject.get("sub");

		Long id = universityRepository.findIdByEmail(email);

		StudentResponseToUniversity responseToUniversity = null;
		List<StudentResponseToUniversity> responseToUniversityList = new ArrayList<StudentResponseToUniversity>();
		List<UniversityJobShareToStudent> universityJobShareToStudent = new ArrayList<UniversityJobShareToStudent>();

		try {
			if (id != null) {
				universityJobShareToStudent = universityJobShareRepository.getSharedJobList(id);
				if (universityJobShareToStudent.size() != 0) {
					for (int i = 0; i < universityJobShareToStudent.size(); i++) {

						responseToUniversity = new StudentResponseToUniversity();

						responseToUniversity
								.setJob(jobRepository.findByJobId(universityJobShareToStudent.get(i).getJobId()));
						responseToUniversity.setStudent(
								studentRepository.findByStudentId(universityJobShareToStudent.get(i).getStudentId()));

						if (responseToUniversity.getJob() != null | responseToUniversity.getStudent() != null) {

							if (universityJobShareToStudent.get(i).getStudentResponseStatus() != null
									&& universityJobShareToStudent.get(i).getStudentResponseStatus()) {

								responseToUniversity.setStudentResponse("ACCEPTED");

							} else if (universityJobShareToStudent.get(i).getStudentResponseStatus() != null
									&& !universityJobShareToStudent.get(i).getStudentResponseStatus()) {

								responseToUniversity.setStudentResponse("REJECTED");

							} else {
								responseToUniversity.setStudentResponse("No Response !!");
							}

						} else {
							responseToUniversity.setStudentResponse("No Data Found For This Job Or Student !!");
						}

						responseToUniversity.setStudentFeedBack(universityJobShareToStudent.get(i).getFeedBack());

						responseToUniversityList.add(responseToUniversity);
					}

				} else {
					throw new UniversityException("You Are Not Sharing Any Job To Students !!");
				}
			} else {
				throw new UniversityException("Something Went Wrong !! University Data Not Found !!");
			}

		} catch (Exception e) {
			throw e;
		}
		return responseToUniversityList;
	}

	@Override
	public List<?> studentFilter(String token, Long batchId, Long branchId, Double cgpa) throws ParseException {
		String[] chunks1 = token.split(" ");
		String[] chunks = chunks1[1].split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();

		String payload = new String(decoder.decode(chunks[1]));
		JSONParser jsonParser = new JSONParser();
		Object obj = jsonParser.parse(payload);

		JSONObject jsonObject = (JSONObject) obj;

		String email = (String) jsonObject.get("sub");
		List<Student> studentList = new ArrayList<Student>();
		List<University> university = new ArrayList<University>();

		university = universityRepository.getDetailsByEmail1(email);

		try {
			if (university.size() == 1) {
				studentList = studentRepository.getStudentFilter(university.get(0).getUniversityId(), batchId, branchId,
						cgpa);

				if (studentList.size() != 0) {

				} else {
					throw new UniversityException("No Student Found In This Filter !!");
				}
			} else {
				throw new UniversityException("Something Went Wrong !! Duplicte Data Found !!");
			}
		} catch (Exception e) {
			throw e;
		}

		return studentList;
	}

}
