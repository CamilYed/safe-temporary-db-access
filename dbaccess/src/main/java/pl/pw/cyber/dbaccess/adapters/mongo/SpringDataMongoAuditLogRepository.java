package pl.pw.cyber.dbaccess.adapters.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
interface SpringDataMongoAuditLogRepository extends MongoRepository<MongoTemporaryAccessAuditLog, String> {

    List<MongoTemporaryAccessAuditLog> findByExpiresAtBeforeAndRevokedFalse(Instant now);
}
