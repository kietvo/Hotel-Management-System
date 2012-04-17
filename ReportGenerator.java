import java.sql.*;
/**
Class to handle the report generation
*/
public class ReportGenerator
{
	Connection connection;
	/**
	ReportGenerator constructor specifying the connection object
	@param connection The JDBC connection object  
	*/
	public ReportGenerator(Connection connection)
	{
		this.connection = connection;
	}
	/**
	This method reports the occupancy by room type across the entire hotel chain
	*/
	public void reportOccByRoomType()
	{
		/*
 * 		QUERY THE ROOM TABLE AND GROUP BY THE HOTEL CATEGORY WHERE THE AVAILABILITY IS FALSE
 * 		*/	


	//	String query = "select r.hid, r.rnumber, r.category, res.cid, re.r_checkindt, re.r_checkoutdt from room r, reserves re, reservation res where r.hid = re.r_hid and r.rnumber = re.r_rnumber and re.r_resid = res.resid and r.avail='F' and (re.r_checkoutdt is NULL or re.r_checkoutdt>=current_timestamp) group by r.hid, r.r.category"; 
		String query = "select category, count(*) from room where avail='F' group by category";
		try
		{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
//			System.out.println("ResultSet fetch Size " + rs.getFetchSize());
			System.out.println("Occupancy by room type :"); 
			while(rs.next())
			{
		//		System.out.println(rs.getInt(1) + " " + rs.getInt(2) + " " + rs.getString(3) + " " + rs.getInt(4) + " " + rs.getTimestamp(5) + " " + rs.getTimestamp(6));
//				System.out.println(rs.getInt(1) + " " + rs.getInt(2) + " " + rs.getString(3) + " " + rs.getInt(4));
				System.out.println(rs.getString(1) + " " + rs.getInt(2));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                	System.out.println(e.getMessage());
                }
	}
	/**
	  This method reports the occupancy by the date range
	  @param startdt The start date for the query
	  @param enddt The end date for the query
	 */
	public void reportOccByDateRange(String startdt, String enddt)
	{
	try
		{
			/*
 * 			GET THE ROOM, RESERVATION AND CUSTOMER DETAILS BETWEEN THE GIVEN DATES
 * 			*/

			PreparedStatement st = connection.prepareStatement("select r.hid, r.rnumber, r.category, res.cid, re.r_checkindt, re.r_checkoutdt from room r, reserves re, reservation res where r.hid = re.r_hid and r.rnumber = re.r_rnumber and re.r_resid = res.resid and re.r_checkindt between to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')");
//(r.hid, r.rnumber) in (select r_hid, r_rnumber from reserves where r_checkindt >= to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') AND to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')<=r_checkoutdt)");
			st.setString(1, startdt);
			st.setString(2, enddt);
			ResultSet rs = st.executeQuery();
			System.out.println("Occupancy by date range between "+ startdt+ " and " +enddt+" :\n");
			while(rs.next())
			{
				System.out.println(rs.getInt(1) + " " + rs.getInt(2) + " " + rs.getString(3) + " " + rs.getInt(4) + " " + rs.getTimestamp(5) + " " + rs.getTimestamp(6));
			}
			System.out.println("\n\n");
		}
		catch(SQLException se)
                {
                        System.out.println(se.getMessage());
                }

	}
	/**
	  This method reports the occupancy by the hotel
	  */
	public void reportOccByHotel()
	{
		try
		{
			/*
 * 			QUERY THE ROOM TABLE WHERE AVAILIBILITY IS FALSE AND GROUP BY THE HOTEL ID
			*/
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery("select hid, count(*) from room where avail='F' group by hid");
			System.out.println("Occupancy by hotel :\n");
			while(rs.next())
			{
				System.out.println(rs.getInt(1) + " " + rs.getInt(2));
//				System.out.println(rs.getInt(1)+" "+rs.getString(2) + " "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6)+" "+rs.getFloat(7)+" "+rs.getInt(8)+" "+rs.getInt(9));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	/**
	  This method reports the occupants within a specified time range
	  @param startdt The start date the query
	  @param enddt The end date for the query
	  */
	public void reportOccupants(String startdt, String enddt)
	{
		try
		{
			/*
 * 			QUERY THE CUSTOMER TABLE AND SELECT THE CUSTOMERS USING A SUB-QUERY SO THAT
 * 			THE CUSTOMER HAS A RESERVATION BETWEEN THE SPECIFIED DATES
			*/
			PreparedStatement st = connection.prepareStatement("select cid, cname, cemail, caddr, cphone from customer where cid in (select cid from reservation where resid in (select r_resid from reserves where r_checkindt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r_checkindt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')))");
			st.setString(1, startdt);
			st.setString(2, enddt);
			ResultSet rs = st.executeQuery();
			System.out.println("Occupants by date range between "+ startdt+ " and " +enddt+" :\n");
			while(rs.next())
			{
				System.out.println(rs.getInt(1)+" "+rs.getString(2)+ " "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	/**
	  This method reports the percentage of rooms occupied 
	  */
	public void reportPercentRoomsOccupied()
	{
		/*
 * 		QUERY THE ROOM TABLE AND REPORT THE PERCENTAGE OCCUPANCY (USING AVAILABILITY ATTRIBUTE IN THE ROOM)
		*/
		String query = "select sum(case when avail='T' then 1 else 0 end)*100/count(*) from room";
		try
		{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
//			System.out.println("ResultSet fetch Size " + rs.getFetchSize());
			System.out.println("Percentage of Rooms Occupied :\n"); 
			while(rs.next())
			{
				System.out.println(rs.getFloat(1));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }
	}
	/**
	  This method reports the staff information by the given role
	  @param jobtitle The job title
	  */
	public void reportStaffByRole(String jobtitle)
	{
		try
		{
			/*
 * 			QUERY THE STAFF TABLE AND SELECT THE ROWS WHICH SATISFY THE GIVEN JOBTITLE
			*/
			PreparedStatement st = connection.prepareStatement("select staffid, sname, semail, saddr, jobtitle, sphone, salary, hid, sage from staff where jobtitle=?");
			st.setString(1,jobtitle);
			ResultSet rs = st.executeQuery();
			System.out.println("Information on staff grouped by the role "+jobtitle+"\n");
			while(rs.next())
			{
				System.out.println(rs.getInt(1)+" "+rs.getString(2) + " "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6)+" "+rs.getFloat(7)+" "+rs.getInt(8)+" "+rs.getInt(9));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	/** 
	  Report the customers being served by a given staff member between the specified dates
	  @param staffid The staff id
	  */

	public void reportCustomerByStaff(int staffid)
	{
		try
		{
			/*
 * 			//QUERY THE ASSIGNED TABLE TO GET THE ROOM NUMBER AND THE HOTEL ID 
 * 			CORRESPONDING TO THE GIVEN CUSTOMER ID
			*/
			PreparedStatement st = connection.prepareStatement("select a_hid, a_rnumber from assigned where a_staffid=?");
			st.setInt(1, staffid);
			ResultSet rs = st.executeQuery();
			while(rs.next())
			{
				/*
 * 				FOR EACH ROOM, GET THE DETAILS OF THE CORRESPONDING CUSTOMER 
 * 				RESIDING IN THAT ROOM
 * 				*/
				PreparedStatement st2 = connection.prepareStatement("select * from customer where cid in (select res.cid from reserves re, reservation res where re.r_resid = res.resid and re.r_hid = ? and re.r_rnumber = ?)");
			       	st2.setInt(1, rs.getInt(1));
				st2.setInt(2, rs.getInt(2));
				ResultSet rs2 = st2.executeQuery();
				while(rs2.next())
				{
					System.out.println(rs2.getInt(1) + " " + rs2.getString(2) + " " + rs2.getString(3) + " " + rs2.getString(4) + " " + rs2.getString(5) + " " + rs2.getString(6) + " " + rs2.getString(7));
				}		
			}
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	
	/** 
          Report the customers being served by a given staff member between the specified dates
          @param staffid The staff id
	  @param startdt start date
	  @param enddt end date
          */

	public void reportCustomerByStaffoperation(int staffid, String startdt, String enddt)
	{
		//THIS FUNCTION IS NOT USED
		try
		{
			connection.setAutoCommit(false);
			PreparedStatement st = connection.prepareStatement("update reserves set r_checkoutdt=current_timestamp where r_checkoutdt is NULL");
			st.executeUpdate();
			(new InfoProcessor(connection)).viewTable("reserves");
			st = connection.prepareStatement("select cid, cname, caddr, cemail, cphone from customer where cid in (select cid from reservation where resid in (select r_resid from reserves where (r_hid, r_rnumber) in ((select a_hid, a_rnumber from assigned a, reserves r where a.a_hid = r.r_hid and a.a_rnumber = r.r_rnumber and a.a_staffid=? and r.r_checkindt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkoutdt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')) union (select a_hid, a_rnumber from assigned a, reserves r where a.a_hid = r.r_hid and a.a_rnumber = r.r_rnumber and a.a_staffid=? and r.r_checkindt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkoutdt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')) union (select a_hid, a_rnumber from assigned a, reserves r where a.a_hid = r.r_hid and a.a_rnumber = r.r_rnumber and a.a_staffid=? and r.r_checkindt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkoutdt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkoutdt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')) union (select a_hid, a_rnumber from assigned a, reserves r where a.a_hid = r.r_hid and a.a_rnumber = r.r_rnumber and a.a_staffid=? and r.r_checkindt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkindt<=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and r.r_checkoutdt>=to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')))))");
			st.setInt(1, staffid);
			st.setString(2, startdt);
			st.setString(3, enddt);
			st.setInt(4, staffid);
			st.setString(5, startdt);
			st.setString(6, enddt);
			st.setInt(7, staffid);
			st.setString(8, startdt);
			st.setString(9, enddt);
			st.setString(10, startdt);
			st.setInt(11, staffid);
			st.setString(12, startdt);
			st.setString(13, enddt);
			st.setString(14, enddt);
			ResultSet rs = st.executeQuery();
			System.out.println("Information on customers alloted to staff ID " + staffid +" :\n");
			while(rs.next())
			{
				System.out.println(rs.getInt(1)+" "+rs.getString(2)+" "+rs.getString(3)+" "+rs.getString(4)+" "+rs.getString(5));
			}
			System.out.println("\n\n");
			connection.rollback();
			connection.setAutoCommit(true);
		}
	
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
}

