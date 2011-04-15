package org.andwellness.config.load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.xml.sax.SAXException;

/**
 * @author selsky
 */
public class CampaignLoader {
	private static final Logger _logger = Logger.getLogger(CampaignLoader.class);
	private JdbcTemplate _jdbcTemplate;
	
	public CampaignLoader() {
		
	}
	
	/**
	 * Validates a configuration file and loads it into the AW database.
	 */
	public static void main(String[] args) throws ValidityException, SAXException, ParsingException, IOException {
		if(args.length != 3) {
			throw new IllegalArgumentException("Invalid arguments: the name of a campaign configuration file is required as the " +
				"first argument, the name of a properties file with db props must be the second argument, and the name of a " +
				"a campaign configuration XML schema file must be the third argument.");
		}
		CampaignLoader loader = new CampaignLoader();
		loader.run(args[0], args[1], args[2]);
	}
	
	/**
	 * Validates a campaign configuration file and loads it into the AW database.
	 * 
	 * @param fileName
	 * @throws ValidityException
	 * @throws SAXException
	 * @throws ParsingException
	 * @throws IOException
	 */
	public void run(String configFileName, String dbPropsFileName, String schemaFileName)
		throws ValidityException, SAXException, ParsingException, IOException {
		
		_logger.info("checking db props");
				
		BufferedReader in = new BufferedReader(new FileReader(dbPropsFileName)); 
		Properties props = new Properties();
		props.load(in);
		in.close();
		
		checkProperty(props, "dbUserName");
		checkProperty(props, "dbPassword");
		checkProperty(props, "dbDriver");
		checkProperty(props, "dbJdbcUrl");
		checkProperty(props, "runningState");
		checkProperty(props, "privacyState");
		checkProperty(props, "description");
		
		validatePrivacyStateProp(props.getProperty("privacyState"));
		validateRunningStateProp(props.getProperty("runningState"));
		
		_logger.info("validating config file ... " + configFileName);
		
		CampaignValidator validator = new CampaignValidator();
		validator.runAgainstFiles(configFileName, schemaFileName);
		
		// set up connection to db
		_jdbcTemplate = new JdbcTemplate(getDataSource(props));
		
		// grab the XML document using Xom
		Builder builder = new Builder();
		Document document = builder.build(configFileName);
		Element root = document.getRootElement();
		
		insertCampaign(root, props);
		
		_logger.info("campaign inserted successfully");
		
	}
		
	/**
	 * Inserts the configuration into the database. 
	 * 
	 * @throws IllegalStateException if the campaign version already exists in the database
	 */
	private void insertCampaign(Element root, Properties props) {
		final String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue(); 
		_logger.info("inserting campaign " + campaignUrn);
		final String campaignName = root.query("/campaign/campaignName").get(0).getValue();
		final String description = props.getProperty("description");
		final String privacyState = props.getProperty("privacyState");
		final String runningState = props.getProperty("runningState");
		final String xml = root.toXML(); // whitespace is left intact - it can be removed with an XSLT in the future
		
		final String sql = "insert into campaign (description, xml, running_state, privacy_state, name, urn, creation_timestamp)" +
				" values (?,?,?,?,?,?,?)"; 
		
		try {
			
			_jdbcTemplate.update(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(sql);
						ps.setString(1, description);
						ps.setString(2, xml);
						ps.setString(3, runningState);
						ps.setString(4, privacyState);
						ps.setString(5, campaignName);
						ps.setString(6, campaignUrn);
						ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
						return ps;
					}
				}
			);
		
		} catch (DataIntegrityViolationException dive) { // thrown if the unique key of campaign_id-version exists
			
			if(((SQLException) dive.getCause()).getErrorCode() == 1062) { // not great to hardcode the MySQL error code
			
			    throw new IllegalStateException("Campaign with URN already exists.", dive);
			    
			} else {
				
				throw new IllegalStateException("Caught DataIntegrityViolation.", dive);
			}
		} 
		
		// instead of catching it, just allow the Spring DataAccessException to be thrown up the call stack
		
	}	
	
	/**
	 * Validates incoming property values for non-emptiness.
	 */
	private void checkProperty(Properties props, String propName) {
		if((! props.containsKey(propName)) || isEmptyOrWhitespaceOnly(props.getProperty(propName))) {
			throw new IllegalArgumentException("Missing required " + propName + " property in input file.");
		}
	}
	
	/**
	 * Validates whether Strings are empty or null. 
	 */
	private boolean isEmptyOrWhitespaceOnly(String string) {
		return null == string || "".equals(string.trim());
	}
	
	/**
	 * Validates the privacy state.
	 */
	private void validatePrivacyStateProp(String value) {
		if(! "private".equals(value) && ! "shared".equals(value)) {
			throw new IllegalArgumentException("invalid privacy state value: " + value + " (it must be either public or shared).");
		}
	}

	/**
	 * Validates the running state.
	 */
	private void validateRunningStateProp(String value) {
		if(! "active".equals(value) && ! "inactive".equals(value)) {
			throw new IllegalArgumentException("invalid state value: " + value + " (it must be either active or inactive).");
		}
	}

	/**
	 * Returns a BasicDataSource configured using the provided Properties. 
	 */
	private BasicDataSource getDataSource(Properties props) {

		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setDriverClassName(props.getProperty("dbDriver"));
		dataSource.setUrl(props.getProperty("dbJdbcUrl"));
		dataSource.setUsername(props.getProperty("dbUserName"));
		dataSource.setPassword(props.getProperty("dbPassword"));
		dataSource.setInitialSize(3);
		// commits are performed automatically
		dataSource.setDefaultAutoCommit(true);
		// use the default MySQL isolation level: REPEATABLE READ
		
		return dataSource;
	}
}
