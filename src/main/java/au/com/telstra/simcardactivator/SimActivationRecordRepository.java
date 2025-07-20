package au.com.telstra.simcardactivator;

import org.springframework.data.jpa.repository.JpaRepository;

// This interface will allow us to save, find, delete records easily
public interface SimActivationRecordRepository extends JpaRepository<SimActivationRecord, Long> {
}

