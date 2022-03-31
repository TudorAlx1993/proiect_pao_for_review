package transaction;

import configs.SystemDate;
import utils.AmountFormatter;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class TransactionLogger implements Comparable<TransactionLogger> {
    private final String transactionId;
    private final LocalDate date;
    private final String transactionType;
    private final double amount;
    private final String transactionDetail;

    public TransactionLogger(TransactionType transactionType,
                             double amount,
                             String transactionDetail) {
        this(transactionType, amount, transactionDetail, SystemDate.getDate());
    }

    public TransactionLogger(TransactionType transactionType,
                             double amount,
                             String transactionDetail,
                             LocalDate dateOfTransatcion) {
        this.transactionId = UUID.randomUUID().toString();
        this.date = dateOfTransatcion;
        this.transactionType = transactionType.toString();
        this.amount = Math.abs(amount);
        this.transactionDetail = transactionDetail;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public double getAmount() {
        return this.amount;
    }

    public String getTransactionDetail() {
        return this.transactionDetail;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.amount,
                this.transactionDetail,
                this.transactionType,
                this.transactionId,
                this.date);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null)
            return false;

        if (!(object instanceof TransactionLogger))
            return false;

        TransactionLogger transactionLogger = (TransactionLogger) object;
        if (!this.transactionId.equals(transactionLogger.transactionId))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return this.transactionId +
                "\t" + this.date.toString() +
                "\t" + AmountFormatter.format(this.amount) +
                "\t" + this.transactionType +
                "\t" + (this.transactionDetail != null ? this.transactionDetail : "NA");
    }

    @Override
    public int compareTo(TransactionLogger transaction) {
        return this.date.compareTo(transaction.date);
    }
}
