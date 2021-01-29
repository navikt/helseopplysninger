package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.api.config.DaoConfig;
import ca.uhn.fhir.jpa.binstore.DatabaseBlobBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.binstore.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.config.HibernateDialectProvider;
import ca.uhn.fhir.jpa.model.config.PartitionSettings;
import ca.uhn.fhir.jpa.model.config.PartitionSettings.CrossPartitionReferenceMode;
import ca.uhn.fhir.jpa.model.entity.ModelConfig;
import org.hl7.fhir.dstu2.model.Subscription;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * This is the primary configuration file for the example server
 */
@Configuration
@EnableTransactionManagement
public class FhirServerConfigCommon {

  private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirServerConfigCommon.class);


  public FhirServerConfigCommon(AppProperties appProperties) {
    ourLog.info("Server configured to " + (appProperties.getAllow_contains_searches() ? "allow" : "deny") + " contains searches");
    ourLog.info("Server configured to " + (appProperties.getAllow_multiple_delete() ? "allow" : "deny") + " multiple deletes");
    ourLog.info("Server configured to " + (appProperties.getAllow_external_references() ? "allow" : "deny") + " external references");
    ourLog.info("Server configured to " + (appProperties.getExpunge_enabled() ? "enable" : "disable") + " expunges");
    ourLog.info("Server configured to " + (appProperties.getAllow_placeholder_references() ? "allow" : "deny") + " placeholder references");
    ourLog.info("Server configured to " + (appProperties.getAllow_override_default_search_params() ? "allow" : "deny") + " overriding default search params");

    if (appProperties.getSubscription().getResthook_enabled()) {
      ourLog.info("REST-hook subscriptions enabled");
    }
  }

  /**
   * Configure FHIR properties around the the JPA server via this bean
   */
  @Bean()
  public DaoConfig daoConfig(AppProperties appProperties) {
    DaoConfig retVal = new DaoConfig();

    retVal.setIndexMissingFields(appProperties.getEnable_index_missing_fields() ? DaoConfig.IndexEnabledEnum.ENABLED : DaoConfig.IndexEnabledEnum.DISABLED);
    retVal.setAutoCreatePlaceholderReferenceTargets(appProperties.getAuto_create_placeholder_reference_targets());
    retVal.setEnforceReferentialIntegrityOnWrite(appProperties.getEnforce_referential_integrity_on_write());
    retVal.setEnforceReferentialIntegrityOnDelete(appProperties.getEnforce_referential_integrity_on_delete());
    retVal.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    retVal.setAllowMultipleDelete(appProperties.getAllow_multiple_delete());
    retVal.setAllowExternalReferences(appProperties.getAllow_external_references());
    retVal.setExpungeEnabled(appProperties.getExpunge_enabled());
    if(appProperties.getSubscription() != null && appProperties.getSubscription().getEmail() != null)
      retVal.setEmailFromAddress(appProperties.getSubscription().getEmail().getFrom());

    Integer maxFetchSize =  appProperties.getMax_page_size();
    retVal.setFetchSizeDefaultMaximum(maxFetchSize);
    ourLog.info("Server configured to have a maximum fetch size of " + (maxFetchSize == Integer.MAX_VALUE ? "'unlimited'" : maxFetchSize));

    Long reuseCachedSearchResultsMillis = appProperties.getReuse_cached_search_results_millis();
    retVal.setReuseCachedSearchResultsForMillis(reuseCachedSearchResultsMillis);
    ourLog.info("Server configured to cache search results for {} milliseconds", reuseCachedSearchResultsMillis);


    Long retainCachedSearchesMinutes = appProperties.getRetain_cached_searches_mins();
    retVal.setExpireSearchResultsAfterMillis(retainCachedSearchesMinutes * 60 * 1000);

    if(appProperties.getSubscription() != null) {
      // Subscriptions are enabled by channel type
      if (appProperties.getSubscription().getResthook_enabled()) {
        ourLog.info("Enabling REST-hook subscriptions");
        retVal.addSupportedSubscriptionType(org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType.RESTHOOK);
      }
    }

    retVal.setFilterParameterEnabled(appProperties.getFilter_search_enabled());

    return retVal;
  }

  @Bean
  public YamlPropertySourceLoader yamlPropertySourceLoader() {
    return new YamlPropertySourceLoader();
  }

  @Bean
  public PartitionSettings partitionSettings(AppProperties appProperties) {
    PartitionSettings retVal = new PartitionSettings();

    // Partitioning
    if (appProperties.getPartitioning() != null) {
      retVal.setPartitioningEnabled(true);
      retVal.setIncludePartitionInSearchHashes(appProperties.getPartitioning().getPartitioning_include_in_search_hashes());
      if(appProperties.getPartitioning().getAllow_references_across_partitions()) {
        retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.ALLOWED_UNQUALIFIED);
      } else {
        retVal.setAllowReferencesAcrossPartitions(CrossPartitionReferenceMode.NOT_ALLOWED); 
      }
    }

    return retVal;
  }


  @Primary
  @Bean
  public HibernateDialectProvider jpaStarterDialectProvider(LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
    return new JpaHibernateDialectProvider(myEntityManagerFactory);
  }

  @Bean
  public ModelConfig modelConfig(AppProperties appProperties) {
    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setAllowContainsSearches(appProperties.getAllow_contains_searches());
    modelConfig.setAllowExternalReferences(appProperties.getAllow_external_references());
    modelConfig.setDefaultSearchParamsCanBeOverridden(appProperties.getAllow_override_default_search_params());

    // You can enable these if you want to support Subscriptions from your server
    if (appProperties.getSubscription() != null && appProperties.getSubscription().getResthook_enabled() != null) {
      modelConfig.addSupportedSubscriptionType(Subscription.SubscriptionChannelType.RESTHOOK);
    }

    return modelConfig;
  }

  @Lazy
  @Bean
  public IBinaryStorageSvc binaryStorageSvc(AppProperties appProperties) {
    DatabaseBlobBinaryStorageSvcImpl binaryStorageSvc = new DatabaseBlobBinaryStorageSvcImpl();

    if (appProperties.getMax_binary_size() != null) {
      binaryStorageSvc.setMaximumBinarySize(appProperties.getMax_binary_size());
    }

    return binaryStorageSvc;
  }
}
