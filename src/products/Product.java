package products;

import currency.Currency;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Product {
    private final Currency currency;
    private final LocalDate openDate;

    protected Product(Currency currency, LocalDate openDate) {
        this.currency = currency;
        this.openDate = openDate;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public LocalDate getOpenDate() {
        return this.openDate;
    }

    protected int generateRandomNumber(int maxNumber) {
        // use uniformely distributed random numbers between 0 (inclusive) and maxNumber (exclusive)
        Random randomNumberGenerator = new Random();
        int intRandomNumber = randomNumberGenerator.nextInt(maxNumber);

        return intRandomNumber;
    }

    public abstract String getProductUniqueId();

    protected String generateUniqueID(int StringLength, int maxDigit) {
        StringBuilder tempString = new StringBuilder();

        for (int i = 0; i < StringLength; ++i)
            tempString.append(this.generateRandomNumber(maxDigit));

        return tempString.toString();
    }

    @Override
    public String toString() {
        return "\t* currency: " + this.currency.toString() + "\n" +
                "\t* open date: " + this.openDate.toString() + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.currency.hashCode(), this.openDate.hashCode());
    }

    public abstract ProductType getProductType();

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Product))
            return false;

        Product product = (Product) object;
        return this.currency.equals(product.currency) && this.openDate.equals(product.openDate);
    }

    public List<String> getDataForCsvWriting(String customerID) {
        // pun si customerID ca sa pot sa asociez produsul cu clientul corespunzator
        return Arrays.asList(this.currency.getCurrencyCode(),
                this.openDate.toString(),
                customerID);
    }

    public List<String> getHeaderForCsvFile() {
        return Stream.of("currency_code",
                        "opening_date",
                        "customer_id")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }
}
