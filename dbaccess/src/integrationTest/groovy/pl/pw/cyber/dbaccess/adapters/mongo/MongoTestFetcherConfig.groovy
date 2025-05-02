package pl.pw.cyber.dbaccess.adapters.mongo


import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.mongodb.core.MongoTemplate

@TestConfiguration
class MongoTestFetcherConfig {

    @Bean
    MongoTemplateAuditLogTestFetcher mongoTemplateAuditLogTestFetcher(MongoTemplate mongoTemplate) {
        return new MongoTemplateAuditLogTestFetcher(mongoTemplate)
    }
}