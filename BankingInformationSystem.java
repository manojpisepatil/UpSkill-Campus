import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import java.util.Scanner;

public class BankingInformationSystem {
    private static final String TABLE_NAME = "BankAccounts";

    public static void main(String[] args) {
        
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        
        try {
            Table table = dynamoDB.getTable(TABLE_NAME);
            table.describe();
        } catch (ResourceNotFoundException e) {
            System.err.println("Table '" + TABLE_NAME + "' does not exist. Creating table...");
            createTable(client);
        }

        Scanner scanner = new Scanner(System.in);

        // Main loop for banking operations
        while (true) {
            System.out.println("Welcome to the Banking Information System");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    checkBalance(scanner, dynamoDB);
                    break;
                case 2:
                    deposit(scanner, dynamoDB);
                    break;
                case 3:
                    withdraw(scanner, dynamoDB);
                    break;
                case 4:
                    System.out.println("Thank you for using Banking Information System. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void createTable(AmazonDynamoDB client) {
        
        client.createTable("BankAccounts", 
            List.of(
                new AttributeDefinition("AccountNumber", ScalarAttributeType.S),
                new AttributeDefinition("Balance", ScalarAttributeType.N)),
            List.of(new KeySchemaElement("AccountNumber", KeyType.HASH)),
            new ProvisionedThroughput(10L, 10L));
        System.out.println("Table created successfully.");
    }

    private static void checkBalance(Scanner scanner, DynamoDB dynamoDB) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();

        Table table = dynamoDB.getTable(TABLE_NAME);
        GetItemSpec spec = new GetItemSpec().withPrimaryKey("AccountNumber", accountNumber);
        Item outcome = table.getItem(spec);

        if (outcome != null) {
            System.out.println("Balance for account " + accountNumber + " is: $" + outcome.getNumber("Balance"));
        } else {
            System.out.println("Account not found.");
        }
    }

    private static void deposit(Scanner scanner, DynamoDB dynamoDB) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        System.out.print("Enter deposit amount: ");
        double amount = scanner.nextDouble();

        Table table = dynamoDB.getTable(TABLE_NAME);
        table.updateItem(new UpdateItemSpec().withPrimaryKey("AccountNumber", accountNumber)
                .withUpdateExpression("SET Balance = Balance + :val")
                .withValueMap(new ValueMap().withNumber(":val", amount)));
        System.out.println("Deposit successful.");
    }

    private static void withdraw(Scanner scanner, DynamoDB dynamoDB) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.next();
        System.out.print("Enter withdrawal amount: ");
        double amount = scanner.nextDouble();

        Table table = dynamoDB.getTable(TABLE_NAME);
        table.updateItem(new UpdateItemSpec().withPrimaryKey("AccountNumber", accountNumber)
                .withUpdateExpression("SET Balance = Balance - :val")
                .withValueMap(new ValueMap().withNumber(":val", amount)));
        System.out.println("Withdrawal successful.");
    }
}
