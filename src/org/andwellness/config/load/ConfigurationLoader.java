package org.andwellness.config.load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.ConfigurationValidator;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.xml.sax.SAXException;

/**
 * @author selsky
 */
public class ConfigurationLoader {
	private static final Logger _logger = Logger.getLogger(ConfigurationLoader.class);
	private JdbcTemplate _jdbcTemplate;
	private int _campaignId;
	
	public ConfigurationLoader() {
		_campaignId = -1;
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
		ConfigurationLoader loader = new ConfigurationLoader();
		loader.run(args[0], args[1], args[2]);
	}
	
	/**
	 * Validates a configuration file and loads it into the AW database.
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
		Properties dbProps = new Properties();
		dbProps.load(in);
		in.close();
		
		checkProperty(dbProps, "dbUserName");
		checkProperty(dbProps, "dbPassword");
		checkProperty(dbProps, "dbDriver");
		checkProperty(dbProps, "dbJdbcUrl");
		
		_logger.info("validating config file ... " + configFileName);
		
		ConfigurationValidator validator = new ConfigurationValidator();
		validator.run(configFileName, schemaFileName);
		
		// set up connection to db
		_jdbcTemplate = new JdbcTemplate(getDataSource(dbProps));
		
		_logger.info("checking for valid campaign and campaign version ... ");

		// grab the XML document using Xom
		Builder builder = new Builder();
		Document document = builder.build(configFileName);
		Element root = document.getRootElement();
		
		checkCampaignName(root);
		insertConfiguration(root);
		
		_logger.info("configuration inserted successfully");
		
	}
	
	/**
	 * Checks that the config file contains a campaign that the db already knows about. Then checks to make sure that the config
	 * file references a version that is not already present in the db.
	 * 
	 *  @throws IllegalStateException if the campaign name references a campaign that does not exist
	 */
	private void checkCampaignName(Element root) {
		
		String campaignName = root.query("/campaign/campaignName").get(0).getValue(); // this query without error checking is ok
		                                                                              // because our schema guarantees the structure
		
		try {
			
			_campaignId = _jdbcTemplate.queryForInt("select id from campaign where name = \"" + campaignName + "\"");
			
		} catch(IncorrectResultSizeDataAccessException irsdae) {
			
			throw new IllegalStateException("invalid config file. campaign name " + campaignName + " not found", irsdae);
			
		}
	}
	
	/**
	 * Inserts the configuration into the database. 
	 * 
	 * @throws IllegalStateException if the campaign version already exists in the database
	 */
	private void insertConfiguration(Element root) {
		final String campaignVersion = root.query("/campaign/campaignVersion").get(0).getValue(); // this query without error checking is ok
													        					      			  // because our schema guarantees the structure
		final String xml = root.toXML(); // whitespace is left intact - it can be removed with an XSLT in the future
		final String sql = "insert into campaign_configuration (campaign_id, version, xml) values (?,?,?)"; 
		
		try {
			
			_jdbcTemplate.update(
				new PreparedStatementCreator() {
					public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						PreparedStatement ps = connection.prepareStatement(sql);
						ps.setInt(1, _campaignId);
						ps.setString(2, campaignVersion);
						ps.setString(3, xml);
						return ps;
					}
				}
			);
		
		} catch (DataIntegrityViolationException dive) { // thrown if the unique key of campaign_id-version exists
			
			if(((SQLException) dive.getCause()).getErrorCode() == 1062) { // not great to hardcode the MySQL error code
			
			    throw new IllegalStateException("campaign_id-version combination already exists", dive);
			    
			} else {
				
				throw new IllegalStateException("caught DataIntegrityException - not a duplicate", dive);
			}
		} 
		
		// instead of catching it, just allow the Spring DataAccessException to be thrown up the call stack
		
	}	
	
	/**
	 * Validates incoming property values. 
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
