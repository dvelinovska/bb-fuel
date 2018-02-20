package com.backbase.testing.dataloader.configurators;

import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Consumer;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.Provider;
import com.backbase.integration.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementPostResponseBody;
import com.backbase.presentation.accessgroup.rest.spec.v2.accessgroups.serviceagreements.ServiceAgreementGetResponseBody;
import com.backbase.presentation.user.rest.spec.v2.users.LegalEntityByUserGetResponseBody;
import com.backbase.testing.dataloader.clients.accessgroup.AccessGroupPresentationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsIntegrationRestClient;
import com.backbase.testing.dataloader.clients.accessgroup.ServiceAgreementsPresentationRestClient;
import com.backbase.testing.dataloader.clients.common.LoginRestClient;
import com.backbase.testing.dataloader.clients.legalentity.LegalEntityPresentationRestClient;
import com.backbase.testing.dataloader.clients.user.UserPresentationRestClient;
import com.backbase.testing.dataloader.data.ServiceAgreementsDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

import static com.backbase.testing.dataloader.data.CommonConstants.USER_ADMIN;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

public class ServiceAgreementsConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAgreementsConfigurator.class);

    private LoginRestClient loginRestClient = new LoginRestClient();
    private UserPresentationRestClient userPresentationRestClient = new UserPresentationRestClient();
    private LegalEntityPresentationRestClient legalEntityPresentationRestClient = new LegalEntityPresentationRestClient();
    private ServiceAgreementsIntegrationRestClient serviceAgreementsIntegrationRestClient = new ServiceAgreementsIntegrationRestClient();
    private ServiceAgreementsDataGenerator serviceAgreementsDataGenerator = new ServiceAgreementsDataGenerator();
    private AccessGroupPresentationRestClient accessGroupPresentationRestClient = new AccessGroupPresentationRestClient();

    public String ingestServiceAgreementWithProvidersAndConsumersWithAllFunctionDataGroups(Set<Provider> providers, Set<Consumer> consumers) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);
        accessGroupPresentationRestClient.selectContextBasedOnMasterServiceAgreement();
        enrichConsumersWithId(consumers);
        enrichProvidersWithId(providers);

        String serviceAgreementId = serviceAgreementsIntegrationRestClient.ingestServiceAgreement(serviceAgreementsDataGenerator.generateServiceAgreementPostRequestBody(providers, consumers))
                .then()
                .statusCode(SC_CREATED)
                .extract()
                .as(ServiceAgreementPostResponseBody.class)
                .getId();

        LOGGER.info(String.format("Service agreement ingested for provider legal entities - admins/users %s, consumer legal entities - admins with all function groups and data groups exposed %s", Arrays.toString(providers.toArray()), Arrays.toString(consumers.toArray())));

        return serviceAgreementId;
    }

    public void updateMasterServiceAgreementWithExternalIdByUser(String externalUserId) {
        loginRestClient.login(USER_ADMIN, USER_ADMIN);

        String internalLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalUserId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(LegalEntityByUserGetResponseBody.class)
            .getId();

        String internalServiceAgreementId = legalEntityPresentationRestClient.getMasterServiceAgreementOfLegalEntity(internalLegalEntityId)
            .then()
            .statusCode(SC_OK)
            .extract()
            .as(ServiceAgreementGetResponseBody.class)
            .getId();

        serviceAgreementsIntegrationRestClient.updateServiceAgreement(internalServiceAgreementId, serviceAgreementsDataGenerator.generateServiceAgreementPutRequestBody())
            .then()
            .statusCode(SC_OK);

        LOGGER.info(String.format("Service agreement [%s] updated with external id", internalServiceAgreementId));
    }

    private void enrichConsumersWithId(Set<Consumer> consumers) {
        for (Consumer consumer : consumers) {
            String externalConsumerAdminUserId = consumer.getAdmins()
                    .iterator()
                    .next();

            String externalConsumerLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalConsumerAdminUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class)
                .getExternalId();

            consumer.setId(externalConsumerLegalEntityId);
        }
    }

    private void enrichProvidersWithId(Set<Provider> providers) {
        for (Provider provider : providers) {
            String externalProviderAdminUserId = provider.getAdmins()
                    .iterator()
                    .next();

            String externalProviderLegalEntityId = userPresentationRestClient.retrieveLegalEntityByExternalUserId(externalProviderAdminUserId)
                    .then()
                    .statusCode(SC_OK)
                    .extract()
                    .as(LegalEntityByUserGetResponseBody.class)
                    .getExternalId();

            provider.setId(externalProviderLegalEntityId);
        }
    }
}
