package customers;

import address.Address;
import configs.Codes;
import configs.CustomerConfig;
import exceptions.InvalidIdentificationCodeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Company extends Customer {
    private String companyName;
    // a comapny can't change its cui
    // cui = codul unic de inregistrare
    private final String cui;
    private final LocalDate establishmentDate;

    public Company(String companyName,
                   String cui,
                   LocalDate establishmentDate,
                   String password,
                   String phoneNumber,
                   String emailAddress,
                   Address address) {
        super(password, phoneNumber, emailAddress, address);

        try {
            this.checkCui(cui);
        } catch (InvalidIdentificationCodeException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.companyName = companyName;
        this.cui = cui;
        this.establishmentDate = establishmentDate;
    }

    private void checkCui(String cui) throws InvalidIdentificationCodeException {
        if (!cui.matches(CustomerConfig.getCuiPattern()))
            throw new InvalidIdentificationCodeException();
    }

    @Override
    public String getCustomerName() {
        return this.companyName;
    }

    @Override
    public String getCustomerUniqueID() {
        return this.cui;
    }

    @Override
    public LocalDate getBirthDay() {
        return this.establishmentDate;
    }

    @Override
    public String getUniqueID() {
        return this.cui;
    }

    @Override
    public String toString() {
        return "Company information:\n" +
                "\t* CUI: " + this.cui + "\n" +
                "\t* name: " + this.companyName + "\n" +
                "\t* establishment date: " + this.getBirthDay().toString() + "\n" +
                super.toString() +
                this.getAddress().toString() + "\n";
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.cui,
                this.companyName,
                this.establishmentDate);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Company))
            return false;

        Company company = (Company) object;
        if (!this.cui.equals(company.cui))
            return false;

        return true;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.COMPANY;
    }

    @Override
    public List<String> getCustomerDataForCsvWriting() {
        List<String> lineContent = new ArrayList<>();

        lineContent.add(CustomerType.COMPANY.toString());
        lineContent.add(this.getCustomerUniqueID());
        lineContent.add(this.companyName);
        lineContent.add(this.getBirthDay().toString());
        lineContent.addAll(super.getCustomerDataForCsvWriting());

        return lineContent;
    }
}
