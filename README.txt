#
# Usage
#
Bank Server Script:
	javac *.java
	java BankServer -classpath bcprov-jdk15on-147.jar

ATM Script:
	javac *.java
	java ATMClient -classpath bcprov-jdk15on-147.jar

Read Log File:
	javac *.java
	java Log -classpath bcprov-jdk15on-147.jar

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
    second to messages, and finally a field for the session key for the final
    message.  Additional fields such as timestamps and nonces are contained in
    the parent class.
    
GeneralMessage.java - Parent class for the AuthenticationMessage and 
	TransactionMessage classes that contains fields for transmitting nonces
	and timestamps between the ATM and Bank Server during communication.
	
TransactionLogMessage - Class that contains information necessary to log the
    results of a transaction process.  This includes a timestamp, the source
    ATM, the account number, and whether the requested action was successful.
    
TransactionMessage - Class that contains the information necessary to make a
	transaction request and receive the response to that transaction request.
	For the request, an actionID and an amount is provided as well as 
	constants that indicate the selected transaction.  For the response, the
	TransactionMessage class contains whether or not the transaction was 
	successful as well as the balance for some transactions.  Additional fields
	such as timestamps and nonces are contained in the parent class.