package au.com.gaiaresources.bdrs.security;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import au.com.gaiaresources.bdrs.model.user.CM3UserImpl;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;


public class CM3AuthenticationService implements UserDetailsService {

//	private static final String[] s_roles = { "ROLE_USER" };

	@Autowired
	private UserDAO userDAO;

    private Logger logger = Logger.getLogger(getClass());

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {

		// Username is actually the CM3 SID which we'll use to find the actual
		// username.
		CM3UserImpl u = new CM3UserImpl();
		u.setSid(username);
		StringBuffer response = new StringBuffer();

		try {
			String key = "climatewatch";
			Date currentTime = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			key += formatter.format(currentTime);

			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(key.getBytes(),0,key.length());
			String hash = new BigInteger(1,m.digest()).toString(16);
			while (hash.length() < 32) {
				  hash = "0" + hash;
			}

			String content = "sid=" + u.getSid() + "&sec=" + hash;
			logger.info(content + "\n");
			URL url = new URL(
					"http://www.climatewatch.org.au/login/authenticate.aspx?" + content);
			logger.info("Performing external authentication for user: " + username + "\n");
			URLConnection urlConn = url.openConnection();

			// check if Basic authentication is required
            String auth = urlConn.getHeaderField("WWW-Authenticate");
            logger.info("Remote Service AUTH Header: " + auth + "\n");
            if (auth != null) {

               // do http basic authentication
               if (auth.startsWith("Basic")) {
                  String user = "ddsn.staging";
                  String pass = "pr1ject5";
                  String password = user + ":" + pass;
                  Base64 b = new Base64();
                  byte[] encodedPassword =
                        b.encode(password.getBytes());
                  logger.info("Sending auth: " + new String(encodedPassword) + "\n");

                  urlConn = url.openConnection();
                  // set authentication property in the http header
                  urlConn.setRequestProperty("Authorization",
                                                      "Basic "
                                                      + new String(encodedPassword));
               }
            }

			BufferedReader is = new BufferedReader(new InputStreamReader(new DataInputStream(urlConn.getInputStream())));

			String str;
			logger.info("Reading Response\n");
			String si;
			ArrayList<String> roles = new ArrayList<String>();
			roles.add("ROLE_USER"); // Lets minimally make ALL CM3 accounts Users.
			while (null != ((str = is.readLine())))	{
				str = str.trim();
				// should really use a proper XML parser, but it's so short.
				if (str.indexOf('>') >= str.length() - 1 ) // single XML tag, so ignore line.
					continue;

				si = str.substring(str.indexOf('>') + 1, str.lastIndexOf('<'));
				if (str.startsWith("<sid>")) {
					// nothing.. we already know this.
				} else if (str.startsWith("<authenticated>")) {
					if (Integer.parseInt(si) == 0)
						throw new UsernameNotFoundException("User not authenticated");
					u.setActive(true);
					u.setPassword("CM3User");
					u.setRegistrationKey("cm3user");
				} else if (str.startsWith("<userid>")) {
					u.setName(si);
				} else if (str.startsWith("<firstname>")) {
					u.setFirstName(si);
				} else if (str.startsWith("<lastname>")) {
					u.setLastName(si);
				} else if (str.startsWith("<email>")) {
					u.setEmailAddress(si);
				} else if (str.startsWith("<group>")) {
					if (si.equalsIgnoreCase("Gaia Member"))	{
						//roles.add("ROLE_USER"); // we've already done this to make all cm3 accounts users.
					} else if (si.equalsIgnoreCase("Gaia Admin")) {
						roles.add(Role.ADMIN);
					} else if (si.equalsIgnoreCase("Gaia Science")) {
						roles.add("ROLE_EXPERT");
					}
				}

				logger.info("Read: '" + str + "'\n");
				response.append(str);
				response.append('\n');
			}
			u.setRoles(roles.toArray(new String[roles.size()]));

			logger.info("Complete Response: " + response);


		} catch (MalformedURLException mue) {
			logger.error(mue.getMessage(), mue);

		} catch (IOException ioe) {
			logger.error(ioe.getMessage(), ioe);

		} catch (Exception e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(os));
			logger.error(os.toString(), e);
		}

		// now check to see if we have a user record.
		User u2 = userDAO.getUser(u.getName());

		// no we don't, so we need to create one so we can link to our records.
		if (u2 == null) {
			try
			{
				u2 = userDAO.createUser(u.getName(), u.getFirstName(), u
					.getLastName(), u.getEmailAddress(), "CM3User", u
					.getRegistrationKey(), "ROLE_USER");
			}
			catch (Exception e)
			{
				logger.error(e.toString(), e);
			}
		}

		if (u2 != null)
		{
			if (!u2.isActive())
				userDAO.makeUserActive(u2, true);

			u2.setRoles(u.getRoles());
			return new UserDetails(u2);
		}
		throw new UsernameNotFoundException("Username not found : " + u.getName());
	}
}
