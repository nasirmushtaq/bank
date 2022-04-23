# Sample Bank Application Demonstrating about transactionality, locking and how to transfer funds from one place to another

Steps To RUN This Application:
1. Setup Mysql Database in local 
Navigate to db.sql file in root directory to create tables required

run following commands
1. mvn clean install
2. Run Application on default port 8080

Below is the postman collection to try:
1. Create a bank account
2. Delete a bank account
3. Deposit Money
4. Withdraw Money
5. Transfer Money(Domestic)
6. Transfer Money(International currently support USD to INR and vice versa)
7. Get Paginated accounts


https://www.getpostman.com/collections/b548bee82bb9c2b1a508
