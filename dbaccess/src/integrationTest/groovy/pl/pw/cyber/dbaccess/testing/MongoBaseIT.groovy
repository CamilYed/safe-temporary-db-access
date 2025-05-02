package pl.pw.cyber.dbaccess.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import pl.pw.cyber.dbaccess.adapters.mongo.MongoTemplateAuditLogTestFetcher
import spock.lang.Shared

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

    static class MongoDbInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            if (!mongoDBContainer.isRunning()) {
               mongoDBContainer.start()
            }
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    "spring.data.mongodb.uri=" + mongoDBContainer.getReplicaSetUrl()
            )
            println "[Testcontainers] MongoDB Initialized: ${mongoDBContainer.getReplicaSetUrl()}"

        }
    }
}
