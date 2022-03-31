ACEST FISIER CONTINE FORMATARI.
RECOMANDARE: DOWNLOAD DE PE GITHUB SI DESCHIDERE IN NOTEPAD

Acest proiect are in vedere implementarea unei aplicatii bancare in Java.
Desi este o simplificare a realitatii, m-am straduit sa utilizez cat mai multe concepte de Java.

Java Version: 18

In legatura cu structurile de date utilizate:
	* folosesc arrays atunci cand cunosc la compile-time nr de elemente din array
	* folosesc ArrayList atunci cand am nevoie de o colectie ordonata si nu stiu cate obiecte trebuie sa memorez la compile-time
	* folosesc HashMap atunci cand am nevoie de o colectie neordonata si unica dpdv al key

Cu exceptia clasei Main, aplicatia este structurata in pachete:
     * pachetul utils
	* clasa statica AmountFormatter are rolul de a formata un numar cu separator de mii si zecimale
	* clasa statica Hash are rolul de a genera hash-ul aferent unei parole utilizand algoritmul SHA-256

     * pachetul tests
	* clasa statica GeneralTest este utilizata pentru a testa diverse sectiuni de cod (aceasta clasa este goala atunci cand proiectul este trimis spre evaluare)
 
     * pachetul transactions
	* clasa TransactionLogger are rolul de a memora tranzactiile derulate de un client pe un cont curent si este utilita mai tarziu pentru a genera un extras de cont
	* enumeratia TransactionDetail defineste tipurile de tranzactii (e.g.: retragere suma ATM, transfer bancar, etc)
	* enumeratia TransactionType defineste tipul tranzactiei, respectiv DEBIT sau CREDIT pe contul curent

     * pachetul service (pachet in care se vor defini serviciile oferite clientilor)
	* clasa abstracta Service (aceasta clasa nu contine nimic, dar este implementata de orice clasa din acest pachet). 
	* clasa ExchangeRateService implementeaza un algorim privind conversia de curs de schimb intre doua monede diferite, dintre care una dintre ele este RON. Clasa este
	  utila in a determina cati bani se obtin in noua moneda atunci cand se doreste schimul unei anumite contitai din cealalta moneda. Transferul intre conturi propriu-zes
	  este implementat de catre banca in clasa Bank din pachetul bank

     * pachetul regulations
	* clasa statica NationalBankRegulations defineste niste constante ce sunt reglementari ale bancii nationale in relatie cu activitatea bancare. Diversi parametri din alte clase
	  vor fi validati in raport cu aceste constate.

     * pachetul exceptions
	* acest pachet contine diverse clasa ce mostenesc clasa Exceptions si au rolul de a valida diverse input-uri referitoare la clienti, produse si servicii bancare
	* ori de cate ori aceste exceptii sunt prinse se afiseaza mesajul corespunzator de eroare si se apleaza System.exit(-1) deoarece input-uri care se valideaza nu au sens 

     * pachetul currency
	* clasa Curreny contine informatii cu privire cu moneda unui anumit produs. Mi-am dat seama dupa ce am scris aceasta clasa ca Java implementeaza un obiect asemanator numit tot Currency
	* peste tot in cadrul acestui proiect, obiectul Currency se refera la propria implementare si NU la implementarea oferita de Java

     * pachetul address
	* clasa Address are rolul de a stoca informatia referitoare la adresa unui client

     * pachetul configs
	* contine mai multe clasa statice
	* rolul acestor clase statice este de organizare a proiectului 
	* datele si metodele membre statice din aceste clase sunt utilizate pentru configurarea aplicatiei bancare
	* sunt implementate configurari cu privire la cursurile de schimb practicate, ratele de dobanda practicate la depozite si credite, comisioane, etc.
	* unele dintre aceste clase ofera si facilitati cu privire la actualizarea unor date membre statice
   
     * pachetul bank
	* clasa Bank (de tip singleton) implementeaza banca propriu-zisa. Aceasta clasa stocheaza elementele de identificare ale bancii (nume, telefon, etc.), lichiditatile bancii (capitalul bancii, in diverse valute),
	  precum si o lista cu clientii bancii. Acesta clasa implementeaza interfata BankActions. Aceasta clasa realizeaza si orice fel de operatiune bancara intre doua conturi.
	* interfata BankActions defineste actiunile pe care management-ul bancii poate sa le realizeze. Aceste actiuni se refera exclusiv la actiunile disponibile catre managementul bancii din meniul interactiv din consola. Actiunile sunt:
		 1) show customers 
                 2) show customer summary 
                 3) show products summary 
                 4) sort customers by number of products (descending) 
                 5) show bank's liquidity 
                 6) set reference exchange rates against RON 
                 7) set the bid spread for exchange rates 
                 8) set the ask spread for exchange rate 
                 9) set interest rates for loans 
                 10) set interest rates for deposits 
                 11) set payment fee for internal transactions 
                 12) set payment fee for external transactions 
                 13) set payment fee for atm withdrawn 
                 14) modify system date 
                 15) add a new customer 
                 16) show current exchange rates 
                 17) show current interest rates 
                 18) show current fees 
                 19) show system date
	  Atunci cand se modifica data sistemului (data calendaristica) algoritmul va trata si situatiile de genul urmator: presupunem ca la data de 31.03.2022 un client crreaza doua depozite, unul cu maturitatea in septembrie 2022 si altul cu
	  maturitatea in martie 2023. Apoi presupunem ca managementul bancii schimba data curenta a sistemului la 01.07.2025. Dupa schimbarea datei sistemului, algoritmul va verifica ca intre timp depozitele au ajuns la maturitatea, iar
	  in acest caz sistemul va lichida (adica sterge depozitele) si va plati suma depozitata + dobanda aferenta catre contul curent al clientului. Aceste tranzactii vor aparea in extrasul de cont cu data reala (adica cea de maturitate a 
	  depozitului) si nu cu data curenta (data sistemului).
	* clasa statica ConsoleMenu este utilizata pentru a creea meniul interactiv. Sunt implementate doua situatii: se logheaza un manager de banca sau un client.

     * pachetul customers
	* clasa abstracta Customer implementeaza datele si membrele comune tuturor clientilor. De asemenea de definesc metode abstracte ce au comportament diferit in functie de clasa care mosteneste aceasta clasa abstracta.
	  Aceasta clasa va fi mostenita de doua clasa: Individual si Company
	  Aceasta clasa implementeaza si interfata CustomerOperations
	  Aceasta clasa contine ca data membra si un ArrayList de produse. Adica un client poate avea unul sau mai multe produse de diverse tipuri. By default, fiecare client este initializat cu un cunt curent in RON (asa se intampla si la fiecare banca din Romania).
	* interfata CustomerOperations defineste actiunile si interogarile pe care un client al bancii poate sa le deruleze. Aceste actiuni se refera exclusiv la actiunile disponibile din meniul interactiv din consola.
		 1) show my products 
                 2) show my current accounts 
                 3) show my debit cards 
                 4) show my deposits 
                 5) show my loans 
                 6) show current interest rates 
                 7) ask for exchange rates 
                 8) update password 
                 9) add new current account 
                 10) delete current acocunt 
                 11) add money to current account 
                 12) create new debit card 
                 13) generate current account statement 
                 14) make payment transfer 
                 15) apply for loan 
                 16) perform currency exchange 
                 17) create deposit 
                 18) delete debit card 
                 19) update debit card PIN 
                 20) withdraw money from ATM 
                 21) liquidate deposit before maturity
	* enumeratia CustomerType defineste tipurile de clienti disponibili: persoane fizice (INDIVIDUAL) sau companie (COMPANY)
	* clasa Individual mosteneste clasa Customer. Aceasta clasa descrie clientii persoane fizice si contine (pe langa datele membre din clasa parinte) date membre specifice unei persoane fizice. De asemenea, 
	  aici sunt implementate si metodele abstract din clasa parinte
	* clasa Company mosteneste clasa Customer. Aceasta clasa descrie clientii persoane juridice.

     * pachetul products
	Acest pachet implementeaza produsele oferite de catre banca
	Am facut o separatie intre partea de produse (cunturi curente, depozite, etc.) si partea de servicii (schimb valutar).
	* enumeratia ProductType defineste tipurile de produse oferite de banca si implementate in aplicatie
	* clasa abstracta Product. Aceasta clasa este mostenita de catre fiecare produs implementat in acest pachet, iar rolul acestei clase este de a putea initializa un arraylist de diferite produse
	  Fiecare produs este caracterizat de urmatoarele informatii: moneda produsului si data deschiderii. 
	  In aceasta clasa este declarata si o metoda abstracta ce va return id-ul unic al produsului. Spre exemplu, pentru un cont curent id-ul unic este reprezentat de IBAN.
	* Clasa CurrentAccount mosteneste clasa Product si implementeaza produsul standard al bancii, adica contul curent.
	  Datele membre sunt reprezentate de IBAN, suma din cont si un ArrayList de obiecte de tip TransactionLogger care va memora tranzactiile facute pe contul curent.
	  Clasa implementeaza diverse metode specifice contului curent 
	  Toate celelalte produse (loan, debit card si deposit) contin ca data membra si un cont curent asociat produsului respectiv. Din ce stiu eu, la fiecare banca orice produs are asociat un cont curent. Adica
	  daca avem la o banca din Romania doua conturi curent in aceeasi valuta, si dorim sa facem un depozit atunci la maturitatea dobanda si suma depozitata se adauga in contul curent de unde s-a constituit intial depozitul
	  bancar. Deci trebuie sa memoram si informatia despre cont curent.
	* Clasa DebitCard mosteneste clasa Product. Aceast clasa implementeaza un produs de tip card de debit.
	  Datele membre ale acestui produs sunt: cardID (adica numarul de pe card), data expirarii, hash-ul PIN-ului, numele de pe card, procesatorul de plati si contul curent la care este atasat acest card.
	  Metodele implementeaza interogari specifice acestui produs.
	* Clasa Deposit mosteneste clasa Product. Aceasta clasa implementeaza depozitul bancar
	  Datele membre sunt reprezentate de: contul curent asociat, suma depozitata, rata dobanzii, dobanda la maturitate, maturitatea in luni si un id unic.
	  Aceasta clasa contine metode ce implementeaza diverse interogari referitoare la acest produs bancar.
	* Clasa Loan mosteneste clasa Product si implementeaza produc bancar de creditare.
	  Datele membre asociate acestui produs sunt: un id unic, contul curent asociat, rata dobanzii, maturitatea in luni, datele de plata (scadentar), valoarea initiala si curenta a creditului, un index catre urmatoarea data de plata.
	   
In clasa Main re realizeaza urmatoarele operatiuni:
	* se initializeaza singurul obiect al clasei Bank
	* se realizeaza anumite configurari ale bancii
	* se intializeaza clienti de tip individuals si companii
	* se exemplifica cate operatiuni care pot fi efectuate de client
	* se transfera executia programului pe baza unui meniu in consola. Aici ne putem loga ca manager al bancii (si putem realiza anumite operatiuni) sau ne logam ca un client al banii (si putem realiza un alt set de operatiuni).
	* atunci cand un client se conecteaza el trebuie sa introduca user-ul (CNP sau CUI) si parola. Exemplu de credentials: 1930729000000 si parola1