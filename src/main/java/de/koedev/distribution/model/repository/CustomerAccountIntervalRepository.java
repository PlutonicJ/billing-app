package de.koedev.distribution.model.repository;

import de.koedev.distribution.model.CustomerAccountInterval;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerAccountIntervalRepository extends JpaRepository<CustomerAccountInterval, Long> {
    @Query("""
                SELECT cai
                FROM CustomerAccountInterval cai
                WHERE cai.customerId = :customerId
                AND cai.createdDateTime = :createdDateTime
                ORDER BY cai.intervalEnd DESC
            """)
    Page<CustomerAccountInterval> findByCustomerIdAndCreatedDateTime(
            @Param("customerId") String customerId,
            @Param("createdDateTime") LocalDateTime now,
            Pageable pageable);

    @Query("""
                SELECT DISTINCT cai.customerId
                FROM CustomerAccountInterval cai
                WHERE cai.createdDateTime = :createdDateTime
                ORDER BY cai.customerId
            """)
    Page<String> findDistinctCustomerIdsByCreatedDateTime(@Param("createdDateTime") LocalDateTime now, Pageable pageable);

    @Query("""
                SELECT COUNT(cai)
                FROM CustomerAccountInterval cai
                WHERE cai.customerId = :customerId
                AND cai.iban = :iban
                AND (
                    (:intervalStart BETWEEN cai.intervalStart AND cai.intervalEnd) OR
                    (:intervalEnd BETWEEN cai.intervalStart AND cai.intervalEnd) OR
                    (cai.intervalStart BETWEEN :intervalStart AND :intervalEnd)
                )
            """)
    int countOverlappingIntervals(@Param("customerId") String customerId,
                                  @Param("iban") String iban,
                                  @Param("intervalStart") LocalDate intervalStart,
                                  @Param("intervalEnd") LocalDate intervalEnd);
}