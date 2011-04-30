package org.andwellness.config.load;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Procedure for adding a campaign to a class in the database: creates the campaign_class relation and for each user in the 
 * class, creates the user_role_campaign relation based on the user's class_role.   
 * 
 * This class will be replaced by /app/campaign/create and /app/campaign/update.
 * 
 * @author selsky
 */
public class AddCampaignToClass {
	private static Logger _logger = Logger.getLogger(AddCampaignToClass.class);
	
	/**
	 * A Java Properties file must be the only argument. The file must contain the following properties: campaignUrn, classUrn,
	 * dbUserName, dbPassword, dbDriver, dbJdbcUrl. 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 1) {
			throw new IllegalArgumentException("Invalid arguments: the name of a properties file must be the only argument.");
		}
		new AddCampaignToClass().runFromFile(args[0]);
	}
	
	private void runFromFile(String fileName) throws IOException {
		_logger.info("attempting to retrieve properties from file: " + fileName);
		
		BufferedReader in = new BufferedReader(new FileReader(fileName)); 
		Properties props = new Properties();
		props.load(in);
		in.close();
		
		checkProperty(props, "dbUserName");
		checkProperty(props, "dbPassword");
		checkProperty(props, "dbDriver");
		checkProperty(props, "dbJdbcUrl");
		checkProperty(props, "campaignUrn");
		checkProperty(props, "classUrn");
		
		// TODO replace "sys" with something more meaningful when we actually get around to naming the ap
		if(! props.getProperty("classUrn").startsWith("urn:sys")) {
			throw new IllegalArgumentException("invalid class URN: " + props.getProperty("classUrn"));
		}
		if(! props.getProperty("campaignUrn").startsWith("urn:")) {
			throw new IllegalArgumentException("invalid campaign URN: " + props.getProperty("campaignUrn"));
		}
		
		BasicDataSource dataSource = getDataSource(props);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		
		// make sure the campaign exists
		final int campaignId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
				                                         // if the campaign cannot be found
			"select id from campaign where urn = '" + props.getProperty("campaignUrn") + "'"
		); 
		_logger.info("found campaign.id " + campaignId + " for campaign URN " + props.getProperty("campaignUrn")); 
		
		
		
		// make sure the class exists
		final int classId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
												      // if the class cannot be found
			"select id from class where urn = '" + props.getProperty("classUrn") + "'"
		); 
		_logger.info("found class.id " + classId + " for class URN " + props.getProperty("classUrn"));
		
		
		
		// find the user role ids for participants and supervisors
		final int participantId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
			                                                // if the id cannot be found
			"select id from user_role where role = 'participant'"
		); 
		_logger.info("found user_role.id " + participantId + " for role 'participant'");
		
		
		
		final int supervisorId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
		                                                   // if the id cannot be found
			"select id from user_role where role = 'supervisor'"
		); 
		_logger.info("found user_role.id " + supervisorId + " for role 'supervisor'");

		
		
		final int authorId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
			                                                // if the id cannot be found
			"select id from user_role where role = 'author'"
		); 
		_logger.info("found user_role.id " + authorId + " for role 'author'");
		
		
		
		final int analystId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException 
		                                                   // if the id cannot be found
			"select id from user_role where role = 'analyst'"
		); 
		_logger.info("found user_role.id " + analystId + " for role 'analyst'");
		
		int[] privilegedCampaignRoles = {supervisorId, participantId};
		int[] restrictedCampaignRoles = {participantId, analystId};
		
		// prep for inserting into user_role_campaign
		String selectUserClass = "select user_id, role, user_class_role_id from user_class, class, user_class_role " +
				                 "where class.urn = ? and user_class.class_id = class.id and user_class.user_class_role_id = user_class_role.id";
		
		@SuppressWarnings("unchecked")
		List<UserIdClassRole> results = (List<UserIdClassRole>) jdbcTemplate.query(
			  selectUserClass, new Object[] {props.getProperty("classUrn")},
	           new RowMapper() {
                   public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                	   UserIdClassRole uicr = new UserIdClassRole();
                	   uicr.setId(rs.getInt(1));
                	   uicr.setClassRole(rs.getString(2));
                	   return uicr;
                   }
               }
		);
		
		if(results.isEmpty()) {
			throw new IllegalStateException("no users in user_class for class URN " + props.getProperty("classUrn"));
		}
		
		// Execute the rest of the queries inside a transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Transaction wrapping an insert into user, user_personal, user_user_personal, and user_class");
		
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		try {
			
			// create the campaign_class relationship
			jdbcTemplate.update(
			    new PreparedStatementCreator() {
			    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
			    		PreparedStatement ps = connection.prepareStatement(
			    			"insert into campaign_class (campaign_id, class_id) values (?,?)"
			    		);
			    		ps.setInt(1, campaignId);
			    		ps.setInt(2, classId);
			    		return ps;
			    	}
			    }
			);
			
//			// create the user_role_campaign relationship
//			for(UserIdClassRole uicr : results) {
//				final int userId = uicr.getId();
//				final String classRole = uicr.getClassRole();
//				
//				jdbcTemplate.update(
//				    new PreparedStatementCreator() {
//				    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
//				    		PreparedStatement ps = connection.prepareStatement(
//				    			"insert into user_role_campaign (user_id, campaign_id, user_role_id) values (?,?,?)"
//				    		);
//				    		ps.setInt(1, userId);
//				    		ps.setInt(2, campaignId);
//				    		if("privileged".equals(classRole)) {
//				    			ps.setInt(3, supervisorId);
//				    		} else if("restricted".equals(classRole)) {
//				    			ps.setInt(3, participantId);
//				    		} else {
//				    			// this is bad because it means there is incorrect data in the user_class table 
//				    			// TODO add a lookup table for class_role ??
//				    			throw new InvalidDataAccessResourceUsageException("incorrect class_role found for user id " + userId);
//				    		}
//				    		return ps;
//				    	}
//				    }
//				);
//			}
//			
			// create the user_role_campaign relationship for privileged users
			for(UserIdClassRole uicr : results) {
				
				final int userId = uicr.getId();
				final String classRole = uicr.getClassRole();
				
				if("privileged".equals(classRole)) {
					
					for(final int role : privilegedCampaignRoles) {
						jdbcTemplate.update(
						    new PreparedStatementCreator() {
						    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						    		PreparedStatement ps = connection.prepareStatement(
						    			"insert into user_role_campaign (user_id, campaign_id, user_role_id) values (?,?,?)"
						    		);
						    		ps.setInt(1, userId);
						    		ps.setInt(2, campaignId);
						    		ps.setInt(3, role);
						    		return ps;
						    	}
						    }
						);
					}
				}
			}
			
			// create the user_role_campaign relationship for restricted users
			for(UserIdClassRole uicr : results) {
				
				final int userId = uicr.getId();
				final String classRole = uicr.getClassRole();
				
				if("restricted".equals(classRole)) {
					
					for(final int role : restrictedCampaignRoles) {
						jdbcTemplate.update(
						    new PreparedStatementCreator() {
						    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
						    		PreparedStatement ps = connection.prepareStatement(
						    			"insert into user_role_campaign (user_id, campaign_id, user_role_id) values (?,?,?)"
						    		);
						    		ps.setInt(1, userId);
						    		ps.setInt(2, campaignId);
						    		ps.setInt(3, role);
						    		return ps;
						    	}
						    }
						);
					}
				}
			}
			
			transactionManager.commit(status); // end transaction
			_logger.info("transaction committed");
		}
		
		catch (DataAccessException dae) {
			
			_logger.error("Rolling back transaction!", dae);
			transactionManager.rollback(status);
			throw dae;
			
		}
		
		finally { // clean up
			try {
				
				if(dataSource != null) {
					dataSource.close();
					dataSource = null;
				}
			} 
			catch(SQLException sqle) {
				// not much that can be done so just log the error
				_logger.error(sqle);
				
			}
		}
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
	 * Returns a BasicDataSource configured using the provided Properties. 
	 */
	private BasicDataSource getDataSource(Properties props) {

		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setDriverClassName(props.getProperty("dbDriver"));
		dataSource.setUrl(props.getProperty("dbJdbcUrl"));
		dataSource.setUsername(props.getProperty("dbUserName"));
		dataSource.setPassword(props.getProperty("dbPassword"));
		dataSource.setInitialSize(3);
		// commits are performed manually
		dataSource.setDefaultAutoCommit(false);
		// use the default MySQL isolation level: REPEATABLE READ
		
		return dataSource;
	}
	
	public class UserIdClassRole {
		private int _id;
		private String _classRole;
		private int _classRoleId;
		
		public int getId() {
			return _id;
		}
		public void setId(int id) {
			_id = id;
		}
		public int getClassRoleId() {
			return _classRoleId;
		}
		public void setClassRoleId(int id) {
			_classRoleId = id;
		}
		public String getClassRole() {
			return _classRole;
		}
		public void setClassRole(String classRole) {
			_classRole = classRole;
		}
	}
}
