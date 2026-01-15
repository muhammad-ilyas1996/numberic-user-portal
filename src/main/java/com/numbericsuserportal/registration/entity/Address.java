package com.numbericsuserportal.registration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;
    
    @Column(name = "line1", nullable = false, length = 255)
    private String line1;
    
    @Column(name = "line2", length = 255)
    private String line2;
    
    @Column(name = "city", nullable = false, length = 100)
    private String city;
    
    @Column(name = "state_province", nullable = false, length = 100)
    private String stateProvince;
    
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;
    
    @Column(name = "country", nullable = false, length = 2)
    private String country; // ISO-3166-1 alpha-2
}

