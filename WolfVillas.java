
/* TEAM B
 * Hotel Management System 
 * 
 * Team members
	

 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

/**
 * The driver class for the WolfVillas application
 */
public class WolfVillas {
	private static final String jdbcURL = "jdbc:oracle:thin:@ora.csc.ncsu.edu:1523:orcl";

	// Put your oracle ID and password here
	private static final String user = "svpendse";
	private static final String password = "dbmsenter";


	private static Connection connection;

	/**
 * 	The main method
 * 	@param args The command line arguments if any
 * 	*/
	public static void main(String[] args) throws Exception {


		try {

			BufferedReader br = new BufferedReader( new InputStreamReader( System.in ) );
/*			Class.forName("oracle.jdbc.driver.OracleDriver");


			connection = null;
			connection = DriverManager.getConnection(jdbcURL, user, password);
*/
			start();

			while(true){

				//give options for menu.
				System.out.println("Choose a category \n");
				System.out.println("1. Information Processing \n");
				System.out.println("2. Bill and Service Maintenance \n");
				System.out.println("3. Reports \n");
				System.out.println("4. View a table \n");
				System.out.println("Type -1 to exit \n");

				System.out.print("\nEnter your choice : ");
				//take input and call corresponding function.
				int ch = Integer.parseInt(br.readLine());

				if(ch == -1)
				{
					System.out.println("Exiting application..");
					break;			

				}
				switch(ch)
				{
				case 1: 
					InfoProcessor ip = new InfoProcessor(connection);
//					ip.addcustomer(105, "Michael Hendrix", "michael.hendrix@teleworm.com", "591, Wyndmere Drive, Southborough, MA, 01752", "932412344", "M", "9249123424");
//					ip.updatecustomer("9192529713",104);
//					ip.removecustomer(105);
//					ip.checkAvailableRooms();
//					ip.checkAvailableRoomsByHotel(150);
					System.out.println("1. Assign room to customer");
					System.out.println("2. Release a room");
					System.out.println("3. Check available rooms across hotels");
				       	System.out.println("4. Check available rooms at a particular hotel branch");
					System.out.println("5. Add new customer");
					System.out.println("6. Add new staff");
					System.out.println("7. Add new room");
					System.out.println("8. Add new room category");
					System.out.println("9. Update customer information");
					System.out.println("10. Update staff information");
					System.out.println("11. Update room information");
					System.out.println("12. Update a room category");
					System.out.println("13. Delete customer");
					System.out.println("14. Delete staff");
					System.out.println("15. Delete room");
					System.out.println("16. Delete a room category");
					System.out.print("Enter your choice : ");
					ch = Integer.parseInt(br.readLine());
					if(ch==1)
					{
						System.out.print("Enter customer id : ");
						int custid = Integer.parseInt(br.readLine());
						System.out.print("Enter start date (YYYY-MM-DD HH24:MI:SS): ");
						String startdate = br.readLine();
						System.out.println("Enter # to insert null value for end date");
						System.out.print("Enter end date (YYYY-MM-DD HH24:MI:SS): ");
						String enddate = br.readLine();
						System.out.print("Enter reservation method : ");
						String resmethod = br.readLine();
						System.out.print("Enter the hotel id : ");
						int hid = Integer.parseInt(br.readLine());
						System.out.print("Enter the room number : ");
						int rnumber = Integer.parseInt(br.readLine());
						ip.assignRoomToCustomer(custid, startdate, enddate, resmethod, hid, rnumber);
//						ip.assignRoomToCustomer(101, "2012-01-11 10:00:00", "2012-04-13 10:00:00", "O", 150, 2001); 
					}
					else if(ch==2)
					{
						System.out.print("Enter the hotel id : ");
						int hid = Integer.parseInt(br.readLine());
						System.out.print("Enter the room number : ");
						int rnumber = Integer.parseInt(br.readLine());
						System.out.print("Enter the customer id : ");
                                                int cid = Integer.parseInt(br.readLine());
                                                System.out.print("Enter the reservation id : ");
						int resid = Integer.parseInt(br.readLine());
						ip.releaseRoom(hid, rnumber, cid, resid);
//						ip.releaseRoom(2001, 150);
					}
					else if(ch==3)
					{
						ip.checkAvailableRooms();	
					}
					else if(ch==4)
					{
						System.out.println("Enter the hotel id : ");
						int hid = Integer.parseInt(br.readLine());
						ip.checkAvailableRoomsByHotel(hid);
					}
					else if(ch==5)
					{
						//public void addCustomer(int cid, String cname, String cemail, String caddr, String cssn, String cgender, String cphone)
						//Add new customer
						System.out.println("Enter '#' to insert null valuesi\n");
						System.out.print("Enter customer name : ");
						String cname = br.readLine();
						System.out.print("Enter customer email : ");
						String cemail = br.readLine();
						System.out.print("Enter customer address : ");
						String caddr = br.readLine();
						System.out.print("Enter customer ssn : ");
						String cssn = br.readLine();
						System.out.print("Enter customer gender : ");
						String cgender = br.readLine();
						System.out.print("Enter customer phone : ");
						String cphone = br.readLine();
						ip.addCustomer(cname, cemail, caddr, cssn, cgender, cphone);
					}
					else if(ch==6)
					{
						//Add new staff
						//public void addStaff(int staffid, String sname, String semail, String saddr, String jobtitle, String sphone, String sgender, String staffssn, float salary, int hid, int sage)
						int hid,sage,did;
						float salary;
						System.out.println("Enter '#' to insert null valuesi\n");
						System.out.print("Enter staff name : ");
						String sname = br.readLine();
						System.out.print("Enter staff email : ");
						String semail = br.readLine();
						System.out.print("Enter staff address : ");
						String saddr = br.readLine();
						System.out.print("Enter job title : ");
						String jobtitle = br.readLine();
						System.out.print("Enter staff age : ");
                                                String str = br.readLine();
                                                if(str.equals("#"))
                                                {
                                                        sage = -9999;
                                                }
                                                else
                                                {
                                                        sage = Integer.parseInt(str);
                                                }


						System.out.print("Enter staff phone : ");
						String sphone = br.readLine();
						System.out.print("Enter staff gender : ");
						String sgender = br.readLine();
						System.out.print("Enter staff ssn : ");
						String sssn = br.readLine();
						System.out.print("Enter staff salary : ");
						str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        salary = -9999;
                                                }
                                                else
                                                {
                                                        salary = Float.parseFloat(str);
                                                }
						System.out.print("Enter hotel id : ");
						str = br.readLine();
                                                if(str.equals("#"))
                                                {
                                                        hid = -9999;
                                                }
                                                else
                                                {
                                                        hid = Integer.parseInt(str);
                                                }
						
						System.out.print("Enter department id : ");
                                                str = br.readLine();
                                                if(str.equals("#"))
                                                {
                                                        did = -9999;
                                                }
                                                else
                                                {
                                                        did = Integer.parseInt(str);
                                                }


						ip.addStaff(sname, semail, saddr, jobtitle, sage, sphone, sgender, sssn, salary, hid, did);
					}
					else if(ch==7)
					{
						//Add new room
						//public void addRoom(int hid,int rnumber,String avail,String category)
						int hid,rnumber,maxoccupancy;
						System.out.println("Enter '#' to insert null valuesi\n");
						System.out.print("Enter hotel id : ");
						String str = br.readLine();
						if(str.equals("#"))
						{
							hid = -9999;
						}
						else
						{
							hid = Integer.parseInt(str);
						}
						System.out.print("Enter room number : ");
						str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        rnumber = -9999;
                                                }
                                                else
                                                {
                                                        rnumber = Integer.parseInt(str);
                                                }
						System.out.print("Enter room availability : ");
						String avail = br.readLine();
						System.out.print("Enter room category : ");
						String category = br.readLine();
						System.out.print("Enter maximum occupancy : ");
                                                str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        maxoccupancy = -9999;
                                                }
                                                else
                                                {
                                                        maxoccupancy = Integer.parseInt(str);
                                                }

						ip.addRoom(hid, rnumber, avail, category,maxoccupancy);
					}
					else if(ch==8)
					{

						//public void addRoomCategory(String category, float nightlyrate, int maxoccupancy)
						//Add new room category
						float nightlyrate;
						System.out.println("Enter '#' to insert null values\n");	
						System.out.print("Enter room category : ");
						String category = br.readLine();
						System.out.print("Enter nightly rate : ");
						String str = br.readLine();
						if(str.equals("#"))
						{
							nightlyrate = -9999;
						}
						else
						{
							nightlyrate = Float.parseFloat(str);
						}
						ip.addRoomCategory(category, nightlyrate);
					}
					else if(ch==9)
					{
						//Update customer
						int cid;
						System.out.println("Enter '#' to insert null values and enter '~' if you dont want to update the field\n");
						System.out.print("Enter customer id of whose details needs to be updated: ");
						String str = br.readLine();
						if(str.equals("#"))
						{
							cid=-9999;
						}
						else if(str.equals("~")) 
						{
							cid=-1111;
						}
						else
						{
                                                	cid = Integer.parseInt(str);
                                                }
						System.out.print("Enter customer name to update: ");
                                                String cname = br.readLine();
                                                System.out.print("Enter customer email to update: ");
                                                String cemail = br.readLine();
                                                System.out.print("Enter customer address to update: ");
                                                String caddr = br.readLine();
                                                System.out.print("Enter customer ssn to update: ");
                                                String cssn = br.readLine();
                                                System.out.print("Enter customer gender to update: ");
                                                String cgender = br.readLine();
                                                System.out.print("Enter customer phone to update: ");
                                                String cphone = br.readLine();
                                                ip.updateCustomer(cid, cname, cemail, caddr, cssn, cgender, cphone);

					}
					else if(ch==10)
					{
						//Update staff information
						int staffid,hid,did,sage;
						float salary;
						System.out.println("Enter '#' to insert null values and enter '~' if you dont want to update the field\n");
						
						System.out.print("Enter staff id to be updated: ");
                                                String str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        staffid=-9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        staffid=-1111;
                                                }
                                                else
                                                {
                                                        staffid = Integer.parseInt(str);
                                                }

                                                System.out.print("Enter staff name to update: ");
                                                String sname = br.readLine();
                                                System.out.print("Enter staff email to update: ");
                                                String semail = br.readLine();
                                                System.out.print("Enter staff address to update: ");
                                                String saddr = br.readLine();
                                                System.out.print("Enter job title to update: ");
                                                String jobtitle = br.readLine();

						System.out.print("Enter staff age to update: ");
                                                str = br.readLine();
                                                if(str.equals("#"))
                                                {
                                                        sage=-9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        sage=-1111;
                                                }
                                                else
                                                {
                                                        sage = Integer.parseInt(str);
                                                }

                                                System.out.print("Enter staff phone to update: ");
                                                String sphone = br.readLine();
                                                System.out.print("Enter staff gender to update: ");
                                                String sgender = br.readLine();
                                                System.out.print("Enter staff ssn to update: ");
                                                String sssn = br.readLine();
                                                
						System.out.print("Enter staff salary to update: ");
                                                str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        salary=-9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        salary=-1111;
                                                }
                                                else
                                                {
							salary = Float.parseFloat(str);
                                               	}
 
