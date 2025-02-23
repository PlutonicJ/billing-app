package de.koedev.distribution.model.repository;

import de.koedev.distribution.model.CustomerAccountInterval;
import de.koedev.distribution.model.CustomerIdIbanCombination;
import de.koedev.distribution.model.TransactionInfo;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionInfoRepository extends CrudRepository<TransactionInfo, Long> {

    @Query("""
                SELECT DISTINCT new de.koedev.distribution.model.CustomerIdIbanCombination(ti.customerId, ti.iban)
                FROM TransactionInfo ti
                WHERE ti.createdDateTime < :now
                ORDER BY ti.customerId, ti.iban
            """)
    Page<CustomerIdIbanCombination> findCustomerIdIbanCombinations(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
                SELECT DISTINCT new de.koedev.distribution.model.CustomerIdIbanCombination(ti.customerId, ti.iban)
                FROM TransactionInfo ti
                WHERE ti.customerId = :customerId
                AND ti.createdDateTime <= :now
                ORDER BY ti.customerId, ti.iban
            """)
    Page<CustomerIdIbanCombination> findCustomerIdIbanCombinations(@Param("customerId") String customerId, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
                SELECT DISTINCT new de.koedev.distribution.model.CustomerIdIbanCombination(ti.customerId, ti.iban)
                FROM TransactionInfo ti
                WHERE ti.customerId = :customerId
                AND ti.iban = :iban
                AND ti.createdDateTime <= :now
                ORDER BY ti.customerId, ti.iban
            """)
    Page<CustomerIdIbanCombination> findCustomerIdIbanCombinations(@Param("customerId") String customerId, @Param("iban") String iban, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("""
                SELECT ti
                FROM TransactionInfo ti
                WHERE ti.customerId = :#{#interval.customerId}
                AND ti.iban = :#{#interval.iban}
                AND ti.createdDateTime >= :#{#interval.intervalStart}
                AND ti.createdDateTime < :#{#interval.intervalEnd}
                ORDER BY ti.createdDateTime ASC
            """)
    Page<TransactionInfo> findTransactionInfosForCustomerAccountInterval(
            @Param("interval") CustomerAccountInterval interval,
            Pageable pageable);
}