package com.prakhar.crm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "contracts")
@Data
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "contract_value")
private Double value; 

    private LocalDate startDate;

    private LocalDate endDate;

@Enumerated(EnumType.STRING)
@Column(columnDefinition = "varchar(20)")
private ContractStatus status;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}