#
# Usage (see tutorial below)
#
Bank Server Script:
	javac -classpath bcprov-jdk15on-147.jar *.java
	java -classpath bcprov-jdk15on-147.jar:. BankKeys
	java -classpath bcprov-jdk15on-147.jar:. MakeAccounts
	java -classpath bcprov-jdk15on-147.jar:. BankServer 

ATM Script (After Bank Server instructions above):
	java -classpath bcprov-jdk15on-147.jar:. ATMClient 1 localhost

Read Log File (After Bank Server instructions above):
	java -classpath bcprov-jdk15on-147.jar:. Log
#
#Tutorial
#
adwoa@toplofty:/mnt/castor/seas_home/a/adwoa/cis551-proj3/project3> javac -classpath bcprov-jdk15on-147.jar *.java
adwoa@toplofty:/mnt/castor/seas_home/a/adwoa/cis551-proj3/project3> java -classpath bcprov-jdk15on-147.jar:. BankKeys
adwoa@toplofty:/mnt/castor/seas_home/a/adwoa/cis551-proj3/project3> java -classpath bcprov-jdk15on-147.jar:. MakeAccounts
File: acct.db not found. Create it? (y/n): y
Creating new database
----- Creating Account -----
Account owner: alice
Please enter a pin number: 1234
Please enter ATM card filename: alice.card
Starting Balance: 1000.00
Creating account. Please wait...... Done
Create another account? (y/n) y
----- Creating Account -----
Account owner: bob
Please enter a pin number: 4321
Please enter ATM card filename: bob.card
Starting Balance: 100.00
Creating account. Please wait...... Done
Create another account? (y/n) n
dwoa@toplofty:/mnt/castor/seas_home/a/adwoa/cis551-proj3/project3> java -classpath bcprov-jdk15on-147.jar:. BankServer
--------------------------
  Bank Server is Running  
--------------------------
04-04-2012 06:07:21 ATM #1: Got first message from ATM #1
04-04-2012 06:07:21 ATM #1: Found the account
04-04-2012 06:07:21 ATM #1: Sending Challenge
04-04-2012 06:07:21 ATM #1: Waiting for response
04-04-2012 06:07:21 ATM #1: Response received
04-04-2012 06:07:21 ATM #1: Sending accept (with shared key)
04-04-2012 06:07:21 ATM #1: Authentication over
SignedMessage@1e79ed7f
04-04-2012 06:07:25 0 SUCCESS Deposited Amount was: 20.0
SignedMessage@6ba7508a
04-04-2012 06:07:29 0 SUCCESS Requested Balance was: 1020.0
SignedMessage@62c09554
04-04-2012 06:07:33 0 SUCCESS Withdrawal Amount was: 10.0
SignedMessage@3af42ad0
04-04-2012 06:07:38 0 SUCCESS User ended session.

dwoa@toplofty:/mnt/castor/seas_home/a/adwoa/cis551-proj3/project3> java -classpath bcprov-jdk15on-147.jar:. ATMClient 1 localhost
*****************************
			ATM #1

Please insert card: alice.card
Please enter your PIN: 
1234
Authentication init...
Waiting for the bank's response
Challenge received
Response sent
Waiting for the shared key
Received
Shared key obtained
Authentication over
Welcome alice
*****************************
(1) Deposit
(2) Withdraw
(3) Get Balance
(4) Quit

Please enter your selection: 1
Enter the deposit amount: 
20
Deposit was successful.
New Balance: 1020.0
*****************************
(1) Deposit
(2) Withdraw
(3) Get Balance
(4) Quit

Please enter your selection: 3
Balance: 1020.0
*****************************
(1) Deposit
(2) Withdraw
(3) Get Balance
(4) Quit

Please enter your selection: 2
Enter the withdrawal amount: 10
Withdrawal was successful.
New Balance: 1010.0 
*****************************                                                                                                                                   (1) Deposit
(2) Withdraw
(3) Get Balance
(4) Quit  

Please enter your selection: 4
Goodbye!
*****************************
*****************************                                                                                                                                              ATM #1   

Please insert card:    

 
#
# Group created classes
#
AuthenticationLogMessage.java - Class that contains information necessary to
	log the authentication process. All but the final authentication log 
	message contain the date it occurred, the source ATM, and the message that
	states the action that occurred. The final authentication message contains
	the above information, as well as the accountID and session key so that
	the contents of the signed transaction messages can be decrypted to reveal
	their contents.
	
AuthenticationMessage.java - Class that contains the information that is sent
    between the ATM and Bank Server during the authentication process.  This
    includes the Account ID for the first message, the challenge field for the
    second to fourth messages, and a field containing the session key for the final
    message.  Additional fields such as timestamps and nonces are contained in
    the parent class.
    
GeneralMessage.java - Parent class for the AuthenticationMessage and 
	TransactionMessage classes that contains fields for transmitting nonces
	and timestamps between the ATM and Bank Server during communication.
	
TransactionLogMessage - Class that contains information necessary to log the
    results of a transaction process.  This includes a timestamp, the source
    ATM, the account number, and whether the requested action was successful.
    
TransactionMessage - Class that contains the information necessary to make a
	transaction request and receive a response to that transaction request.
	For the request, an actionID and an amount is provided as well as 
	constants that indicate the selected transaction.  For the response, the
	TransactionMessage class contains whether or not the transaction was 
	successful as well as the balance for some transactions.  Additional fields
	such as timestamps and nonces are contained in the parent class.

cis551-project3.pdf - The report for the project.