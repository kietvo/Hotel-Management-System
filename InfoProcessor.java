import java.sql.*;
/**
 * Class to handle the data manipulation
 */
public class InfoProcessor
{
	Connection connection;
	/**
 * 	InfoProcessor constructor specifying the connection object
 * 	@param connection The JDBC connection object
 * 	*/
	public InfoProcessor(Connection connection)
	{
		this.connection = connection;
	}
    /**
 * 	This method displays the contents of the table
 * 	@param hid the name of the table
 * 	*/
	public void viewTable(String tablename)
	{
		// THIS QUERY LIST ALL THE ATTRIBUTES FROM THE PARTICULAR TABLE 
		try
		{
			String query = "select * from " +tablename;
			PreparedStatement st = connection.prepareStatement(query);

			ResultSet rs = st.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colCount = rsmd.getColumnCount();
			System.out.println("no. of columns : " + colCount);
			while(rs.next())
			{
				for(int i=1;i<=colCount;i++)
				{
					if(rsmd.getColumnType(i)==Types.INTEGER || rsmd.getColumnType(i)==Types.NUMERIC)
					{
						int temp = rs.getInt(i);
						if(!rs.wasNull())
							System.out.print(rs.getInt(i)+" ");
					}
					else if(rsmd.getColumnType(i)==Types.VARCHAR)
					{
						String temp  = rs.getString(i);
						if(!rs.wasNull())
							System.out.print(temp.replaceAll("\\r\\n","")+" ");
					}
					else if(rsmd.getColumnType(i)==Types.FLOAT)
					{
						float temp = rs.getFloat(i);
						if(!rs.wasNull())
							System.out.print(rs.getFloat(i)+" ");
					}
					else if(rsmd.getColumnType(i)==Types.TIMESTAMP)
					{
						Timestamp temp = rs.getTimestamp(i);
						if(temp!=null)
							System.out.print(rs.getTimestamp(i)+" ");
					}
				}
				System.out.println();
			}
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
	/**
 * 	This method checks  and displays the available rooms.
 * 	@param 
 * 	*/
	public void checkAvailableRooms()
	{

		// THIS QUERY LIST ALL THE ROOMS IN ALL HOTELS WHERE AVAIL ATTRIBUTE IS 'T'
		String query = "select hid, rnumber from room where avail='T'";
		try
		{
			Statement st = connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			System.out.println("Currently available rooms:"); 
			while(rs.next())
			{
				System.out.println(rs.getInt(1) + " " + rs.getInt(2));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}
	/**
 * 	This method checks  and displays the available rooms by hotel id.
 * 	@param hid the hotel id
 * 	*/
	public void checkAvailableRoomsByHotel(int hid)
	{
		// THIS QUERY LIST ALL THE ROOMS IN THE PARTICULAR HOTEL WHERE AVAIL ATTRIBUTE IS 'T'
		try
		{
			PreparedStatement st = connection.prepareStatement("select hid, rnumber from room where avail='T' and hid=?");
			st.setInt(1, hid);
			ResultSet rs = st.executeQuery();
//			System.out.println("Occupancy by date range between "+ startdt+ " and " +enddt+" :\n");
			System.out.println("Currently available rooms in hotel " + hid);
			while(rs.next())
			{
				System.out.println(rs.getInt(1) + " " + rs.getInt(2));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
	/**
 * 	This method assigns a particular room to a particular customer
 * 	@param cid the customer id
 * 	@param checkindt the check-in date and time
 * 	@param resmethod the method of reservation
 * 	@param hid the hotel id
 * 	@param rnumber the room number
 * 	*/
	public void assignRoomToCustomer(int cid, String checkindt, String checkoutdt, String resmethod, int hid, int rnumber)
	{
		// THIS QUERY ASSIGNS A PARTICULAR ROOM TO CUSTOMER 

		try
		{
			connection.setAutoCommit(false);
			int nextid = 0;
			String method = "";
			if(resmethod.equals("O"))
				method = "Online";
			else
				method = "at the Front Desk";
			Statement query = connection.createStatement();
			ResultSet rs1=query.executeQuery("select avail from room where hid="+hid+"and rnumber="+rnumber);
			rs1.next();

			String avail=rs1.getString(1);


			ResultSet rs2=query.executeQuery("select r_checkoutdt from reserves where r_hid="+hid+"and r_rnumber="+rnumber+" and r_resid>= all(select r_resid from reserves where r_rnumber="+rnumber+"and r_hid="+hid+")");
			

			rs2.next();


			Timestamp checkodt=Timestamp.valueOf("9999-12-31 11:00:00");
			if(!rs2.wasNull())
			{
			checkodt=rs2.getTimestamp(1);
			}
		PreparedStatement ps=connection.prepareStatement("select to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') from dual");
		         ps.setString(1,checkindt);
                         ResultSet rs3=ps.executeQuery();
			 rs3.next();								  
			 Timestamp chkindt=rs3.getTimestamp(1);
			
			// CHECKS IF ROOM IS BOOKED OR NOT 

			if(rs2.wasNull()&&avail.equals('F'))
			{
				System.out.println("Room is booked. \n Please choose another room");
				return;
			}
			else
			{

			// CHECKS IF CHECK OUT IS NULL SO IT MEANS THE ROOM IS BOOKED FOR A LONG TIME

			if(chkindt.compareTo(checkodt)<0)
			{
				System.out.println("The room is booked till your check in data\n Please choose another room or date");
				return;
			}
				
			
			ResultSet rs = query.executeQuery("select reservation_seq.nextval from dual");
			while(rs.next())
			{
				nextid = rs.getInt(1);
			} 
			PreparedStatement st = connection.prepareStatement("insert into reservation(resid, rmethod, rdatetime, cid) values (? ,?, current_timestamp, ?)");
			st.setInt(1, nextid);
			st.setString(2, resmethod);
			st.setInt(3, cid);
			st.executeUpdate();
	
//			PreparedStatement st = connection.prepareStatement("select count(*) from room where avail='F' and (hid, rnumber) in (select r_hid, r_rnumber from reserves where r_checkindt >= to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') AND to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')<=r_checkoutdt)");
			st = connection.prepareStatement("insert into reserves(r_hid, r_rnumber, r_resid, r_checkindt, r_checkoutdt) values (?, ?, ?, to_timestamp(?,'YYYY-MM-DD HH24:MI:SS'), to_timestamp(?,'YYYY-MM-DD HH24:MI:SS'))");
			st.setInt(1, hid);
			st.setInt(2, rnumber);
			st.setInt(3, nextid);
			st.setString(4, checkindt);
			
			// CHECKS FOR THE CHECKOUT TIME IF IT IS NULL

			if(checkoutdt.equals("#"))
			{
				st.setNull(5, java.sql.Types.TIMESTAMP);
			}
			else
			{
				st.setString(5, checkoutdt);
			}
			st.executeUpdate();
			connection.commit();
			connection.setAutoCommit(true);
			System.out.println("Room "+ rnumber + " at Hotel " + hid + " assigned to customer " + cid + " between " + checkindt + " and " + checkoutdt+ ". Booking was done " + method);
		}
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
			try
			{
				connection.rollback();
				connection.setAutoCommit(true);
			}
			catch(SQLException se)
                        {
                                System.out.println(e.getMessage());
                        }
		}
	}
/**
 * 	This method releases a particular room.
 * 	@param rnumber the room number
 * 	@param hid the hotel id
 *  @param cid customer id
 * 	@param resid the reservation id
 * 	*/
	public void releaseRoom(int rnumber, int hid, int cid, int resid)
	{
		BillMaintainer bm=new BillMaintainer(connection);
		try
		{
			// UPDATES THE ROOM AVAILABILITY IN THE PARTICULAR HOTEL TO 'T'
			connection.setAutoCommit(false);
			bm.checkOutCustomer(cid,resid, 1);
			PreparedStatement st = connection.prepareStatement("update room set avail='T' where rnumber=? and hid=?");
			st.setInt(1, rnumber);
			st.setInt(2, hid);
			st.executeUpdate();
//			System.out.println("Occupancy by date range between "+ startdt+ " and " +enddt+" :\n");
			connection.commit();
			System.out.println("Room " + rnumber + " at Hotel " + hid + " released successfully");
			connection.setAutoCommit(true);
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
			try
			{
				connection.rollback();
				connection.setAutoCommit(true);
			}
			catch(SQLException se)
			{
				System.out.println(se.getMessage());
			}
		}	
	}
/**
 * 	This method inserts details about a  new customer into the customer table
 * 	@param cname the name of the customer
 * 	@param cemail the email id of the customer
 *  @param caddr the address of the customer
 *  @param cssn the social security number of the customer
 * 	@param cgender the gender of the customer
 *  @param cphone the phone number of the customer
 * 	*/

	public void addCustomer(String cname, String cemail, String caddr, String cssn, String cgender, String cphone)
	{
	    int cid=0;
		try
		{
			// THIS QUERY ADDS A NEW CUSTOMER AND CERTAIN FIELDS WHICH CAN BE NULL ARE ENTERED AS '#' WHICH ARE HANDLE BY JDBC SETNULL FOR EACH DATATYPE
			Statement query = connection.createStatement();

			ResultSet rs = query.executeQuery("select customerid_seq.nextval from dual");
			while(rs.next())
			{
				 cid = rs.getInt(1);
			}
	    
			PreparedStatement st = connection.prepareStatement("insert into customer(cid,cname,cemail,caddr,cssn,cgender,cphone) values (?,?,?,?,?,?,?)");
			
			st.setInt(1, cid);	
	
			if(cname.equals("#"))
			{
				st.setNull(2, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(2, cname);
			}

			if(cemail.equals("#"))
			{
			       st.setNull(3, java.sql.Types.VARCHAR); 
			}
			else
			{
				st.setString(3, cemail);
			}

			if(caddr.equals("#"))
			{
				st.setNull(4, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(4, caddr);
			}
			if(cssn.equals("#"))
			{
				st.setNull(5, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(5, cssn);
			}
			if(cgender.equals("#"))
			{
				st.setNull(6, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(6, cgender);
			}
			if(cphone.equals("#"))
			{
				st.setNull(7, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(7, cphone);
			}

			st.executeUpdate();

			System.out.println("The new customer details with customer id "+ cid + "is added successfully"); 
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}

	/**
 * 	This method inserts details about a  new staff into the staff table
 * 	@param sname the name of the staff
 * 	@param semail the email id of the staff
 *  @param saddr the address of the staff
 *  @param jobtitle the job title of the staff
 *  @param sage the age of the staff
 *  @param sphone the phone of the staff
 *  @param staffssn the social security number of the staff
 * 	@param sgender the gender of the staff
 *  @param salary the salary of the staff
 *  @param hid the hotel id
 *  @param did the department id
 * 	*/
	public void addStaff(String sname, String semail, String saddr, String jobtitle, int sage, String sphone, String sgender, String staffssn, float salary, int hid, int did)
	{
		
		// THIS QUERY ADDS A NEW STAFF AND CERTAIN FIELDS WHICH CAN BE NULL ARE ENTERED AS '#' WHICH ARE HANDLE BY JDBC SETNULL FOR EACH DATATYPE
		int staffid=0;
		try
		{
			Statement query = connection.createStatement();

			PreparedStatement st = connection.prepareStatement("insert into staff(staffid,sname,semail,saddr,jobtitle,sage,sphone,sgender,staffssn,salary,hid,did) values (?,?,?,?,?,?,?,?,?,?,?,?)");
			
			ResultSet rs = query.executeQuery("select staffid_seq.nextval from dual");
			while(rs.next())
			{
				staffid = rs.getInt(1);
			}
			
			st.setInt(1,staffid);

			if(sname.equals("#"))
			{
				st.setNull(2, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(2, sname);
			}
			
			if(semail.equals("#"))
			{
				st.setNull(3, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(3, semail);
			}

			if(saddr.equals("#"))
			{
				st.setNull(4, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(4, saddr);
			}
			
			if(jobtitle.equals("#"))
			{
				st.setNull(5, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(5, jobtitle);
			}

			if(sage < 0)
			{
				st.setNull(6, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(6, did);
			}


			if(sphone.equals("#"))
			{
				st.setNull(7, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(7, sphone);
			}

			if(sgender.equals("#"))
			{
				st.setNull(8, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(8, sgender);
			}

			if(staffssn.equals("#"))
			{
				st.setNull(9, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(9, sphone);
			}

			if(salary < 0)
			{
				st.setNull(10, java.sql.Types.FLOAT);
			}
			else
			{
				st.setFloat(10, salary);
			}

			if(hid < 0)
			{
				st.setNull(11, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(11, hid);
			}

			if(did < 0)
			{
				st.setNull(12, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(12, did);
			}

			st.executeUpdate();

			System.out.println("The new staff details with staff id "+ staffid + "is added successfully"); 
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
/**
 * 	This method inserts details about a  new room into the room table
 * 	@param hid the hotel id
 * 	@param rnumber the room number
 *  @param avail the availability status of the room of the customer
 *  @param category the category of the room
 * 	@param maxocc the maximum occupancy of the room
 
 * 	*/
	
	public void addRoom(int hid,int rnumber,String avail,String category,int maxocc)
	{
		 // THIS QUERY ADDS A NEW ROOM AND CERTAIN FIELDS WHICH CAN BE NULL ARE ENTERED AS '#' WHICH ARE HANDLE BY JDBC SETNULL FOR EACH DATATYPE

		try
		{
			PreparedStatement st = connection.prepareStatement("insert into room(hid,rnumber,avail,category,maxocc) values (?,?,?,?,?)");
			
			if(hid < 0)
			{
				st.setNull(1, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(1,hid);
			}

			if(rnumber < 0)
			{
				st.setNull(2, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(2, rnumber);
			}

			if(avail.equals("#"))
			{
				st.setNull(3, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(3, avail);
			}
			
			if(category.equals("#"))
			{
				st.setNull(4, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(4, category);
			}

			if(maxocc < 0)
			{
				st.setNull(5, java.sql.Types.INTEGER);
			}
			else
			{	
				st.setInt(5, maxocc);
			}
			st.executeUpdate();

			System.out.println("The new room with room number "+ rnumber + "in hotel with hotel id " +hid+ "is added successfully");

		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
/**
 * 	This method inserts details about category and nightlyrate of the room
 *  @param category the category of the room
 * 	@param nightlyrate the rate of the room per night the maximum occupancy of the room
 
 * 	*/
	public void addRoomCategory(String category, float nightlyrate)
	{
		
		 // THIS QUERY ADDS A NEW ROOM AND CERTAIN FIELDS WHICH CAN BE NULL ARE ENTERED AS '#' WHICH ARE HANDLE BY JDBC SETNULL FOR EACH DATATYPE

		try
		{
			PreparedStatement st = connection.prepareStatement("insert into room_category(category, nightlyrate) values (?,?)");
			if(category.equals("#"))
			{
				st.setNull(1, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(1, category);
			}

			if(nightlyrate < 0)
			{
				st.setNull(2, java.sql.Types.FLOAT);
			}
			else
			{
				st.setFloat(2, nightlyrate);
			}
			st.executeUpdate();

			System.out.println("Room category " + category + "with nightly rate " + nightlyrate +"is added successfully");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
/**
 * 	This method updates details about a customer in the customer table
 * 	@param cid the customer id
 * 	@param cname the customer name
 *  @param cemail the email id of the customer
 *  @param caddr the address of customer
 * 	@param cssn the social security number of the customer
 * 	@param cgender the gender of the customer
 *  @param cphone the phone number of the customer
 * 	*/
	public void updateCustomer(int cid, String cname, String cemail, String caddr, String cssn, String cgender, String cphone)
	{

		// THIS DOES A SELECT QUERY FOR PARTICULAR CUSTOMER ID AND GETS THAT ROW IN A RESULT SET

		try
		{
			Statement stmt = connection.createStatement();
			if(cid < 0)
			{
				System.out.println("Customer id is invalid");
				return;
			}

			String query = "select * from customer where cid ="+cid;

			ResultSet rs = stmt.executeQuery(query);

			rs.next();

			// FROM THE RESULT SET EACH ATTRIBUTE IS ASSIGNED TO EACH TEMP VARIABLE OF THAT DATATYPE
			String u_cname = rs.getString(2);
			if(rs.wasNull())
			{
				u_cname=null;			
			}	
			else if(cname.equals("~"))
			{
				cname=u_cname;
			}	
			
			String u_cemail = rs.getString(3);
			if(rs.wasNull())
			{
				u_cemail=null;			
			}
			else if(cemail.equals("~"))
			{
				cemail=u_cemail;
			}

			String u_caddr = rs.getString(4);
			if(rs.wasNull())
			{
				u_caddr=null;			
			}
			else if(caddr.equals("~"))
			{
				caddr=u_caddr;
			}
		
			String u_cssn = rs.getString(5);
			if(rs.wasNull())
			{
				u_cssn=null;
			}

			else if(cssn.equals("~"))
			{
				cssn=u_cssn;
			}


			String u_cgender = rs.getString(6);
			if(rs.wasNull())
			{
				u_cgender=null;
			}

			else if(cgender.equals("~"))
			{
				cgender=u_cgender;
			}			


			String u_cphone = rs.getString(7);
			if(rs.wasNull())
			{
				u_cphone=null;
			}

			else if(cphone.equals("~"))
			{
				cphone=u_cphone;
			}
			
			// IF THE FUNCTION PARAMETERS GIVE '~' AS VALUE JUST ASSIGNED BACK THE SAME RESULT SET ATTRIBUTE OR UPDATE THE NEW VALUE ENTERED
			PreparedStatement st = connection.prepareStatement("update customer set cname=?,cemail=?, caddr=?, cssn=?, cgender=?, cphone=? where cid=?");

			if(cname.equals("#") || u_cname == null)
			{
				st.setNull(1, java.sql.Types.VARCHAR);	
			}
			/*else if(u_cname == null && (!cname.equals("~")) && (!cname.equals("#")))
			{
				st.setString(1, cname);
			} */
			else
			{
				st.setString(1, cname);
			}

			if(cemail.equals("#") || u_cemail ==null)
			{
				st.setNull(2, java.sql.Types.VARCHAR);	
			}
			else
			{
				st.setString(2, cemail);
			}			
			
			if(caddr.equals("#") || u_caddr == null)
			{
				st.setNull(3, java.sql.Types.VARCHAR);	
			}
			else
			{
				st.setString(3, caddr);
			}

			if(cssn.equals("#") || u_cssn == null)
			{
				st.setNull(4, java.sql.Types.VARCHAR);	
			}
			else
			{
				st.setString(4, cssn);
			}
			
			if(cgender.equals("#") || u_cgender==null) 
			{
				st.setNull(5, java.sql.Types.VARCHAR);	
			}
			else
			{
				st.setString(5, cgender);
			}

			if(cphone.equals("#") || u_cphone==null)
			{
				st.setNull(6, java.sql.Types.VARCHAR);	
			}
			else
			{
				st.setString(6, cphone);
			}			

			st.setInt(7, cid);
			 
			st.executeUpdate();
			 
			System.out.println("Customer with customerid " + cid + "details are updated successfully");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
	
	/**
 * 	This method updates details about astaff into the staff table
 *  @param staffid the id of the staff
 * 	@param sname the name of the staff
 * 	@param semail the email id of the staff
 *  @param saddr the address of the staff
 *  @param jobtitle the job title of the staff
 *  @param sage the age of the staff
 *  @param sphone the phone of the staff
 *  @param staffssn the social security number of the staff
 * 	@param sgender the gender of the staff
 *  @param salary the salary of the staff
 *  @param hid the hotel id
 *  @param did the department id
 * 	*/

	public void updateStaff(int staffid, String sname, String semail, String saddr, String jobtitle, int sage, String sphone, String sgender, String staffssn, float salary, int hid, int did)
	{
		// THIS DOES A SELECT QUERY FOR PARTICULAR STAFF ID AND GETS THAT ROW IN A RESULT SET

		try
		{
			Statement stmt = connection.createStatement();
			if(staffid < 0)
			{
				System.out.println("StaffID is invalid");
				return;
			}

			String query = "select * from staff where staffid ="+staffid;
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			
			// FROM THE RESULT SET EACH ATTRIBUTE IS ASSIGNED TO EACH TEMP VARIABLE OF THAT DATATYPE

			String u_sname = rs.getString(2);
			if(rs.wasNull())
			{
				u_sname=null;
			}
			else if(sname.equals("~"))
			{
				sname=u_sname;
			}

			String u_semail = rs.getString(3);
			if(rs.wasNull())
			{
				u_semail=null;
			}
			else if(semail.equals("~"))
			{
				semail=u_semail;
			}

			String u_saddr = rs.getString(4);
			if(rs.wasNull())
			{
				u_saddr=null;
			}
			else if(saddr.equals("~"))
			{
				saddr=u_saddr;
			}

			String u_jobtitle = rs.getString(5);
			if(rs.wasNull())
			{
				u_jobtitle=null;
			}
			else if(jobtitle.equals("~"))
			{
				jobtitle=u_jobtitle;
			}

			int u_sage = rs.getInt(6);
			if(rs.wasNull())
			{
				u_sage=-6666;
			}
			else if(sage == -1111)
			{
				sage=u_sage;
			}

			String u_sphone = rs.getString(7);
			if(rs.wasNull())
			{
				u_sphone=null;
			}
			else if(sphone.equals("~"))
			{
				sphone=u_sphone;
			}

			String u_sgender = rs.getString(8);
			if(rs.wasNull())
			{
				u_sgender=null;
			}
			else if(sgender.equals("~"))
			{
				sgender=u_sgender;
			}

			String u_staffssn = rs.getString(9);
			if(rs.wasNull())
			{
				u_staffssn=null;
			}
			else if(staffssn.equals("~"))
			{
				staffssn=u_staffssn;
			}

			float u_salary = rs.getFloat(10);
			if(rs.wasNull())
			{
				u_salary=-6666;
			}
			else if(salary == -1111)
			{
				salary=u_salary;
			}

			int u_hid = rs.getInt(11);
			if(rs.wasNull())
			{
				u_hid=-6666;
			}
			else if(hid == -1111)
			{
				hid=u_hid;
			}

			int u_did = rs.getInt(12);
			if(rs.wasNull())
			{
				u_did=-6666;
			}
			else if(did == -1111)
			{
				did=u_did;
			}

				
			// IF THE FUNCTION PARAMETERS GIVE '~' AS VALUE JUST ASSIGNED BACK THE SAME RESULT SET ATTRIBUTE OR UPDATE THE NEW VALUE ENTERED
			
			PreparedStatement st = connection.prepareStatement("update staff set sname=?,  semail=?,  saddr=?,  jobtitle=?,  sage=?,  sphone=?,  sgender=?, staffssn=?,  salary=?,	hid=?,	did=? where staffid=?");
			
			if(sname.equals("#") || u_sname ==null)
			{
				st.setNull(1, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(1, sname);
			}

			if(semail.equals("#") || u_semail ==null)
			{
				st.setNull(2, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(2, semail);
			}

			if(saddr.equals("#") || u_saddr ==null)
			{
				st.setNull(3, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(3, saddr);
			}

			if(jobtitle.equals("#") || u_jobtitle ==null)
			{
				st.setNull(4, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(4, jobtitle);
			}

			if(sage==-6666 || sage ==-9999)
			{
				st.setNull(5, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(5, sage);
			}


			if(sphone.equals("#") || u_sphone ==null)
			{
				st.setNull(6, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(6, sphone);
			}


			if(sgender.equals("#") || u_sgender ==null)
			{
				st.setNull(7, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(7, sgender);
			}

			if(staffssn.equals("#") || u_staffssn ==null)
			{
				st.setNull(8, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(8, staffssn);
			}

			if(salary==-6666 || salary ==-9999)
			{
				st.setNull(9, java.sql.Types.FLOAT);
			}
			else
			{
				st.setFloat(9, salary);
			}

			
			if(hid==-6666 || hid ==-9999)
			{
				st.setNull(10, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(10, hid);
			}

			if(did==-6666 || did ==-9999)
			{
				st.setNull(11, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(11, did);
			}

			
			st.setInt(12, staffid);
			st.executeUpdate();
			System.out.println("Staff with staffid " + staffid + "details are updated successfully");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
	/**
 * 	This method updates details about the room inthe room table
 * 	@param hid the hotel id
 * 	@param rnumber the room number
 *  @param avail the availability status of the room of the customer
 *  @param category the category of the room
 * 	@param maxocc the maximum occupancy of the room
 
 * 	*/
	
	public void updateRoom(int hid,int rnumber,String avail,String category,int maxocc)
	{
		// THIS DOES A SELECT QUERY FOR PARTICULAR ROOM ID IN A HOTEL AND GETS THAT ROW IN A RESULT SET

		try
		{
			Statement stmt = connection.createStatement();
			if(hid < 0)
			{
				System.out.println("HotelID is invalid");
				return;
			}
			if(rnumber < 0)
			{
				System.out.println("Room number is invalid");
				return;
			}
			String query = "select * from room where hid="+hid+"and rnumber="+rnumber;
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			

			// FROM THE RESULT SET EACH ATTRIBUTE IS ASSIGNED TO EACH TEMP VARIABLE OF THAT DATATYPE

			String u_avail = rs.getString(3);
			if(rs.wasNull())
			{
				u_avail=null;
			}
			else if(avail.equals("~"))
			{
				avail=u_avail;
			}

			String u_category = rs.getString(4);
			if(rs.wasNull())
			{
				u_category=null;
			}
			else if(avail.equals("~"))
			{
				category=u_category;
			}

			int u_maxocc = rs.getInt(5);
			if(rs.wasNull())
			{
				u_maxocc=-6666;
			}
			else if(maxocc == -1111)
			{
				maxocc=u_maxocc;
			}


			// IF THE FUNCTION PARAMETERS GIVE '~' AS VALUE JUST ASSIGNED BACK THE SAME RESULT SET ATTRIBUTE OR UPDATE THE NEW VALUE ENTERED

			PreparedStatement st = connection.prepareStatement("update room set avail=?, category=?, maxocc=? where rnumber=? and hid=?");
			if(avail.equals("#") || u_avail ==null)
			{
				st.setNull(1, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(1, avail);
			}

			if(category.equals("#") || u_category ==null)
			{
				st.setNull(2, java.sql.Types.VARCHAR);
			}
			else
			{
				st.setString(2, category);
			}

			if(maxocc ==-6666 || maxocc ==-9999)
			{
				st.setNull(3, java.sql.Types.INTEGER);
			}
			else
			{
				st.setInt(3, maxocc);
			}
			
			st.setInt(4, rnumber);
			st.setInt(5, hid);
			st.executeUpdate();
			System.out.println("Room " + rnumber + " at Hotel " + hid + " details are updated successfully");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
/**
 * 	This method updates details about category and nightlyrate of the room
 *  @param category the category of the room
 * 	@param nightlyrate the rate of the room per night the maximum occupancy of the room
 
 * 	*/

	public void updateRoomCategory(String category, float nightlyrate)
	{
		// THIS DOES A SELECT QUERY FOR PARTICULAR ROOM CATEGORY AND GETS THAT ROW IN A RESULT SET

		try
		{
			Statement stmt = connection.createStatement();
			if(category.equals("#") || category.equals("~"))
			{
				System.out.println("CategoryID is invalid");
				return;
			}
			
			String query = "select * from room_category where category like '"+category+"'";
			ResultSet rs = stmt.executeQuery(query);
			rs.next();
			
			// FROM THE RESULT SET EACH ATTRIBUTE IS ASSIGNED TO EACH TEMP VARIABLE OF THAT DATATYPE
			
			float u_nightlyrate = rs.getFloat(2);
			if(rs.wasNull())
			{
				u_nightlyrate=-6666;
			}
			else if(nightlyrate==-1111)
			{
				nightlyrate=u_nightlyrate;
			}

			// IF THE FUNCTION PARAMETERS GIVE '~' AS VALUE JUST ASSIGNED BACK THE SAME RESULT SET ATTRIBUTE OR UPDATE THE NEW VALUE ENTERED

			PreparedStatement st = connection.prepareStatement("update room_category set nightlyrate=? where category=?");
			
			if(nightlyrate==-6666 || nightlyrate ==-9999)
			{
				st.setNull(1, java.sql.Types.FLOAT);
			}
			else
			{
				st.setFloat(1, nightlyrate);
			}

			st.setString(2,category);
			st.executeUpdate();
			System.out.println("Room category" + category + "details are updated successfully");
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}

/**
 * 	This method removes a customer from the customer table.
 *  @param cid customer id
 
* 	*/

	public void removeCustomer(int cid)
	{	
		// THIS EXECUTES QUERY TO REMOVE A CUSTOMER BASED ON A PARTICULAR CUSTOMER ID

		try
		{
			PreparedStatement st = connection.prepareStatement("delete from customer where cid =?");
			st.setInt(1, cid);
			st.executeUpdate();
			System.out.println("Customer with customer id " + cid  + " removed successfully");

		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}
/* 	This method removes a staff from the staff table.
 *  @param staff id staff id
 
* 	*/
	public void removeStaff(int staffid)
	{
		// THIS EXECUTES QUERY TO REMOVE A STAFF BASED ON A PARTICULAR STAFF ID

		try
		{
			PreparedStatement st = connection.prepareStatement("delete from staff where staffid=?");
			st.setInt(1, staffid);
			st.executeUpdate();
			System.out.println("Staff with staffid " + staffid  + " removed successfully");

		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}
/* 	This method removes a room from a room table.
 *  @param hid hotel id
 *  @param rnumber room number
 
* 	*/
	public void removeRoom(int hid, int rnumber)
	{
		// THIS EXECUTES QUERY TO REMOVE A ROOM IN A HOTEL BASED ON A PARTICULAR ROOM AND HOTEL ID

		try
		{
			PreparedStatement st = connection.prepareStatement("delete from room where hid =? and rnumber=?");
			st.setInt(1, hid);
			st.setInt(2, rnumber);
			st.executeUpdate();
			System.out.println("Room with room number " + rnumber+ "in hotel with hid" + hid +" removed successfully");

		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}
	
	/* 	This method removes a room category from the room_category table.
 *  @param category : the room category
 
 
* 	*/
	public void removeRoomCategory(String category)
	{
		// THIS EXECUTES QUERY TO REMOVE A ROOM CATEGORY ON A PARTICULAR CATEGORY NAME

		try
		{
			PreparedStatement st = connection.prepareStatement("delete from room_category where category=?");
			st.setString(1, category);
			st.executeUpdate();
			System.out.println("Room category " + category + " removed successfully");
			PreparedStatement st1 = connection.prepareStatement("delete from hotel_category where category=?");
			st1.setString(1, category);
			st1.executeUpdate();

		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}

	}

}
