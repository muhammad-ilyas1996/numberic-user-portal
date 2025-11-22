package com.numbericsuserportal.invoiceproduct.entity;

import com.numbericsuserportal.invoice.entity.InvoiceAndTaxEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invoice_product")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private Double quantity;
    private Double amount;
    private String description;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private InvoiceAndTaxEntity invoiceAndTaxEntity;
}