						System.out.print("Enter hotel id to update:");
						str = br.readLine();
						if(str.equals("#"))
                                                {       
                                                        hid=-9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        hid=-1111;
                                                }
                                                else
                                                {
                                                	hid = Integer.parseInt(str);
						}
                                                
						System.out.print("Enter department id to update: ");
                                                str = br.readLine();
                                                if(str.equals("#"))
                                                {       
                                                        did=-9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        did=-1111;
                                                }
                                                else
                                                {
							did = Integer.parseInt(str);
						}
                                                ip.updateStaff(staffid, sname, semail, saddr, jobtitle, sage, sphone, sgender, sssn, salary, hid, did);
					}
					else if(ch==11)
					{
						//Update room information
						int hid,rnumber,maxoccupancy;
						System.out.println("Enter '#' to insert null values and enter '~' if you dont want to update the field\n");
						System.out.print("Enter hotel id to be updated: ");
                                                String str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        hid = -9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        hid = -1111;
                                                }
                                                else
                                                {
							hid = Integer.parseInt(str);
						}

                                                System.out.print("Enter room number to be updated: ");
                                                str = br.readLine();
						if(str.equals("#"))
                                                {
                                                        rnumber = -9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        rnumber = -1111;
                                                }
                                                else
                                                {
                                                        rnumber = Integer.parseInt(str);
                                                }

						System.out.print("Enter room availability to update: ");
                                                String avail = br.readLine();
                                                System.out.print("Enter room category to update: ");
                                                String category = br.readLine();
                                                
						System.out.print("Enter maximum occupancy to update: ");
                                               	str = br.readLine();
                                                if(str.equals("#"))
                                                {
                                                        maxoccupancy = -9999;
                                                }
                                                else if(str.equals("~"))
                                                {
                                                        maxoccupancy = -1111;
                                                }
                                                else
                                                {
                                                        maxoccupancy = Integer.parseInt(str);
                                                }

                                                ip.updateRoom(hid, rnumber, avail, category,maxoccupancy);

					}
					else if(ch==12)
					{
						//Update a room category
						float nightlyrate;
						System.out.println("Enter '#' to insert null values and enter '~' if you dont want to update the field\n");
						System.out.print("Enter room category name who details needs to be updated: ");
                                                String category = br.readLine();
                                                System.out.print("Enter nightly rate to update");
						String str = br.readLine();
						if(str.equals("#"))
						{
							nightlyrate = -9999;
						}
						else if(str.equals("~"))
						{
							nightlyrate = -1111;
						}
						else
						{
							nightlyrate = Float.parseFloat(str);
                                                }
						ip.updateRoomCategory(category, nightlyrate);
					}
					
					else if(ch==13)
					{
						//Delete a customer
						System.out.print("Enter customer id : ");
						int cid = Integer.parseInt(br.readLine());
						ip.removeCustomer(cid);
					}
					else if(ch==14)
					{
						//Delete staff
						System.out.print("Enter staff id : ");
						int staffid = Integer.parseInt(br.readLine());
						ip.removeStaff(staffid);
					}
					else if(ch==15)
					{
						//Delete room
						System.out.print("Enter hotel id : ");
						int hid = Integer.parseInt(br.readLine());
						System.out.print("Enter room number : ");
						int rnumber = Integer.parseInt(br.readLine());
						ip.removeRoom(hid, rnumber);
					}
					else if(ch==16)
					{
						//Delete room category
						System.out.print("Enter room category : ");
						String category = br.readLine();
						ip.removeRoomCategory(category);
					}
					else
					{
						System.out.println("Invalid choice...\n");
					} 

	//				infProcessing();
				break;  
				/*case 2:
					ServiceMaintainer sm = new ServiceMaintainer(connection);
					System.out.print("Enter hotel id : ");
					int hid = Integer.parseInt(br.readLine());
					System.out.print("Enter room number : ");
					int rnumber = Integer.parseInt(br.readLine());
					System.out.println("Enter service id : ");
					int serviceid = Integer.parseInt(br.readLine());
					System.out.println("Enter particular id : ");
					int partid = Integer.parseInt(br.readLine());
					System.out.println("Enter order count : ");
					int ordercount = Integer.parseInt(br.readLine());
					sm.insertService(hid, rnumber, serviceid, partid, ordercount);
					//sm.insertService(150,2001,501,805,4);
	//				 maintainservicerecords();
				break;*/
				case 2:
					System.out.println("1. Add new order");
					System.out.println("2. Display bill for customer/reservation");
					//System.out.println("3. Check out customer");
					System.out.print("Enter your choice : ");
					ch = Integer.parseInt(br.readLine());
					BillMaintainer bm = new BillMaintainer(connection);
					if(ch==1)
					{
						bm = new BillMaintainer(connection);
						System.out.print("Enter the hotel id : ");
						int hid = Integer.parseInt(br.readLine());
						System.out.print("Enter the room number : ");
						int rnumber = Integer.parseInt(br.readLine());
						System.out.print("Enter the customer id : ");
						int cid = Integer.parseInt(br.readLine());
						System.out.print("Enter the reservation id : ");
						int resid = Integer.parseInt(br.readLine());
						System.out.print("Enter the service id : ");
						int service_id = Integer.parseInt(br.readLine());
						System.out.print("Enter the particular id : ");
						int part_id = Integer.parseInt(br.readLine());
						System.out.print("Enter the number of units : ");
						int ordercount = Integer.parseInt(br.readLine());
						bm.insertNewOrder(hid, rnumber, cid, resid, service_id, part_id, ordercount); 

					}
					else if(ch==2)
					{
						bm = new BillMaintainer(connection);
						System.out.print("Enter the customer id for which you want to generate the bill : ");
						int custid = Integer.parseInt(br.readLine());
						System.out.print("Enter the reservation id for which you want to generate the bill : ");
						int resid = Integer.parseInt(br.readLine());
						System.out.println("1. Cumulative bill");
						System.out.println("2. Itemized bill");
						System.out.print("Enter your choice : ");
						int option = Integer.parseInt(br.readLine());
						if(option==1)
							bm.displayBill(custid, resid);
						else if(option==2)
							bm.displayItemizedBill(custid, resid);
					}
					/*else if(ch==3)
					{
						bm = new BillMaintainer(connection);
						System.out.print("Enter the customer id for which you want to generate the bill : ");
						int custid = Integer.parseInt(br.readLine());
						System.out.print("Enter the reservation id for which you want to generate the bill : ");
						int resid = Integer.parseInt(br.readLine());
						bm.checkOutCustomer(custid, resid);	
					}*/
					else
					{
						System.out.println("Invalid choice....\n");
					}
