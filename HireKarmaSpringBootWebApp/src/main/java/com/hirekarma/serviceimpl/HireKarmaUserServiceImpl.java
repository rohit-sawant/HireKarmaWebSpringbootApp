package com.hirekarma.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hirekarma.beans.HireKarmaUserBean;
import com.hirekarma.model.HireKarmaUser;
import com.hirekarma.repository.HireKarmaUserRepository;
import com.hirekarma.service.HireKarmaUserService;

@Service("hireKarmaUserService")
public class HireKarmaUserServiceImpl implements HireKarmaUserService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HireKarmaUserServiceImpl.class);
	
	@Autowired
	private HireKarmaUserRepository hireKarmaUserRepository;

	@Override
	public HireKarmaUser insert(HireKarmaUser hireKarmaUser) {
		LOGGER.debug("Inside HireKarmaUserServiceImpl.insert(-)");
		HireKarmaUser karmaUser=null;
		try {
			LOGGER.debug("Inside try block of HireKarmaUserServiceImpl.insert(-)");
			karmaUser=hireKarmaUserRepository.save(hireKarmaUser);
			LOGGER.info("Data successfully saved using HireKarmaUserServiceImpl.insert(-)");
			return karmaUser;
		}
		catch (Exception e) {
			LOGGER.error("Data Insertion failed using HireKarmaUserServiceImpl.insert(-): "+e);
			throw e;
		}
	}

	@Override
	public HireKarmaUserBean checkLoginCredentials(String email, String password) {
		LOGGER.debug("Inside HireKarmaUserServiceImpl.checkLoginCredentials(-,-)");
		HireKarmaUserBean karmaUserBean=null;
		HireKarmaUser karmaUser=null;
		try {
			LOGGER.debug("Inside try block of HireKarmaUserServiceImpl.checkLoginCredentials(-,-)");
			karmaUser=hireKarmaUserRepository.checkLoginCredentials(email, password);
			if(karmaUser!=null) {
				LOGGER.info("user credential match using HireKarmaUserServiceImpl.checkLoginCredentials(-,-)");
				karmaUserBean=new HireKarmaUserBean();
				BeanUtils.copyProperties(karmaUser, karmaUserBean);
				return karmaUserBean;
			}
			else {
				LOGGER.info("user credential does not match using HireKarmaUserServiceImpl.checkLoginCredentials(-,-)");
				return null;
			}
		}
		catch (Exception e) {
			LOGGER.info("Error occured in HireKarmaUserServiceImpl.checkLoginCredentials(-,-): "+e);
			throw e;
		}
	}

}
