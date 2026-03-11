package com.grocerytrack.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "budget")
@Data
@NoArgsConstructor
public class Budget {

    @Id
    private Long id = 1L; // Singleton row

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal weeklyAmount;
}
