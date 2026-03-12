package com.grocerytrack.repository;

import com.grocerytrack.model.ReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReceiptItemRepository extends JpaRepository<ReceiptItem, Long> {

    List<ReceiptItem> findByReceiptId(Long receiptId);

    @Query("SELECT ri.category, ri.categoryColor, SUM(ri.price) FROM ReceiptItem ri "
            + "JOIN ri.receipt r WHERE r.receiptDate BETWEEN :start AND :end "
            + "GROUP BY ri.category, ri.categoryColor")
    List<Object[]> sumByCategory(LocalDate start, LocalDate end);

    @Query("SELECT ri.category, ri.categoryColor, SUM(ri.price) FROM ReceiptItem ri GROUP BY ri.category, ri.categoryColor")
    List<Object[]> sumByCategoryAllTime();
}
