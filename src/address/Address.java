package address;

import java.util.Objects;
import java.util.Scanner;

public class Address {
    private String country;
    private String city;
    private String zipCode;
    private int streetNumber;
    private String streetName;
    private String additionalInfo;

    public Address(String country,
                   String city,
                   String zipCode,
                   String streetName,
                   int streetNumber,
                   String additionalInfo) {
        this.country = country;
        this.city = city;
        this.zipCode = zipCode;
        this.streetNumber = streetNumber;
        this.streetName = streetName;
        this.additionalInfo = additionalInfo;
    }

    public Address(String country,
                   String city,
                   String zipCode,
                   String streetName,
                   int streetNumber) {
        this(country, city, zipCode, streetName, streetNumber, "");
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "\t* country: " + this.country + "\n" +
                "\t* city: " + this.city + "\n" +
                "\t* zip code: " + this.zipCode + "\n" +
                "\t* street name: " + this.streetName + "\n" +
                "\t* street number: " + this.streetNumber + "\n" +
                "\t* additional information about address: " + (this.additionalInfo.equals("") ? "NA" : this.additionalInfo) + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.country, this.city, this.zipCode, this.streetName, this.streetNumber, this.additionalInfo);
    }

    public static Address readAddressFromKeyboard() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the country:\n");
        final String country = scanner.nextLine();
        System.out.print("Enter the city: ");
        final String city = scanner.nextLine();
        System.out.print("Enter the zip code: ");
        final String zipCode = scanner.nextLine();
        System.out.print("Enter the street name: ");
        final String streetName = scanner.nextLine();
        System.out.print("Enter the street number: ");
        final int streetNumber = scanner.nextInt();
        System.out.print("Enter additional info or NA: ");
        final String additionalInfo = scanner.nextLine();

        return new Address(country, city, zipCode, streetName, streetNumber, additionalInfo);
    }
}
