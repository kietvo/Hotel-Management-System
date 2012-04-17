import java.sql.*;
/**
 * Class to handle the services,orders and its particulars
 */
public class ServiceMaintainer
{
	Connection connection;
	/**
 * 	ServiceMaintainer constructor specifying the connection object
 * 	@param connection The JDBC connection object
 * 	*/
	public ServiceMaintainer(Connection connection)
	{
		this.connection = connection;
	}
	/*
 * 	orderdt = current_timestamp
 * 	order_id has to be inserted using a sequence

 * 	*/
 
 /**
 * 	This method inserts a new service into the orders table.
 * 	@param hid the hotel id
 * 	@param rnumber the room number
 * 	@param billid the bill id
 * 	@param service_id the service id
 * 	@param part_id the particular id
 * 	@param ordercount the no. of units ordered
 * 	*/
	public void insertService(int hid, int rnumber, int billid, int service_id, int part_id, int order_count)
	{
		try
		{
			connection.setAutoCommit(false);
			int nextid = 0;
			Timestamp ts = null;
			Statement query = connection.createStatement();
			
			//GET THE NEXT ORDER ID 
			ResultSet rs = query.executeQuery("select orderid_seq.nextval from dual");
			while(rs.next())
			{
				nextid = rs.getInt(1);
			}

			//GET CURRENT TIMESTAMP FROM DUAL
			ResultSet rs2 = query.executeQuery("select current_timestamp from dual");
			while(rs2.next())
			{
				ts = rs2.getTimestamp(1);
			} 
		
			//INSERT A NEW ENTRY INTO ORDERS
			PreparedStatement st = connection.prepareStatement("insert into orders(orderid, orderdt, hid, rnumber, billid) values(?, ?, ?, ?, ?)");
			st.setInt(1, nextid);
			st.setTimestamp(2, ts);
			st.setInt(3, hid);
			st.setInt(4, rnumber);
			st.setInt(5, billid);
			st.executeUpdate();
	
			//INSERT A NEW ENTRY INTO ORDER PARTICULAR 
			st = connection.prepareStatement("insert into order_particular(op_orderid, op_orderdt, op_serviceid, op_partid, order_count) values (?, ?, ?, ?, ?)"); 
			st.setInt(1, nextid);
			st.setTimestamp(2, ts);
			st.setInt(3, service_id);
			st.setInt(4, part_id);
			st.setInt(5,order_count);
			st.executeUpdate();
			
			connection.commit();
			connection.setAutoCommit(true);
			//PRINT THE INFORMATION ABOUT THE INSERTION
			System.out.println("Order " + nextid +" for " + order_count + " unit(s) of service " + service_id + " and particular " + part_id + " processed successfully at " + ts);
//			System.out.println("Room "+ rnumber + " at Hotel " + hid + " assigned to customer " + cid + " between " + checkindt + " and " + checkoutdt+ ". Booking was done " + method);
		}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
		}
	}
}
