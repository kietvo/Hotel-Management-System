import java.util.Date;
import java.io.*;
import java.sql.*;
/**
 * Class to handle the bill processing
 */
public class BillMaintainer
{
	Connection connection;
	/**
 * 	BillMaintainer constructor specifying the connection object
 * 	@param connection The JDBC connection object
 * 	*/
	public BillMaintainer(Connection connection)
	{
		this.connection = connection;
	}
	/**
 * 	Method to process checkout of a customer
 * 	@param cid The customer id
 * 	@param resid The reservation id
 * 	@param checkout A flag to indicate whether the customer is actually checking out, or just querying
 * 	*/
	public void checkOutCustomer(int cid, int resid, int checkout)
	{
		/*CHECK OUT A CUSTOMER OR SIMULATE THE CHECKOUT PROCESS AS INDICATED BY THE CHECKOUT
	*	FLAG*/
		try
		{
//			System.out.println("checkout value : " + checkout);
//
//			GET THE CHECK IN, CHECK OUT DATES FOR THE GIVEN CUSTOMER AND RESERVATION
			PreparedStatement st = connection.prepareStatement("select re.r_checkindt, re.r_checkoutdt from room r, reserves re, reservation res where r.hid = re.r_hid and r.rnumber = re.r_rnumber and re.r_resid = res.resid and res.resid = ? and res.cid = ?");
			st.setInt(1, resid);
			st.setInt(2, cid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
				count++;
			
			//CHECK IF THE GIVEN RESERVATION-CUSTOMER PAIR IS VALID
			if(count==0)
			{
				System.out.println("The reservation " + resid + " does not correspond to the customer " + cid);
				return;
			}
			else
			{
				rs = st.executeQuery();
				while(rs.next())
				{
					//CHECK WHETHER THE CUSTOMER HAS ALREADY CHECKED OUT
					Timestamp start = rs.getTimestamp(1);
					Timestamp end = rs.getTimestamp(2);
					if(!rs.wasNull())
					{
						Timestamp current = new Timestamp((new Date()).getTime());
						if(end.compareTo(current)<0)
						{
							System.out.println("Cannot process checkout. Customer already checked out\n");
							return;
						}
					}
				}	
			}
			//IF CHECK OUT IS 1, THEN CARRY OUT THE ACTUAL CHECKOUT PROCESS
			//REQUEST FOR THE PAYMENT METHOD AND THE CARD NUMBER
			String method = null, cardnumber = null;
			if(checkout==1)
			{
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				System.out.print("Enter the payment method : ");
				if(method.toLowerCase().equals("card"))
				{
					System.out.print("Enter the card number : ");
					cardnumber = br.readLine();
				}
				else if(method.toLowerCase().equals("cash"))
				{}
				else
				{
					System.out.println("Invalid payment method...\n");
					return;
				}
			}
			//GET THE APPROPRIATE BILLID (EITHER EXISTING OR A NEWLY GENERATED ID) 
			//CORRESPONDING TO THSI CUSTOMER AND RESERVATION
			int new_billid = -1;
			if(checkout==1)
				new_billid = manageBillEntryForCheckout(cid, resid, method, cardnumber);
			else
				new_billid = manageBillEntry(cid, resid);

			if(new_billid==-1)
				return;
			//UPDATE THE BILL ENTRY ACCORDINGLY
			updateBillEntry(new_billid, cid, resid);	
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	
	}
	/**
 * 	This method inserts a new order into the orders table.
 * 	@param hid the hotel id
 * 	@param rnumber the room number
 * 	@param cid the customer id
 * 	@param resid the reservation id
 * 	@param service_id the service id
 * 	@param part_id the particular id
 * 	@param ordercount the no. of units ordered
 * 	*/
	public void insertNewOrder(int hid, int rnumber, int cid, int resid, int service_id, int part_id, int ordercount)
	{
		try
		{
			//GET THE CHECK IN AND CHECK OUT DATES FOR GIVEN CUSTOMER, ROOM AND RESERVATION
			PreparedStatement st = connection.prepareStatement("select re.r_checkindt, re.r_checkoutdt from room r, reserves re, reservation res where r.hid = re.r_hid and r.rnumber = re.r_rnumber and re.r_resid = res.resid and r.hid = ? and r.rnumber = ? and res.resid = ? and res.cid = ?");
			st.setInt(1, hid);
			st.setInt(2, rnumber);
			st.setInt(3, resid);
			st.setInt(4, cid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
				count++;
			//CHECK WHETHER THE RESERVATION-CUSTOMER PAIR IS VALID
			if(count==0)
			{
				System.out.println("No reservation made by customer " + cid + " for room " + rnumber + " in hotel "  + hid);
				return;
			}
			else
			{
				rs = st.executeQuery();
				while(rs.next())
				{
					Timestamp start = rs.getTimestamp(1);
					Timestamp end = rs.getTimestamp(2);
					if(!rs.wasNull())
					{
						//CHECK IF THE CUSTOMER HAS ALREADY CHECKED OUT
						Timestamp current = new Timestamp((new Date()).getTime());
						if(end.compareTo(current)<0)
						{
							System.out.println("Cannot place order. Customer already checked out\n");
							return;
						}
					}
				}	
			}
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                	return;
		}
		//GET THE BILL ID (EITHER EXISTING OR A NEWLY GENERATED)
		int new_billid = manageBillEntry(cid, resid);
		if(new_billid==-1)
			return;
		//INSTANTIATE A SERVICEMAINTAINER OBJECT TO POPULATE THE ORDERS AND THE
		//ORDER_PARTICULAR TABLES IN THE DATABASE
		ServiceMaintainer sm = new ServiceMaintainer(connection);
		sm.insertService(hid, rnumber, new_billid, service_id, part_id, ordercount);

		//UPDATE THE BILL ENTRY ACCORDINGLY 
		updateBillEntry(new_billid, cid, resid);
	}
	/**This method updates an existing bill entry in the bill table corresponding to a given customer and a reservation
 * 	@param billid The bill id
 * 	@param cid The customer id
 * 	@param resid The reservation id
 * 	*/
	public void updateBillEntry(int billid, int cid, int resid)
	{
		try
		{
			//GET THE ROOM AND THE RESERVATION DETAILS FROM THE RESERVES TABLE FOR THIS RESERVATION 
			PreparedStatement st = connection.prepareStatement("select r_resid, r_hid, r_rnumber, to_char(r_checkindt,'YYYY-MM-DD HH24:MI:SS'), to_char(r_checkoutdt,'YYYY-MM-DD HH24:MI:SS') from reserves where r_resid=?");
			st.setInt(1, resid);

			ResultSet rs = st.executeQuery();
			String checkindt, checkoutdt;
			int hid, rnumber;
			while(rs.next())
			{
				hid = rs.getInt(2);
				rnumber = rs.getInt(3);
				checkindt = rs.getString(4);
				checkoutdt = rs.getString(5);
				//GET THE IMEMIZED LIST OF ORDERS, THE ORDER COUNT AND THE RATE OF EACH UNIT FROM THE ORDERS, OREDER_PARTICULAR AND THE PARTICULARS RELATION
				PreparedStatement st2 = connection.prepareStatement("select o.orderid, p.partid, op.order_count, p.rate from orders o, particulars p, order_particular op where o.orderid = op.op_orderid and op.op_partid = p.partid and o.orderdt >= to_timestamp(?,'YYYY-MM-DD HH24:MI:SS')");
//and to_timestamp(?,'YYYY-MM-DD HH24:MI:SS') and o.hid=? and o.rnumber=?");
				st2.setString(1, checkindt);
//				st2.setString(2, checkoutdt);
//				(new InfoProcessor(connection)).viewTable("orders");	
				ResultSet rs2 = st2.executeQuery();
				
				//CALCULATE THE TOTAL AMOUNT
				float totalamount = 0;
				while(rs2.next())
				{
					totalamount += (rs2.getInt(3)*rs2.getFloat(4));
				}
				//ADD THE ROOM RENT
				totalamount += getRoomRent(resid);
//				System.out.println("totalamount = " + totalamount);

				//NOW UPDATE THE BILL TABLE USING THE SPECIFIED BILL ID
				PreparedStatement st3 = connection.prepareStatement("update bill set billamount=? where billid=?");

				st3.setFloat(1, totalamount);
				st3.setInt(2, billid);
				st3.executeUpdate();
			}
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	/**
 * 	This method returns the room rent corresponding to a reservation
 * 	@param resid The reservation id
 * 	@return The cumulative room rent for the reservation
 * 	*/
	public float getRoomRent(int resid)
	{
		float rent = 0;
		try
		{
			//GET THE RESERVATION, ROOM AND ROOM CATEGORY DETAILS FROM RESERVES, ROOM
			//AND THE ROOM CATEGORY TABLES
			PreparedStatement st = connection.prepareStatement("select res.r_resid, res.r_hid, res.r_rnumber, to_char(res.r_checkindt,'YYYY-MM-DD HH24:MI:SS'), to_char(res.r_checkoutdt,'YYYY-MM-DD HH24:MI:SS'), rc.nightlyrate from reserves res, room r, room_category rc where res.r_hid = r.hid and res.r_rnumber = r.rnumber and r.category = rc.category and res.r_resid=?");
			st.setInt(1, resid);			
			ResultSet rs = st.executeQuery();
			String start = "" , end = "";
			int hid = 0, rnumber = 0;
			float nightlyrate = 0.0f;
			boolean flag = false;
			while(rs.next())
			{
				start = rs.getString(4);
				end = rs.getString(5);
				if(rs.wasNull())
					flag = true;
				hid = rs.getInt(2);
				rnumber = rs.getInt(3);
				nightlyrate = rs.getFloat(6);
			}
		/*	while(rs.next())
			{
				start = rs.getString(4);
				end = rs.getString(5);
				long diff_in_millis = (end.getTime() - start.getTime());
				double diff_in_days = diff_in_millis * 60 * 60 * 24/1000;
				System.out.println(rs.getInt(1) + " " + rs.getInt(2) + " " + rs.getInt(3) + " " + start + " " + end + " " + rs.getFloat(6) + " " +diff_in_days);
			}*/
			if(flag)
			{
				//GET THE NUMBER OF DAYS OF ROOM OCCUPANCY UP TO THE 
				//CURRENT DATE IF THE CHECKOUT DATE IS NULL
				st = connection.prepareStatement("select current_date - to_date(?,'YYYY-MM-DD HH24:MI:SS') from dual"); 
				st.setString(1, start);
			}
			else
			{
				//OTHERWISE, GET THE NUMBER OF DAYS OF THE ROOM OCCUPANCY
				//USING THE CHECKOUT AND THE CHECKIN DATES
				st = connection.prepareStatement("select to_date(?,'YYYY-MM-DD HH24:MI:SS')  - to_date(?,'YYYY-MM-DD HH24:MI:SS') from dual");
				st.setString(1, end);
				st.setString(2, start);
			}
			ResultSet rs2 = st.executeQuery();
			while(rs2.next())
			{
				rent = (float)Math.floor(rs2.getFloat(1))*nightlyrate;
			}
//			System.out.println("diff : " + diff);
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

		return rent;
	}
	/**
 * 	This method manages the bill entry for checking out a customer
 * 	@param cid The customer id
 * 	@param resid The reservation id
 * 	@param paymentmethod The payment method
 * 	@param cardnumber The card number
 * 	@return A new or existing bill id corresponding to the given customer and reservation
 * 	*/
	public int manageBillEntryForCheckout(int cid, int resid, String paymentmethod, String cardnumber)
	{
		int billid = 0;
		try
		{
			/*PreparedStatement st = connection.prepareStatement("select * from reservation where resid=? and cid=?");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
				count++;
			if(count==0)
			{
				System.out.println("No matching reservation found for the customer");
				return -1;
			}	*/

			//GET THE TUPLE FROM THE BILL RELATION CORRESPONDING TO THE GIVEN
			//CUSTOMER AND RESERVATION
			PreparedStatement st = connection.prepareStatement("select * from bill where cid=? and b_resid=?");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
			{
				billid = rs.getInt(1);
				count++;
			}
			//IF NO TUPLE EXISTS, INSERT A NEW TUPLE INTO THE BILL TABLE
			if(count==0)
			{
				Statement sts = connection.createStatement();
				ResultSet temp = sts.executeQuery("select billid_seq.nextval from dual");
				while(temp.next())
				{
					billid = temp.getInt(1);
				}
				st = connection.prepareStatement("insert into bill(billid, billamount, paymentmethod, cardno, cid, b_resid, billtime) values (?, 0, ?, ?, ?, ?, current_timestamp)");

				st.setInt(1, billid);
				st.setString(2, paymentmethod);
				st.setString(3, cardnumber);
			        st.setInt(4, cid);
				st.setInt(5, resid);			  
				st.executeUpdate();
				System.out.println("new bill inserted with id " + billid);	
			}
			//OTHERWISE, UPDATE THE EXISTING BILL ENTRY WITH THE SPECIFIED ATTRIBUTES
			else
			{
				st = connection.prepareStatement("update bill set paymentmethod=?, cardno=? where billid=?");
				st.setString(1, paymentmethod);
				st.setString(2, cardnumber);
				st.setInt(3, billid);
				st.executeUpdate();
			}
		}
		catch(SQLException e)
                {
		//	e.printStackTrace();
                        System.out.println(e.getMessage());
		//	System.exit(0);
		}
		return billid;
	}

	/**This method manages a bill entry for a given customer and a reservation.If the corresponding credentials do not exist, a new entity is created in the bill table
 * 	@param cid The customer id
 * 	@param resid The reservation id
 * 	@param update Update flag
 * 	@return int The corresponding billid
 * 	*/
	public int manageBillEntry(int cid, int resid)
	{
		int billid = 0;
		try
		{
			/*PreparedStatement st = connection.prepareStatement("select * from reservation where resid=? and cid=?");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
				count++;
			if(count==0)
			{
				System.out.println("No matching reservation found for the customer");
				return -1;
			}	*/
			//GET THE TUPLE FROM THE BILL TABLE CORRESPONDING TO THE CUSTOMER
			//AND THE RESERVATION
			PreparedStatement st = connection.prepareStatement("select * from bill where cid=? and b_resid=?");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
			int count = 0;
			while(rs.next())
			{
				billid = rs.getInt(1);
				count++;
			}
			//IF NO TUPLE EXISTS, THEN INSERT A NEW TUPLE CORRESPONDING TO THIS CUSTOMER
			if(count==0)
			{
				Statement sts = connection.createStatement();
				ResultSet temp = sts.executeQuery("select billid_seq.nextval from dual");
				while(temp.next())
				{
					billid = temp.getInt(1);
				}
				st = connection.prepareStatement("insert into bill(billid, billamount, paymentmethod, cardno, cid, b_resid, billtime) values (?, 0,'undefined', 'undefined', ?, ?, current_timestamp)");
				st.setInt(1, billid);
			        st.setInt(2, cid);
				st.setInt(3, resid);			  
				st.executeUpdate();
				System.out.println("new bill inserted with id " + billid);	
			}
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
			System.exit(0);
		}
		return billid;
	}
	/**This method displays the bill corresponding to a given customer and a given reservation id
 * 	@param cid The customer id
 * 	@param resid The reservation id
 * 	*/
	public void displayBill(int cid, int resid)
	{
		try
		{
			System.out.println("CAlling from display bill...\n");
			
			//SIMULATE THE CHECKOUT TO UPDATE THE BILL TABLE
			this.checkOutCustomer(cid, resid, 0);

			//SELECT THE APPROPRIATE TUPLE FROM THE BILL TABLE
			PreparedStatement st = connection.prepareStatement("select billid, billamount, paymentmethod, cardno, cid, billtime, b_resid from bill where cid = ? and b_resid=?");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
//			System.out.println("Occupancy by date range between "+ startdt+ " and " +enddt+" :\n");
			//DISPLAY THE BILL
			System.out.println("Bill for customer " + cid);
			while(rs.next())
			{
				String cardno = rs.getString(4);
				if(!rs.wasNull())
					System.out.println(rs.getInt(1) + " " + rs.getFloat(2) + " " + rs.getString(3)+ " xxxxxxxx" + rs.getString(4).substring(rs.getString(4).length()-4) + " "+ rs.getInt(5) + " "+rs.getTimestamp(6) + " " + rs.getInt(7));
				else
					System.out.println(rs.getInt(1) + " " + rs.getFloat(2) + " " + rs.getString(3) + " " + rs.getInt(5) + " " + rs.getTimestamp(6) + " " + rs.getInt(7));
			}
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
	/**This method displays the itemized bill corresponding to a given customer and a given transaction id
 * 	@param cid The customer id
 * 	@param resid The reservation id
 	*/
	public void displayItemizedBill(int cid, int resid)
	{
		try
		{
			//SELECT THE BILL AND THE CORRESPONDING ORDER DETAILS GIVEN THE 
			//CUSTOMER AND THE RESERVATION ID
			PreparedStatement st = connection.prepareStatement("select billid, orderid, partid, serviceid, partname, order_count*rate from (select billid, orderid, partid, serviceid, partname, order_count, rate from orders o, order_particular op, particulars p where o.orderid = op.op_orderid and op.op_partid = p.partid and op.op_serviceid = p.serviceid) where billid in (select billid from bill where cid = ? and b_resid=?)");
			st.setInt(1, cid);
			st.setInt(2, resid);
			ResultSet rs = st.executeQuery();
			int billid = 0, orderid = 0;
			float nightlyrate = 0;
//			System.out.println("Occupancy by date range between "+ startdt+ " and " +enddt+" :\n");
			//PRINT THE ITEMIZED BILL
			System.out.println("Itemized Bill for customer " + cid + " for reservation " + resid);
			while(rs.next())
			{
				billid = rs.getInt(1);
				orderid = rs.getInt(2);
				System.out.println(rs.getInt(1)+ " " + rs.getInt(2)+ " "+rs.getInt(3)+ " " + rs.getInt(4)+ " " + rs.getString(5)+ " " + rs.getFloat(6));
			}

			//GET THE CHECK IN, CHECK OUT AND THE ROOM CATEGORY INFORMATION FROM RESERVES, ROOM AND ROOM CATEGORY TABLES
			st = connection.prepareStatement("select res.r_resid, res.r_hid, res.r_rnumber, to_char(res.r_checkindt,'YYYY-MM-DD HH24:MI:SS'), to_char(res.r_checkoutdt,'YYYY-MM-DD HH24:MI:SS'), rc.nightlyrate from reserves res, room r, room_category rc where res.r_hid = r.hid and res.r_rnumber = r.rnumber and r.category = rc.category and res.r_resid=?");
			st.setInt(1, resid);			
			rs = st.executeQuery();
			String start = "" , end = "";
			int hid = 0, rnumber = 0;
			boolean flag = false;
			while(rs.next())
			{
				start = rs.getString(4);
				end = rs.getString(5);
				if(rs.wasNull())
					flag = true;
				hid = rs.getInt(2);
				rnumber = rs.getInt(3);
				nightlyrate = rs.getFloat(6);
			}
		/*	while(rs.next())
			{
				start = rs.getString(4);
				end = rs.getString(5);
				long diff_in_millis = (end.getTime() - start.getTime());
				double diff_in_days = diff_in_millis * 60 * 60 * 24/1000;
				System.out.println(rs.getInt(1) + " " + rs.getInt(2) + " " + rs.getInt(3) + " " + start + " " + end + " " + rs.getFloat(6) + " " +diff_in_days);
			}*/

			//IF CHECKOUT DATE IS SPECIFIED THEN TAKE THE DIFFERENCE BETWEEN THE CHECK OUT AND CHECK IN DATES TO GET THE NUMBER OF DAYS
			if(!flag)
			{
				st = connection.prepareStatement("select to_date(?,'YYYY-MM-DD HH24:MI:SS') - to_date(?,'YYYY-MM-DD HH24:MI:SS') from dual"); 
				st.setString(1, end);
				st.setString(2, start);
			}
			//IF THE CHECKOUT DATE IS NULL, TAKE THE DIFFERENCE FROM THE CURRENT DATE
			else
			{
				st = connection.prepareStatement("select current_date - to_date(?,'YYYY-MM-DD HH24:MI:SS') from dual");
				st.setString(1, start);
			}
			ResultSet rs2 = st.executeQuery();
			double diff = 0;
			while(rs2.next())
			{
				diff = Math.floor(rs2.getFloat(1));
			}
//			System.out.println("diff : " + diff);
			
			//DISPLAY ROOM RENT DETAILS
			System.out.println(billid + " " + hid + " " + rnumber + " " + " Room rent " + diff + " " + nightlyrate + " " + diff*nightlyrate); 
			System.out.println("\n\n");
		}
		catch(SQLException e)
                {
                        System.out.println(e.getMessage());
                }

	}
}
