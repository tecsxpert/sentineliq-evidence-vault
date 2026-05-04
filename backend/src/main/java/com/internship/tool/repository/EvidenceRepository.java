package com.internship.tool.repository;

import com.internship.tool.entity.Evidence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    Page<Evidence> findByUserUsername(String username, Pageable pageable);

    @Query("""
            select e from Evidence e
            where e.user.username = :username
              and (:q is null or :q = '' or
                   lower(e.name) like lower(concat('%', :q, '%')) or
                   lower(coalesce(e.type, '')) like lower(concat('%', :q, '%')) or
                   lower(coalesce(e.status, '')) like lower(concat('%', :q, '%')) or
                   lower(coalesce(e.caseNumber, '')) like lower(concat('%', :q, '%')) or
                   lower(coalesce(e.caseName, '')) like lower(concat('%', :q, '%')) or
                   lower(coalesce(e.tags, '')) like lower(concat('%', :q, '%')))
              and (:type is null or :type = '' or lower(e.type) = lower(:type))
              and (:status is null or :status = '' or lower(e.status) = lower(:status))
              and (:fromDate is null or e.dateCollected >= :fromDate)
              and (:toDate is null or e.dateCollected <= :toDate)
            """)
    Page<Evidence> searchForUser(@Param("username") String username,
                                 @Param("q") String q,
                                 @Param("type") String type,
                                 @Param("status") String status,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate,
                                 Pageable pageable);

    long countByUserUsername(String username);

    long countByUserUsernameAndStatusIgnoreCase(String username, String status);

    @Query("select e.status, count(e) from Evidence e where e.user.username = :username group by e.status")
    List<Object[]> countByStatusForUser(@Param("username") String username);

    @Query("select e.type, count(e) from Evidence e where e.user.username = :username group by e.type")
    List<Object[]> countByTypeForUser(@Param("username") String username);

    @Query("select e.priority, count(e) from Evidence e where e.user.username = :username group by e.priority")
    List<Object[]> countByPriorityForUser(@Param("username") String username);

    List<Evidence> findByDeadlineBetweenAndStatusIgnoreCase(LocalDate start, LocalDate end, String status);
}
