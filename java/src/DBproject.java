/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql){
		/*
			CREATE TABLE Doctor
			(
				doctor_ID INTEGER NOT NULL,
				name VARCHAR(128),
				specialty VARCHAR(24),
				did INTEGER NOT NULL,
				PRIMARY KEY (doctor_ID),
				FOREIGN KEY (did) REFERENCES Department(dept_ID)
			);
		*/

		int docID;
		String name;
		String specialty;
		int deptID;

		String query;	

		//gets values for each of these variables
		//retrieves docID
		while(true)
		{
			System.out.print("Input Doctor ID: ");
			try
			{
				//parseInt retrieves the data type of the input
				docID = Integer.parseInt(in.readLine());
				break;
			}
			//handles errors
			catch (Exception e)
			{
				System.out.println("Invalid input! Error Message: " + e.getMessage());
				continue;
			}
		}

		//retrieves name
		while(true)
		{
			System.out.print("Input Doctor Name: ");
			try
			{
				name = in.readLine();
				if(name.length() <= 0)
				{
					throw new RuntimeException("Invalid input! The Doctor's name cannot be empty.");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid input! Error Message: " + e.getMessage());
				continue;
			}
		}

		//retrives specialty
		while(true)
		{
			System.out.print("Please input Doctor's Specialty: ")
			try
			{
				specialty = in.readLine();
				if(specialty.length() <= 0)
				{
					throw new RuntimeException("Invalid input! The Doctor's specialty cannot be empty.");
				}
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid input! Error Message: " + e.getMessage());
				continue;
			}
		}

		//retrieves deptID
		while(true)
		{
			System.out.print("Please input Department ID: ")
			try
			{
				deptID = Integer.parseInt(readLine());
				break;
			}
			catch (Exception e)
			{
				System.out.println("Invalid input! Error Message: " + e.getMessage());
				continue;
			}
		}

		//now to combine all the queries and add it to the database
		try
		{
			query = "INSERT INTO Doctor_works_dept (doctor_ID, name, specialty, did) VALUES (" + docID " , '" + name + "', '" + specialty + "', " + deptID + " );";
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println("Query attempt failed. Error message: " + e.getMessage());
		}

	}

	public static void AddPatient(DBproject esql) {//2
		/*
		CREATE TABLE Patient
		(
			patient_ID INTEGER NOT NULL,
			name VARCHAR(128) NOT NULL,	
			gtype _GENDER NOT NULL,
			age INTEGER NOT NULL,
			address VARCHAR(256),
			number_of_appts INTEGER,
			PRIMARY KEY (patient_ID)
		);
		*/

		int pID;
		String name;
		String gender;
		int numApt;
		int age;
		String address;

		String query;

		//retrieves patient id
		while(true)
		{
			System.out.print("Please enter Patient ID: ");
			try
			{
				pID = Integer.parseInt(in.readLine());
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves name
		while(true)
		{
			System.out.print("Please enter Patient name: ");
			try
			{
				name = in.readLine();
				if(name.length() <= 0)
				{
					throw new RuntimeException("Invalid input! The Patient's name cannot be empty.");
				}
			}
			catch (Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves patient's gender NOTE: PREDETERMINED TABLES ONLY ALLOW M OR F, THIS IS NOT BY INDIVIDUAL CHOICE
		while(true)
		{
			System.out.print("Please enter Patient's gender(Use 'M' or 'F': ");
			try
			{
				gender  = in.readLine();
				if(gender != "M" || gender != "F")
				{
					throw new RuntimeException("Invalid input! The only valid input for a Patient's gender is 'M' or 'F'.");
				}
			}
			catch (Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves the age of the patient
		while(true)
		{
			System.out.print("Please enter the Patient's age: ");
			try
			{
				age = Integer.parseInt(in.readLine());
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves the patient's address
		while(true)
		{
			System.out.print("Please enter the Patient's address: ");
			try
			{
				address = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves number of appointments
		while(true)
		{
			System.out.print("Please enter the number of appointments the Patient has: ");
			try
			{
				numApt = Integer.parseInt(in.readLine());
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//combine queries and add to database
		try
		{
			query = "INSERT INTO Patient(patient_ID, name, gtype, age, address, number_of_appts) VALUES (" + pID + ", '" + name + "', '" + gender + "', " + age + ", '" + address + "', " + numApt + ");");
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println("Query attempt failed. Error message: " + e.getMessage());
		}
	}

	public static void AddAppointment(DBproject esql) {//3
		/*
		CREATE TABLE Appointment
		(	
			appnt_ID INTEGER NOT NULL,	
			adate DATE NOT NULL,
			time_slot VARCHAR(11),
			status _STATUS,
			PRIMARY KEY (appnt_ID)
		);
		);
		*/
		int apptID;
		String dateM;
		String dateD;
		String dateY;
		String timeSlotStart;
		String timeSlotEnd;
		String status;

		String query;

		//retrieves appointment id
		while(true)
		{
			System.out.print("Please enter the Appointment ID: ");
			try
			{
				apptID = Integer.parseInt(in.readLine());
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves appointment day(month)
		while(true)
		{
			System.out.print("Please enter the month of the Appointment: ");
			try
			{
				dateM = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves appointment day(day)
		while(true)
		{
			System.out.print("Please enter the day of the Appointment: ");
			try
			{
				dateD = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves appointment day(year)
		while(true)
		{
			System.out.print("Please enter the month of the Appointment: ");
			try
			{
				dateM = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves appointment start time slot, i.e. 13:00 is 1:00pm
		while(true)
		{
			System.out.print("Please enter the Appointment Start time slot(Using hours:minutes. i.e. 2:00 or 17:00): ");
			try
			{
				timeSlotStart = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves appointment end time slot, i.e. 13:00 is 1:00pm
		while(true)
		{
			System.out.print("Please enter the Appointment End time slot(Using hours:minutes. i.e. 2:00 or 17:00): ");
			try
			{
				timeSlotEnd = in.readLine();
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//retrieves status of appointment (PA=past, AC=active, AV=available, WL=waitlisted)
		while(true)
		{
			System.out.print("Please enter the status of the Appointment: ");
			try
			{
				status = in.readLine();
				if(status != 'PA' || status != 'AC' || status != 'AV' || status != 'WL')
				{
					throw new RuntimeException("Invalid input! The only valid input for an Appointment Status is 'PA' or 'AC' or 'AV' or 'WL'.");
				}
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! Error message: " + e.getMessage());
				continue;
			}
		}

		//combine queries and add to database
		try
		{
			//VALUES (apptID, 'dateM/dateD/dateY', 'timeSlotStart-timeSlotEnd', 'status')
			query = "INSERT INTO Appointment(appnt_ID, adate, time_slot, status) VALUES (" + apptID + ", '" + dateM + "/" +dateD + "/" + dateY "', '" + timeSlotStart + "-" + timeSlotEnd + "', '" + status + "');");
			esql.executeUpdate(query);
		}
		catch (Exception e)
		{
			System.err.println("Query attempt failed. Error message: " + e.getMessage());
		}
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor

		// AC=active, AV=available
		// SELECT A.appnt_ID 
		// FROM Appointment A, Doctor D, has_appointment H
		// WHERE H.doctor_id = D.doctor_ID AND H.appt_id = A.appnt_ID AND (A.status = 'AV' OR A.status = 'AC') AND A.adate => startDate AND A.adate <= endDate
		// Note: D.doctor_ID, startDate, endDate is user inputted

		int docID;
		String startDate;
		String endDate;
		String query;

		//docID
		while(true)
		{
			System.out.print("Please enter Doctor ID: ");
			try
			{
				docID = Integer.parseInt(in.readLine());
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//startDate
		while(true)
		{
			System.out.print("Please enter a start date for your search(Use MM/DD/YY): ");
			try 
			{
				startDate = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//endDate
		while(true)
		{
			System.out.print("Please enter an end date for your search(Use MM/DD/YY): ");
			try 
			{
				endDate = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//combine queries
		try
		{
			query = "SELECT A.appnt_ID from Appointment A, Doctor D has_appointment H where H.doctor_id = " + docID + "AND H.appt_id = A.appnt_ID AND (A.status = 'AV' or A.status = 'AC') AND A.adate => " + startDate + " AND A.adate <= " + endDate;
			esql.executeUpdate(query);
		}
		catch (Exception e) 
		{
				System.err.println("Query failed! " + e.getMessage());
		}
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department

		// SELECT A.appnt_ID
		// FROM Appointment A, Department D, request_maintenance M, has_appointment H 
		// WHERE M.dept_name = dName AND M.did = H.doctor_id AND H.appt_id = A.appnt_ID AND A.status = 'AV' AND A.adate = date
		// dName and date is user inputted

		String dName;
		String date;
		String query;

		//dName
		while(true)
		{
			System.out.print("Please enter the Department name: ");
			try
			{
				dName = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//date
		while(true)
		{
			System.out.print("Please enter a specified date(Use MM/DD/YY): ");
			try
			{
				date = in.readLine();
				break;
			}
			catch(Exception e)
			{
				System.out.println("Invalid input! " + e.getMessage());
				continue;
			}
		}

		//query
		try
		{
			query = "SELECT A.appnt_ID FROM Appointment A, Department D, request_maintenance M, has_appointment H WHERE M.dept_name = \'" + dName + "\' AND M.did = H.doctor_id AND H.appt_id = A.appnt_ID AND A.status = 'AV' AND A.adate = \'" + date + "\'";
			esql.executeUpdate(query);
		}
		catch (Exception e) 
		{
				System.err.println("Query failed! " + e.getMessage());
		}

	}


	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order

		// SELECT D.doctor_ID, D.name, D.specialty, A.status, count(A.status) AS C
		// FROM Doctor D, Appointment A, has_appointment H
		// WHERE H.doctor_id = D.doctor_ID AND A.appnt_ID = H.appt_id
		// GROUP BY D.doctor_ID, D.name, D.specialty, A.status
		// ORDER BY C Desc

		String query;

		try
		{
			query = "SELECT D.doctor_ID, D.name, D.specialty, A.status, count(A.status) AS C "
				+ "FROM Doctor D, Appointment A, has_appointment H "
				+ "WHERE H.doctor_id = D.doctor_ID AND A.appnt_ID = H.appt_id "
				+ "GROUP BY D.doctor_ID, D.name, D.specialty, A.status "
				+ "ORDER BY C Desc;";
		}
		catch(Exception e)
		{
			System.err.println("Query failed! " + e.getMessage());
		}
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.

		// SELECT D.doctor_ID, D.name, D.specialty, count(S.pid) AS C
		// FROM Doctor D, Searches S, has_appointment H, Appointment A
		// WHERE H.doctor_id = D.doctor_ID AND A.status = status AND A.appnt_ID = S.aid AND H.appt_id = S.aid
		// GROUP BY D.doctor_ID, D.name, D.specialty

		// Note: in table searches: hid = hospital id, pid = patient id, aid = appointment id 
		// Also note status is retrieved from the user input 

		try
		{
			System.out.print("Please enter the appointment status: ");
			String status = in.readLine();

			String query = "SELECT D.doctor_ID, D.name, D.specialty, count(S.pid) AS C "
							+ "FROM Doctor D, Searches S, has_appointment H, Appointment A "
							+ "WHERE H.doctor_id = D.doctor_ID AND A.status = " + status + " AND A.appnt_ID = S.aid AND H.appt_id = S.aid "
							+ "GROUP BY D.doctor_ID, D.name, D.specialty;";
			esql.executeUpdate(query);
		}
		catch(Exception e)
		{
			System.err.println("Query failed! " + e.getMessage());
		}
	}
}
