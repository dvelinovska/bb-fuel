package com.backbase.ct.bbfuel.data;

import static com.backbase.ct.bbfuel.util.CommonHelpers.generateRandomNumberInRange;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromEnumValues;
import static com.backbase.ct.bbfuel.util.CommonHelpers.getRandomFromList;
import static com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator.CRDT;
import static java.util.Arrays.asList;

import com.backbase.ct.bbfuel.util.CommonHelpers;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody;
import com.backbase.integration.transaction.external.rest.spec.v2.transactions.TransactionsPostRequestBody.CreditDebitIndicator;
import com.backbase.presentation.categories.management.rest.spec.v2.categories.id.CategoryGetResponseBody;
import com.github.javafaker.Faker;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.DateUtils;
import org.iban4j.Iban;

public class TransactionsDataGenerator {

    private static Faker faker = new Faker();
    private static final String EUR_CURRENCY = "EUR";
    private static final List<String> TRANSACTION_TYPE_GROUPS = asList(
        "Payment",
        "Withdrawal",
        "Loans",
        "Fees"
    );

    private static final List<String> TRANSACTION_TYPES = asList(
        "SEPA CT",
        "SEPA DD",
        "BACS (UK)",
        "Faster payment (UK)",
        "CHAPS (UK)",
        "International payment",
        "Loan redemption",
        "Interest settlement"
    );
    private static final List<String> DEBIT_BUSINESS_CATEGORIES = asList(
        "Suppliers",
        "Salaries",
        "Office rent",
        "Loan repayment",
        "Miscellaneous"
    );
    private static final List<String> CREDIT_BUSINESS_CATEGORIES = asList(
        "Intercompany receivable",
        "Intracompany receivable",
        "Direct debit collections",
        "Interest received",
        "Term deposit"
    );
    private static List<String> DEBIT_RETAIL_CATEGORIES = asList(
        "Food Drinks",
        "Transportation",
        "Home",
        "Health Beauty",
        "Shopping",
        "Bills Utilities",
        "Hobbies Entertainment",
        "Transfers",
        "Uncategorised",
        "Car",
        "Beauty",
        "Health Fitness",
        "Mortgage",
        "Rent",
        "Public Transport",
        "Internet",
        "Mobile Phone",
        "Utilities",
        "Alcohol Bars",
        "Fast Food",
        "Groceries",
        "Restaurants",
        "Clothing",
        "Electronics"
    );
    private static List<String> CREDIT_RETAIL_CATEGORIES = asList(
        "Income",
        "Other Income",
        "Bonus",
        "Salary/Wages",
        "Interest Income",
        "Rental Income"
    );

    public static TransactionsPostRequestBody generateTransactionsPostRequestBody(String externalArrangementId,
        boolean isRetail, List<CategoryGetResponseBody> categories) {
        CreditDebitIndicator creditDebitIndicator = getRandomFromEnumValues(CreditDebitIndicator.values());

        String finalCategory;

        if (isRetail) {
            if (!categories.isEmpty()) {
                CREDIT_RETAIL_CATEGORIES = categories.stream()
                    .filter(category -> "INCOME".equals(category.getCategoryType()))
                    .map(CategoryGetResponseBody::getCategoryName)
                    .collect(Collectors.toList());

                CREDIT_RETAIL_CATEGORIES = categories.stream()
                    .filter(category -> "EXPENSE".equals(category.getCategoryType()))
                    .map(CategoryGetResponseBody::getCategoryName)
                    .collect(Collectors.toList());
            }

            finalCategory = creditDebitIndicator == CRDT
                ? getRandomFromList(CREDIT_RETAIL_CATEGORIES)
                : getRandomFromList(DEBIT_RETAIL_CATEGORIES);
        } else {
            finalCategory = creditDebitIndicator == CRDT
                ? getRandomFromList(CREDIT_BUSINESS_CATEGORIES)
                : getRandomFromList(DEBIT_BUSINESS_CATEGORIES);
        }

        return new TransactionsPostRequestBody().withId(UUID.randomUUID().toString())
            .withArrangementId(externalArrangementId)
            .withReference(faker.lorem().characters(10))
            .withDescription(faker.lorem().sentence().replace(".", ""))
            .withTypeGroup(getRandomFromList(TRANSACTION_TYPE_GROUPS))
            .withType(getRandomFromList(TRANSACTION_TYPES))
            .withCategory(finalCategory)
            .withBookingDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withValueDate(DateUtils.addDays(new Date(), generateRandomNumberInRange(-365, 0)))
            .withAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
            .withCurrency(EUR_CURRENCY)
            .withCreditDebitIndicator(creditDebitIndicator)
            .withInstructedAmount(CommonHelpers.generateRandomAmountInRange(100L, 9999L))
            .withInstructedCurrency(EUR_CURRENCY)
            .withCurrencyExchangeRate(CommonHelpers.generateRandomAmountInRange(1L, 2L))
            .withCounterPartyName(faker.name().fullName())
            .withCounterPartyAccountNumber(Iban.random().toString())
            .withCounterPartyBIC(faker.finance().bic())
            .withCounterPartyCountry(faker.address().countryCode())
            .withCounterPartyBankName(faker.company().name());
    }
}
