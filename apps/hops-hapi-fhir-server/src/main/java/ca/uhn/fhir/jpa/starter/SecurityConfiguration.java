package ca.uhn.fhir.jpa.starter;

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation;
import org.springframework.context.annotation.Configuration;

// https://github.com/navikt/token-support#token-validation--configuration
@EnableJwtTokenValidation
@Configuration
class SecurityConfiguration {

}