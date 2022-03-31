package configs;

import regulations.NationalBankRegulations;

public final class LoanConfig {
    // maturities in months
    private static final int minLoanMaturityIndividual;
    private static final int maxLoanMaturityIndividual;
    private static final int minLoanMaturityCompany;
    private static final int maxLoanMaturityCompany;
    private static final int loanUniqueIdLength;
    private static double maxThresholdDebtRatioIndividual;
    private static double maxThresholdDebtRatioCompany;

    static {
        minLoanMaturityIndividual = 1;
        maxLoanMaturityIndividual = 360;
        minLoanMaturityCompany = 1;
        maxLoanMaturityCompany = 120;
        loanUniqueIdLength = 20;
        maxThresholdDebtRatioIndividual = NationalBankRegulations.getMaxThresholdDebtRatioIndividual();
        maxThresholdDebtRatioCompany = NationalBankRegulations.getMaxThresholdDebtRatioCompany();

    }

    private LoanConfig() {

    }


    public static int getMinLoanMaturityIndividual() {
        return LoanConfig.minLoanMaturityIndividual;
    }

    public static int getMaxLoanMaturityIndividual() {
        return LoanConfig.maxLoanMaturityIndividual;
    }

    public static int getMinLoanMaturityCompany() {
        return LoanConfig.minLoanMaturityCompany;
    }

    public static int getMaxLoanMaturityCompany() {
        return LoanConfig.maxLoanMaturityCompany;
    }

    public static int getLoanUniqueIdLength() {
        return LoanConfig.loanUniqueIdLength;
    }

    public static double getMaxThresholdDebtRatioIndividual() {
        return LoanConfig.maxThresholdDebtRatioIndividual;
    }

    public static double getMaxThresholdDebtRatioCompany() {
        return LoanConfig.maxThresholdDebtRatioCompany;
    }

    public static void setMaxThresholdDebtRatioIndividual(double maxThresholdDebtRatioIndividual) {
        if (maxThresholdDebtRatioIndividual < 0 ||
                maxThresholdDebtRatioIndividual > NationalBankRegulations.getMaxThresholdDebtRatioIndividual())
            return;

        LoanConfig.maxThresholdDebtRatioIndividual = maxThresholdDebtRatioIndividual;
    }

    public static void setMaxThresholdDebtRatioCompany(double maxThresholdDebtRatioCompany) {
        if (maxThresholdDebtRatioCompany < 0 ||
                maxThresholdDebtRatioCompany > NationalBankRegulations.getMaxThresholdDebtRatioCompany())
            return;

        LoanConfig.maxThresholdDebtRatioCompany = maxThresholdDebtRatioCompany;
    }
}
