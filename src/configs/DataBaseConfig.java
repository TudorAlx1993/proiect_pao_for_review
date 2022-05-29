package configs;

import products.ProductType;

import java.util.HashMap;
import java.util.Map;

public final class DataBaseConfig {
    private static final Map<ProductType, String> sqlDeleteScriptsPerProduct;

    static {
        sqlDeleteScriptsPerProduct = new HashMap<>();

        DataBaseConfig.configurateSqlDeleteScriptsPerProduct();
    }

    private DataBaseConfig() {

    }

    private static void configurateSqlDeleteScriptsPerProduct() {
        DataBaseConfig.sqlDeleteScriptsPerProduct.put(ProductType.CURRENT_ACCOUNT, "delete from current_accounts where iban=?");
        DataBaseConfig.sqlDeleteScriptsPerProduct.put(ProductType.DEBIT_CARD, "delete from debit_cards where card_id=?");
        DataBaseConfig.sqlDeleteScriptsPerProduct.put(ProductType.DEPOSIT, "delete from deposits where deposit_id=?");
        DataBaseConfig.sqlDeleteScriptsPerProduct.put(ProductType.LOAN, "delete from loans where loan_id=?");
    }

    public static Map<ProductType, String> getSqlDeleteScriptsPerProduct() {
        return DataBaseConfig.sqlDeleteScriptsPerProduct;
    }
}
