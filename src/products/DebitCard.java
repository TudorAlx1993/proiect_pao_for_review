package products;

import configs.CardConfig;
import configs.Codes;
import configs.SystemDate;
import exceptions.NotSupportedCardNetworkProcessor;
import exceptions.WeakPasswordException;
import io.Database;
import io.DatabaseTable;
import regulations.NationalBankRegulations;
import utils.Hash;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DebitCard extends Product {
    private static int noDebitCards;

    private final String cardId;
    private final LocalDate expirationDate;
    private String hashOfPin;
    // cardul este asignat unui cont curent
    private final CurrentAccount currentAccount;
    private final String nameOnCard;
    private final String networkProcessorName;

    static {
        DebitCard.noDebitCards = 0;
    }

    public DebitCard(CurrentAccount currentAccount,
                     String cardID,
                     LocalDate openDate,
                     LocalDate expirationDate,
                     String hashOfPin,
                     String nameOnCard,
                     String networkProcessorName) {
        // this constructor will be used when creating debit cards from csv files and mysql database

        super(currentAccount.getCurrency(), openDate);

        try {
            this.validateNetworkProcessor(networkProcessorName);
        } catch (NotSupportedCardNetworkProcessor exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.cardId = cardID;
        this.hashOfPin = hashOfPin;
        this.currentAccount = currentAccount;
        this.nameOnCard = nameOnCard;
        this.networkProcessorName = networkProcessorName.toUpperCase();
        this.expirationDate = expirationDate;
    }

    public DebitCard(CurrentAccount currentAccount,
                     LocalDate openDate,
                     String pin,
                     String nameOnCard,
                     String networkProcessorName) {
        super(currentAccount.getCurrency(), openDate);

        try {
            this.checkPinRequirments(pin);
            this.validateNetworkProcessor(networkProcessorName);
        } catch (WeakPasswordException |
                NotSupportedCardNetworkProcessor exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        DebitCard.noDebitCards += 1;

        this.cardId = this.generateUniqueID(CardConfig.getCardNumberLength(), 10);
        this.hashOfPin = Hash.computeHashOfString(pin, CardConfig.getPinHashAlg());
        this.currentAccount = currentAccount;
        this.nameOnCard = nameOnCard;
        this.networkProcessorName = networkProcessorName.toUpperCase();
        this.expirationDate = this.getOpenDate().plusYears(3);
    }

    public DebitCard(CurrentAccount currentAccount,
                     String pin,
                     String nameOnCard,
                     String networkProcessorName) {
        this(currentAccount, SystemDate.getDate(), pin, nameOnCard, networkProcessorName);
    }

    private void checkPinRequirments(String pin) throws WeakPasswordException {
        if (!pin.matches(CardConfig.getPinPattern()))
            throw new WeakPasswordException("Error: the provided PIN does not respect the security standards!");
    }

    private void validateNetworkProcessor(String networkProcessorName) throws NotSupportedCardNetworkProcessor {
        for (String allowedProcessor : NationalBankRegulations.getNetworkProcessors())
            if (allowedProcessor.equalsIgnoreCase(networkProcessorName))
                return;

        throw new NotSupportedCardNetworkProcessor("Error: " + networkProcessorName.toUpperCase() + " is not supported as a network payment!");
    }

    public boolean validatePin(String pin) {
        return this.hashOfPin.equals(Hash.computeHashOfString(pin, CardConfig.getPinHashAlg()));
    }

    public void changePin(String oldPin, String newPin) {
        if (!this.validatePin(oldPin))
            return;

        try {
            this.checkPinRequirments(newPin);
        } catch (WeakPasswordException exception) {
            System.err.println(exception.getMessage());
            System.exit(Codes.EXIT_ON_ERROR);
        }

        this.hashOfPin = Hash.computeHashOfString(newPin, CardConfig.getPinHashAlg());
        Database.updateEntity(DatabaseTable.DEBIT_CARDS,"hash_of_pin",this.hashOfPin,this.getProductUniqueId());
    }

    private LocalDate generateExpirationDate() {
        return this.getOpenDate().plusYears(CardConfig.getYearsOfValability());
    }

    public boolean isCardExpired() {

        if (SystemDate.getDate().compareTo(this.expirationDate) > 0)
            return true;
        else
            return false;
    }

    public static int getNoDebitCards() {
        return DebitCard.noDebitCards;
    }

    public static void setNoDebitCards(int noDebitCards) {
        DebitCard.noDebitCards = noDebitCards;
    }

    public String getCardId() {
        return this.cardId;
    }

    public CurrentAccount getCurrentAcount() {
        return this.currentAccount;
    }

    @Override
    public String getProductUniqueId() {
        return this.getCardId();
    }

    public LocalDate getExpirationDate() {
        return this.expirationDate;
    }

    public String getHashOfPin() {
        return this.hashOfPin;
    }

    public String getNameOnCard() {
        return this.nameOnCard;
    }

    public String getNetworkProcessorName() {
        return this.networkProcessorName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                this.cardId,
                this.expirationDate.hashCode(),
                this.hashOfPin,
                this.currentAccount.hashCode(),
                this.nameOnCard,
                this.networkProcessorName);
    }

    @Override
    public String toString() {
        return "Debit card details:\n" +
                "\t* network processor: " + this.networkProcessorName + "\n" +
                "\t* name on card: " + this.nameOnCard + "\n" +
                "\t* expiration date: " + this.expirationDate.toString() + "\n" +
                "\t* IBAN associated to: " + this.currentAccount.getIBAN() + "\n" +
                super.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof DebitCard))
            return false;

        DebitCard debitCard = (DebitCard) object;
        if (!this.cardId.equals(debitCard.cardId))
            return false;

        return true;
    }

    @Override
    public ProductType getProductType() {
        return ProductType.DEBIT_CARD;
    }

    @Override
    public List<String> getHeaderForCsvFile() {
        List<String> fileHeader = Stream.of("card_id",
                        "expiration_data",
                        "hash_of_pin",
                        "name_on_card",
                        "network_processor_name",
                        "associated_iban")
                .map(String::toUpperCase)
                .collect(Collectors.toList());
        fileHeader.addAll(super.getHeaderForCsvFile());

        return fileHeader;
    }

    @Override
    public List<String> getDataForCsvWriting(String customerID) {
        List<String> lineContent = new ArrayList<>();

        lineContent.add(this.cardId);
        lineContent.add(expirationDate.toString());
        lineContent.add(hashOfPin);
        lineContent.add(this.nameOnCard);
        lineContent.add(this.networkProcessorName);
        lineContent.add(this.currentAccount.getIBAN());
        lineContent.addAll(super.getDataForCsvWriting(customerID));

        return lineContent;
    }
}

