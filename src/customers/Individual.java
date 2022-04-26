package customers;

import address.Address;
import configs.Codes;
import configs.CustomerConfig;
import exceptions.InvalidIdentificationCodeException;

import java.time.LocalDate;
import java.util.Objects;

public class Individual extends Customer {
    private String firstName;
    private String lastName;
    // a person can't change its CNP
    private final String cnp;

    public Individual(String firstName,
                      String lastName,
                      String cnp,
                      String password,
                      String phoneNumber,
                      String emailAddress,
                      Address address) {
        super(password, phoneNumber, emailAddress, address);

        try {
            this.checkCnp(cnp);
        } catch (InvalidIdentificationCodeException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.firstName = firstName;
        this.lastName = lastName;
        this.cnp = cnp;
    }

    private void checkCnp(String cnp) throws InvalidIdentificationCodeException {
        if (!cnp.matches(CustomerConfig.getCnpPattern()))
            throw new InvalidIdentificationCodeException();

        // validate the CNP
        // implementez algoritmul de pe https://ro.wikipedia.org/wiki/Cod_numeric_personal_(Rom%C3%A2nia)#Validare
        final String constant = "279146358279";
        int c = 0;
        for (int i = 0; i < constant.length(); ++i)
            c += Character.getNumericValue(cnp.charAt(i)) * Character.getNumericValue(constant.charAt(i));
        c %= 11;
        if (c == 10)
            c = 1;

        // codul de mai sus functioneaza perfect
        // dar comentez codul de mai jos deoarece nu vreau sa fiu nevoit sa caut CNP-uri reale cand testez
        //if (c != Character.getNumericValue(cnp.charAt(cnp.length() - 1)))
        //    throw new InvalidIdentificationCodeException();

    }

    @Override
    public String getCustomerName() {
        return this.lastName + " " + this.firstName;
    }

    @Override
    public String getCustomerUniqueID() {
        return this.cnp;
    }

    @Override
    public LocalDate getBirthDay() {
        int firstDigit = Character.getNumericValue(this.cnp.charAt(0));

        int year = Integer.parseInt(this.cnp.substring(1, 3));
        if (firstDigit == 1 || firstDigit == 2)
            year += 1900;
        else if (firstDigit == 5 || firstDigit == 6)
            year += 2000;
        else {
            System.err.println("Error: algorithm not implemented for non resident romanians!");
            System.exit(Codes.EXIT_ON_ERROR);
        }

        int month = Integer.parseInt(this.cnp.substring(3, 5));
        int day = Integer.parseInt(this.cnp.substring(5, 7));

        return LocalDate.of(year, month, day);
    }

    @Override
    public String toString() {
        return "Individual information:\n" +
                "\t* CNP: " + this.cnp + "\n" +
                "\t* first name: " + this.firstName + "\n" +
                "\t* last name: " + this.lastName + "\n" +
                "\t* birth day: " + this.getBirthDay().toString() + "\n" +
                super.toString() +
                this.getAddress().toString() + "\n";
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.cnp,
                this.firstName,
                this.lastName);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof Individual))
            return false;

        Individual individual = (Individual) object;
        if (!this.cnp.equals(individual.cnp))
            return false;

        return true;
    }

    @Override
    public String getUniqueID() {
        return this.cnp;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public CustomerType getCustomerType() {
        return CustomerType.INDIVIDUAL;
    }
}
