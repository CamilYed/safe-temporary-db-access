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
                .id(mongo.id()).requestedByUsername(mongo.requestedByUsername())
                .grantedUsername(mongo.grantedUsername())
                .targetDatabase(mongo.targetDatabase())
                .permissionLevel(mongo.permissionLevel())
                .grantedAt(mongo.grantedAt())
                .expiresAt(mongo.expiresAt())
                .revoked(mongo.revoked())
                .build();
    }
}
