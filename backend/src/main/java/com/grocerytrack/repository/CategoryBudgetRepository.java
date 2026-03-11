package com.grocerytrack.repository;

import com.grocerytrack.model.CategoryBudget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {

    Optional<CategoryBudget> findByCategory(String category);
}
