package configs;

import io.DatabaseTable;
import products.ProductType;

import java.util.HashMap;
import java.util.Map;

public final class DatabaseConfig {
    private static final Map<ProductType, String> sqlDeleteScriptPerProduct;
    private static final Map<DatabaseTable, String> keyNamePerDatabaseTable;

    static {
        sqlDeleteScriptPerProduct = new HashMap<>();
        keyNamePerDatabaseTable = new HashMap<>();

        DatabaseConfig.configurateSqlDeleteScriptPerProduct();
        DatabaseConfig.configurateKeyNamePerDatabaseTable();
    }

    private DatabaseConfig() {

    }

    private static void configurateKeyNamePerDatabaseTable() {
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.CUSTOMERS, "customer_id");
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.CURRENT_ACCOUNTS, "iban");
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.DEBIT_CARDS, "card_id");
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.DEPOSITS, "deposit_id");
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.LOANS, "loan_id");
        DatabaseConfig.keyNamePerDatabaseTable.put(DatabaseTable.CURRENT_ACCOUNT_TRANSACTIONS, "transaction_id");
    }

    private static void configurateSqlDeleteScriptPerProduct() {
        DatabaseConfig.sqlDeleteScriptPerProduct.put(ProductType.CURRENT_ACCOUNT, "delete from current_accounts where iban=?");
        DatabaseConfig.sqlDeleteScriptPerProduct.put(ProductType.DEBIT_CARD, "delete from debit_cards where card_id=?");
        DatabaseConfig.sqlDeleteScriptPerProduct.put(ProductType.DEPOSIT, "delete from deposits where deposit_id=?");
        DatabaseConfig.sqlDeleteScriptPerProduct.put(ProductType.LOAN, "delete from loans where loan_id=?");
    }

    public static Map<ProductType, String> getSqlDeleteScriptPerProduct() {
        return DatabaseConfig.sqlDeleteScriptPerProduct;
    }

    public static Map<DatabaseTable, String> getKeyNamePerDatabaseTable() {
        return DatabaseConfig.keyNamePerDatabaseTable;
    }
}
