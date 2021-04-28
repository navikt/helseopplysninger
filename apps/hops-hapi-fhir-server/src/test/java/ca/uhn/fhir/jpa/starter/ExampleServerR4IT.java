package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static java.lang.Thread.sleep;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class, properties =
        {
                "spring.datasource.url=jdbc:h2:mem:dbr4",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri="
        })
public class ExampleServerR4IT {
    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerR4IT.class);
    private IGenericClient ourClient;
    private FhirContext ourCtx;

    @LocalServerPort
    private int port;

    @Test
    @Order(0)
    void testCreateAndRead() {
        String methodName = "testCreateAndRead";
        ourLog.info("Entering " + methodName + "()...");

        Patient pt = new Patient();
        pt.setActive(true);
        pt.getBirthDateElement().setValueAsString("2020-01-01");
        pt.addIdentifier().setSystem("http://foo").setValue("12345");
        pt.addName().setFamily(methodName);
        IIdType id = ourClient.create().resource(pt).execute().getId();

        Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
        assertEquals(methodName, pt2.getName().get(0).getFamily());


        // Wait until the MDM message has been processed
        await().until(() -> {
            sleep(1000);
            return getGoldenResourcePatient() != null;
        });
        Patient goldenRecord = getGoldenResourcePatient();

        assertEquals(pt.getBirthDate(), goldenRecord.getBirthDate());
    }

    private Patient getGoldenResourcePatient() {
        Bundle bundle = ourClient.search()
                .forResource(Patient.class)
                .cacheControl(new CacheControlDirective().setNoCache(true)).returnBundle(Bundle.class).execute();
        if (bundle.getEntryFirstRep() != null) {
            return (Patient) bundle.getEntryFirstRep().getResource();
        } else {
            return null;
        }
    }

    @BeforeEach
    void beforeEach() {

        ourCtx = FhirContext.forR4();
        ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
        String ourServerBase = "http://localhost:" + port + "/fhir/";
        ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
//		ourClient.registerInterceptor(new LoggingInterceptor(false));
    }
}