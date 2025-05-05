package pl.pw.cyber.dbaccess.adapters.mongo

import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog

class MongoTemplateAuditLogTestFetcher {

    private final MongoTemplate mongoTemplate

    MongoTemplateAuditLogTestFetcher(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate
    }

    List<TemporaryAccessAuditLog> findAllDocuments() {
        return mongoTemplate.findAll(MongoTemporaryAccessAuditLog.class)
                .collect {
                    it -> mapToDomainObject(it)
                }
    }

    long countDocuments() {
        return mongoTemplate.count(new Query(), MongoTemporaryAccessAuditLog.class)
    }

    void deleteAllDocuments() {
        mongoTemplate.remove(new Query(), MongoTemporaryAccessAuditLog.class)
    }

    private static TemporaryAccessAuditLog mapToDomainObject(MongoTemporaryAccessAuditLog mongo) {
        return TemporaryAccessAuditLog.builder()
                .withId(mongo.id())
                .withRequestedByUsername(mongo.requestedByUsername())
                .withGrantedUsername(mongo.grantedUsername())
                .withTargetDatabase(mongo.targetDatabase())
                .withPermissionLevel(mongo.permissionLevel())
                .withGrantedAt(mongo.grantedAt())
                .withExpiresAt(mongo.expiresAt())
                .withRevoked(mongo.revoked())
                .build();
    }
}
