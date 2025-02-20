package ar.com.tandilweb.ApiServer.transport;

import java.util.Calendar;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ar.com.tandilweb.ApiServer.dataTypesObjects.generic.ValidationException;
import ar.com.tandilweb.ApiServer.dataTypesObjects.login.LoginRequest;
import ar.com.tandilweb.ApiServer.dataTypesObjects.login.SessionInformation;
import ar.com.tandilweb.ApiServer.dataTypesObjects.login.SignupRequest;
import ar.com.tandilweb.ApiServer.persistence.domain.Sessions;
import ar.com.tandilweb.ApiServer.persistence.domain.Users;
import ar.com.tandilweb.ApiServer.persistence.repository.SessionsRepository;
import ar.com.tandilweb.ApiServer.persistence.repository.UsersRepository;

@Service
public class LoginAdapter {

	@Autowired
	private UsersRepository userRepository;

	@Autowired
	private SessionsRepository sessionsRepository;

	@Value("${ar.com.tandilweb.ApiServer.users.initialChips}")
	private long initialChips;

	@Value("${ar.com.tandilweb.ApiServer.users.maxBadLogins}")
	private short maxBadLogins;

	public SessionInformation signup(SignupRequest signupData) throws ValidationException {
		if (userRepository.checkEmailUser(signupData.email, signupData.nick_name)) {
			throw new ValidationException(4, "Email o nick are in use");
		}
		Users user = new Users();
		user.setBadLogins((short) 0);
		user.setChips(initialChips);
		user.setEmail(signupData.email);
		user.setNick_name(signupData.nick_name);
		// TODO: crypt password field, and compare hashes in login:
		user.setPassword(signupData.password);
		user.setPhoto(signupData.photo != null ? signupData.photo : "#");
		user = userRepository.create(user);
		// create session for new user:
		Sessions session = new Sessions();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 20);
		session.setExpiration(c.getTime());
		
		session.setId_user(user.getId_user());
		String sessionPassphrase = UUID.randomUUID().toString();
		session.setJwt_passphrase(sessionPassphrase);
		session = sessionsRepository.create(session);
		SessionInformation out = new SessionInformation();
		out.operationSuccess = true;
		out.jwtPasspharse = sessionPassphrase;
		out.sessionID = session.getId_session();
		out.userID = session.getId_user();
		return out;
	}

	public SessionInformation login(LoginRequest loginData) throws ValidationException {
		Users user = userRepository.findByEmailOrUser(loginData.umail);
		if (user == null) {
			throw new ValidationException(3, "Not user found");
		}
		if (maxBadLogins > 0 && user.getBadLogins() == maxBadLogins) {
			throw new ValidationException(4, "You are blocked for max bad logins");
		}
		// TODO: crypt password field, and compare hashes in login:
		if (!user.getPassword().equals(loginData.password)) {
			user.setBadLogins((short) (user.getBadLogins() + 1));
			throw new ValidationException(5, "Invalid password");
		}
		// create session for new user:
		Sessions session = new Sessions();
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MINUTE, 20);
		session.setExpiration(c.getTime());
		
		session.setId_user(user.getId_user());
		String sessionPassphrase = UUID.randomUUID().toString();
		session.setJwt_passphrase(sessionPassphrase);
		session = sessionsRepository.create(session);
		SessionInformation out = new SessionInformation();
		out.operationSuccess = true;
		out.jwtPasspharse = sessionPassphrase;
		out.sessionID = session.getId_session();
		out.userID = session.getId_user();
		return out;
	}

}
