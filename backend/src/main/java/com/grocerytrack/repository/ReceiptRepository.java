package com.grocerytrack.repository;

import com.grocerytrack.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    List<Receipt> findAllByOrderByReceiptDateDescCreatedAtDesc();

    List<Receipt> findByReceiptDateBetweenOrderByReceiptDateDesc(LocalDate start, LocalDate end);

    @Query("SELECT SUM(r.total) FROM Receipt r")
    java.math.BigDecimal sumTotal();

    @Query("SELECT SUM(r.total) FROM Receipt r WHERE r.receiptDate BETWEEN :start AND :end")
    java.math.BigDecimal sumTotalBetween(LocalDate start, LocalDate end);

    @Query("SELECT SUM(r.itemsCount) FROM Receipt r WHERE r.receiptDate BETWEEN :start AND :end")
    Long sumItemsBetween(LocalDate start, LocalDate end);
}
