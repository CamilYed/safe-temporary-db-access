package pl.pw.cyber.dbaccess.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pl.pw.cyber.dbaccess.adapters.mongo.MongoTemplateAuditLogTestFetcher
import pl.pw.cyber.dbaccess.adapters.mongo.MongoTestFetcherConfig
import spock.lang.Shared

@Import(MongoTestFetcherConfig)
@ContextConfiguration(initializers = MongoDbInitializer.class)
class MongoBaseIT extends BaseIT {

    @Autowired
    private MongoTemplateAuditLogTestFetcher mongoAuditLogTestFetcher

    def setup() {
        println("Deleting all documents")
        mongoAuditLogTestFetcher.deleteAllDocuments()
    }

    @Shared
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0")).withReuse(true)

    @Component
    static class MongoDbInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            if (!mongoDBContainer.isRunning()) {
                mongoDBContainer.start()
                println "[Testcontainers] Starting mongo ${mongoDBContainer.containerName}"
            }
            def mongoUri = mongoDBContainer.getReplicaSetUrl() + "?connectTimeoutMS=100&serverSelectionTimeoutMS=100"
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    "spring.data.mongodb.uri=" + mongoUri
            )
            println "[Testcontainers] MongoDB Initialized: ${mongoDBContainer.getReplicaSetUrl()}"
        }
    }
}
