package BankingApplication;

//BankApplication.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

public class BankingApp {
	private static HashMap<String,BankAccount> accounts=new HashMap<>();
	private static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) {
		while(true) {
			System.out.println("\n--- Banking Application ---");
			System.out.println("1. Create Account");
			System.out.println("2. View Account Details");
			System.out.println("3. Update Account Information");
			System.out.println("4. Deposit");
			System.out.println("5. Withdraw");
			System.out.println("6. Transfer");
			System.out.println("7. View Transaction History");
			System.out.println("8. Generate Reports");
			System.out.println("9.Exit");
			System.out.print("Choose an option: ");
			int choice =scanner.nextInt();
			scanner.nextLine();
			
			switch(choice) {
			case 1:
				createAccount();
				break;
			case 2:
				viewAccountDetails();
				break;
			case 3:
				updateAccountInfo();
				break;
			case 4:
				deposit();
				break;
			case 5:
				withdraw();
				break;
			case 6:
				transfer();
				break;
			case 7:
				viewTransactionHistory();
				break;
			case 8:
				generateReports();
				break;
			case 9:
				System.out.println("Thank you for using the banking application!");
				return;
			default:
				System.out.println("Invalid choice. Please try again.");
			}
		}
	}
	
	//Create new Account
	//Create a new account 
	private static void createAccount() {
		String accountNumber;
		while(true) {
			System.out.print("Enter Account Number (8-16 digits):" );
			accountNumber =scanner.nextLine().trim();
			
			//check if account already exist 
			if(accounts.containsKey(accountNumber)) {
				System.out.println("Error: Account with this number already exists.");
				continue;
			}
			
			//Validate account number format 
			if(!isValidAccountNumber(accountNumber)) {
				System.out.println("Error: Account number must be between 8 and 16 digits.");
			}else {
				break; //valid account number
			}
		}
		
		String accountHolderName;
		while(true) {
			System.out.print("Enter Account Holder Name: ");
			accountHolderName=scanner.nextLine().trim();
			if(accountHolderName.isEmpty()) {
				System.out.println("Error: Account holder name cannot be empty.");
			}
			else {
				break;
			}
		}
		
		String accountType;
		while(true) {
			System.out.print("Enter Account Type (S for Savings / C for Current): ");
			String accountTypeInput=scanner.nextLine().trim().toLowerCase();
			
			if(accountTypeInput.equals("s")) {
				accountType="Savings";
				break;
			}
			else if(accountTypeInput.equals("c")) {
				accountType="Current";
				break;
				}
			else {
				System.out.println("Error: Invalid input. Please enter 'S' for Savings or 'C' for Current.");
			}
		}
		
		String address;
		while(true) {
			System.out.print("Enter Address: ");
			address=scanner.nextLine().trim();
			if(address.isEmpty()) {
				System.out.println("Error: Address cannot be empty.");
			}
			else {
				break;
			}
		}
		
		String contactNumber;
		while(true) {
			System.out.print("Enter Contact Number (10 digits):");
			contactNumber=scanner.nextLine().trim();
			
			if(contactNumber.isEmpty()) {
				System.out.println("Error: Contact number cannot be empty.");
			}
			else if(!isValidContactNumber(contactNumber)) {
				System.out.println("Error: Invalid contact number. It must be a 10-digit number.");
			}
			else {
				break;
			}
		}
		
		try {
			BankAccount newAccount = new BankAccount(accountNumber, accountHolderName,accountType,address,
					contactNumber);
			
			//Add to in-memory accounts map
			accounts.put(accountNumber,newAccount);
			
			//Save to database
			try(Connection conn = DatabaseConnection.getConnection()){
				String sql = "INSERT INTO accounts(accountNumber,accountHolderName,accountType,balance,address,"
						+ "contactNumber) VALUES (?,?,?,?,?,?)";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, newAccount.getAccountNumber());
				stmt.setString(2, newAccount.getAccountHolderName());
				stmt.setString(3, newAccount.getAccountType());
				stmt.setDouble(4, newAccount.getBalance());
				stmt.setString(5, newAccount.getAddress());
				stmt.setString(6, newAccount.getContactNumber());
				
				int rowsInserted =stmt.executeUpdate();
				if(rowsInserted > 0) {
					System.out.println("Account created successfully and saved to database!");
				}
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
		catch(IllegalArgumentException e) {
			System.out.println(e.getMessage());
		}
	}
	
	//validation methods 
	private static boolean isValidAccountNumber(String accountNumber) {
		return accountNumber.matches("\\d{8,16}"); //account number must be 8 to 16 digits 
	}
	
	private static boolean isValidContactNumber(String contactNumber) {
		return contactNumber.matches("\\d{10}"); //contact number must be exactly 10 digits
	}
	
	//View account details
	private static void viewAccountDetails() {
		System.out.print("Enter Account Number: ");
		String accountNumber=scanner.nextLine();
		BankAccount account=accounts.get(accountNumber);
		
		if(account!=null) {
			account.displayAccountInfo();
		}
		else {
			System.out.println("Account not found.");
		}
	}
	
	//Update account information 
	//Update account information 
	private static void updateAccountInfo() {
		System.out.print("Enter Account Number: ");
		String accountNumber=scanner.nextLine();
		BankAccount account =accounts.get(accountNumber);
		
		if(account!=null) {
			System.out.print("Enter new Address: ");
			String address=scanner.nextLine();
			System.out.print("Enter new contact number:");
			String contactNumber=scanner.nextLine();
			
			//update in=memory account details
			account.setAddress(address);
			account.setContactNumber(contactNumber);
			
			//Update in database
			try(Connection conn = DatabaseConnection.getConnection()){
				String sql="UPDATE accounts SET address = ?,contactNumber = ? WHERE accountNumber =?";
				PreparedStatement stmt = conn.prepareStatement(sql);
				stmt.setString(1, address);
				stmt.setString(2, contactNumber);
				stmt.setString(3, accountNumber);
				
				int rowsUpdated=stmt.executeUpdate();
				if(rowsUpdated>0) {
					System.out.println("Account information updated successfully in database.");
				}
				else {
					System.out.println("Failed to update account information in database.");
				}
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Account not found.");
		}
	}
	
	//deposit money
	private static void deposit() {
		System.out.print("Enter Account Number: ");
		String accountNumber=scanner.nextLine();
		BankAccount account =accounts.get(accountNumber);
		
		if(account!=null) {
			System.out.print("Enter amount to deposit: ");
			double amount = scanner.nextDouble();
			account.deposit(amount);
			
			try(Connection conn = DatabaseConnection.getConnection()){
				String sql="UPDATE accounts SET balance = ? WHERE accountNumber = ?";
				PreparedStatement stmt=conn.prepareStatement(sql);
				stmt.setDouble(1, account.getBalance());
				stmt.setString(2, account.getAccountNumber());
				stmt.executeUpdate();
				
				//insert transaction record 
				sql="INSERT INTO transactions (accountNumber, accountType, amount) VALUES (?,'Deposit',?)";
				stmt=conn.prepareStatement(sql);
				stmt.setString(1, accountNumber);
				stmt.setDouble(2, amount);
				stmt.executeUpdate();
				
				System.out.println("Deposit successful and recorded in database.");
				System.out.println("Current Balance: "+ account.getBalance()); //display current balance
			}
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Account not found.");
		}
	}
	
	//withdraw money 
	private static void withdraw() {
		System.out.print("Enter Account Number: ");
		String accountNumber = scanner.nextLine();
		BankAccount account=accounts.get(accountNumber);
		
		if(account!=null) {
			System.out.print("Enter amount to withdraw: ");
			double amount = scanner.nextDouble();
			
			//check if withdrawal is successful in th BankAccount class
			if(amount > 0 && amount<=account.getBalance()) {
				account.withdraw(amount);
				
				try(Connection conn = DatabaseConnection.getConnection()){
					//update the balance in the accounts table 
					String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
					PreparedStatement stmt=conn.prepareStatement(sql);
					stmt.setDouble(1, account.getBalance());
					stmt.setString(2, account.getAccountNumber());
					stmt.executeUpdate();
					
					//insert a transaction record for withdrawal 
					sql="INSERT INTO transactions(accountNumber,accountType,amount) VALUES (?,'Withdrawal',?)";
					stmt=conn.prepareStatement(sql);
					stmt.setString(1, accountNumber);
					stmt.setDouble(2, amount);
					stmt.executeUpdate();
					
					System.out.println("Withdarawal successful and recorded in database.");
					System.out.println("Current Balance: "+account.getBalance() ); //display current balance
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Insufficient balance or invalid withdrawal amount.");
			}
		}
		else {
			System.out.println("Account not found.");
		}
	}
	
	
	//transfer money 
	private static void transfer() {
		System.out.print("Enter your Account Number: ");
		String fromaccount_number = scanner.nextLine();
		System.out.print("Enter the recipient Account Number: ");
		String toaccount_number = scanner.nextLine();
		System.out.print("Enter the amount to be transferred: ");
		double amount = scanner.nextDouble();
		
		BankAccount fromAccount = accounts.get(fromaccount_number);
		BankAccount toAccount = accounts.get(toaccount_number);
		
		if(fromAccount != null && toAccount != null) {
			if(amount > 0 && amount <= fromAccount.getBalance()) {
				fromAccount.withdraw(amount);
				toAccount.deposit(amount);
				
				try(Connection con = DatabaseConnection.getConnection()){
					con.setAutoCommit(false);
					
					String sql = "UPDATE accounts SET balance = ? WHERE accountNumber = ?";
					PreparedStatement stmt = con.prepareStatement(sql);
					stmt.setDouble(1, fromAccount.getBalance());
					stmt.setString(2, fromAccount.getAccountNumber());
					stmt.executeUpdate();
					
					stmt.setDouble(1, toAccount.getBalance());
					stmt.setString(2, toAccount.getAccountNumber());
					stmt.executeUpdate();
					
					sql = "INSERT INTO transactions(accountNumber, accountType, amount, description) VALUES (?, " + " 'Transfer Out', ?, ?)";
					stmt = con.prepareStatement(sql);
					stmt.setString(1, fromaccount_number);
					stmt.setDouble(2, amount);
					stmt.setString(3, "Transferred to " + toaccount_number);
					stmt.executeUpdate();
					
					sql = "INSERT INTO transactions(accountNumber, accountType, amount, description) VALUES (?, 'Transfer In', ?, ?)";
					stmt = con.prepareStatement(sql);
					stmt.setString(1, toaccount_number);
					stmt.setDouble(2, amount);
					stmt.setString(3, "Recieved from " + fromaccount_number);
					stmt.executeUpdate();
					
					con.commit();
					System.out.println("Amount transferred successfully and recorded in the datbase");
				}
				catch(SQLException e) {
					e.printStackTrace();
				}
			}
			else {
				System.out.println("Transfer failed. Insufficient balance or invalid amount");
			}
		}
		else {
			System.out.println("One or both the accounts can not be found");
		}
	}
	
	//view transaction history 
	private static void viewTransactionHistory() {
		System.out.print("Enter Account Number: ");
		String accountNumber =scanner.nextLine();
		BankAccount account = accounts.get(accountNumber);
		
		if(account!=null)
		{
			account.displayTransactionHistory();
		}
		else{
			System.out.println("Account not found.");
		}
	}
	
	//generate reports 
	//BankApplication.java
	
	//inside the generateReports() method 
	private static void generateReports() {
		System.out.println("\n--- Report Generation ---");
		System.out.println("1. Customer Details");
		System.out.println("2. Transaction History");
		System.out.println("3. Total Balance");
		System.out.println("4. Number of Accounts by Type");
		System.out.println("Choose an option: ");
		int choice =scanner.nextInt();
		scanner.nextLine();
		
		switch(choice) {
		case 1:
			generateCustomerDetailsReport();
			break;
		case 2:
			System.out.print("Enter Account Number: ");
			String accountNumber = scanner.nextLine();
			BankAccount account=accounts.get(accountNumber);
			if(account!=null) {
				account.displayTransactionHistory();
			}
			else {
				System.out.println("Account not found.");
			}
			break;
		case 3:
			generateTotalBalanceReport();
			break;
		case 4:
			generateAccountTypeReport();
			break;
			default:
				System.out.println("Invalid choice. Please try again.");
		}
	}
	
	//Report 1
	private static void generateCustomerDetailsReport() {
		System.out.println("\nCustomer Details Report:");
		for(BankAccount account : accounts.values()) {
			account.displayAccountInfo();
			System.out.println("-----------------------------");
		}
	}
	
	//report 2
	private static void generateTotalBalanceReport() {
		double totalBalance=accounts.values().stream().mapToDouble(BankAccount::getBalance).sum();
		System.out.println("Total balance across all accounts: "+totalBalance);
	}
	
	//report 3
	private static void generateAccountTypeReport() {
		long savingsCount = accounts.values().stream().filter(acc -> acc.getAccountType().equalsIgnoreCase("Savings")).count();
		long currentCount = accounts.values().stream().filter(acc -> acc.getAccountType().equalsIgnoreCase("Current")).count();
		
		System.out.println("Number of Savings Account: "+ savingsCount);
		System.out.println("Number of Current Accounts: "+currentCount);
		}
	
	
	

}