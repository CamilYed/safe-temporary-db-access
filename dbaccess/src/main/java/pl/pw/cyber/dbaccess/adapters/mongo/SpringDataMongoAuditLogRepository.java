package pl.pw.cyber.dbaccess.adapters.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
interface SpringDataMongoAuditLogRepository extends MongoRepository<MongoTemporaryAccessAuditLog, String> {

    Optional<MongoTemporaryAccessAuditLog> findByGrantedUsername(String grantedUsername);

    List<MongoTemporaryAccessAuditLog> findByExpiresAtBeforeAndRevokedIsFalse(Instant expirationTime);
}
