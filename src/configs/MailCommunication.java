package configs;

import java.util.HashSet;
import java.util.Set;

public final class MailCommunication {
    private static final Set<String> blockedMailDomains;

    static {
        blockedMailDomains = new HashSet<String>();
    }

    private MailCommunication() {

    }

    public static HashSet<String> getBlockedMailDomains() {
        return new HashSet<String>(MailCommunication.blockedMailDomains);
    }

    public static void addBlockedMailDomain(String mailDomain) {
        MailCommunication.blockedMailDomains.add(mailDomain);
    }

    public static void deleteBlockedMailDomain(String mailDomain) {
        MailCommunication.blockedMailDomains.remove(mailDomain);
    }

    public static void showBlockedMailDomains() {
        if (MailCommunication.blockedMailDomains.size() == 0)
            System.out.println("\nThere are no blocked mail domains.\n");
        else {
            System.out.println("\nBlocked mail domains:");
            for (String mailDomain : MailCommunication.blockedMailDomains)
                System.out.println("\t* " + mailDomain);
            System.out.println("\n");
        }
    }
}