//					maintainbillaccounts();
					break;
				case 3: 
					ReportGenerator rg = new ReportGenerator(connection);
					//rg.reportPercentRoomsOccupied();
					System.out.println("1. Report staff by role");
					System.out.println("2. Report occupancy by hotel");
					System.out.println("3. Report occupancy by room type");
					System.out.println("4. Report occupancy by date range");
					System.out.println("5. Report occupants");
					System.out.println("6. Report customers by staff");
					System.out.print("Enter your choice : ");	
					ch = Integer.parseInt(br.readLine());
					if(ch==1)
					{
						System.out.print("Enter the type of staff : ");
						String staffrole = br.readLine();
						//Validation required
						rg.reportStaffByRole(staffrole);
					}
					else if(ch==2)
					{
						rg.reportOccByHotel();
					}
					else if(ch==3)
					{
						rg.reportOccByRoomType();
					}
					else if(ch==4)
					{
						System.out.print("Enter the start date (YYYY-MM-DD HH24:MI:SS): ");
						String startdate = br.readLine();
						System.out.print("Enter the end date (YYYY-MM-DD HH24:MI:SS): ");
						String enddate = br.readLine();
						rg.reportOccByDateRange(startdate, enddate);
					}
					else if(ch==5)
					{
						System.out.print("Enter the start date (YYYY-MM-DD HH24:MI:SS): ");
						String startdate = br.readLine();
						System.out.print("Enter the end date (YYYY-MM-DD HH24:MI:SS): ");
						String enddate = br.readLine();
						rg.reportOccupants( startdate, enddate);
						//rg.reportOccupants("2012-01-11 10:00:00","2012-01-13 10:00:00");
					}
					else if(ch==6)
					{
						System.out.print("Enter the staff id : ");
						int staffid = Integer.parseInt(br.readLine());
						/*System.out.print("Enter the start date (YYYY-MM-DD HH24:MI:SS): ");
						String startdate = br.readLine();
						System.out.print("Enter the end date (YYYY-MM-DD HH24:MI:SS): ");
						String enddate = br.readLine();*/
						rg.reportCustomerByStaff(staffid);
//						rg.reportCustomerByStaff(211, "2012-01-11 10:00:00", "2012-04-13 10:00:00");
					}

//					reports();
				break;
				case 4:
					System.out.print("Enter the table name : ");
					String tablename = br.readLine();
					InfoProcessor ip2 = new InfoProcessor(connection);
					ip2.viewTable(tablename);
				break;
				default:
					System.out.println("Enter a valid option");
				}

				// break;

			}     
			//close connection. 
			close(connection);

		} 
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

		catch(Exception e) {	
			e.printStackTrace();
		}

	}

	static void start() throws Exception
	{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			connection = null;
			connection = DriverManager.getConnection(jdbcURL, user, password);
	}
	static void close(Connection connection) {
		if(connection != null) {
			try { 
				connection.close(); 
			} catch(Throwable whatever) {}
		}
	}
	
	// Close statement and Result code
	static void close(Statement statement) {
		if(statement != null) {
			try { 
				statement.close(); 
			} catch(Throwable whatever) {}
		}
	}
	static void close(ResultSet result) {
		if(result != null) {
			try { 
				result.close(); 
			} catch(Throwable whatever) {}
		}
	}
}

