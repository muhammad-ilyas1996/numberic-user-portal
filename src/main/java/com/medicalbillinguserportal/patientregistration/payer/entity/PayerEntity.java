package com.medicalbillinguserportal.patientregistration.payer.entity;
import com.medicalbillinguserportal.common.constant.PayerStatus;
import com.medicalbillinguserportal.commonpersistence.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayerEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payer_id")
    private Long payerId;

    @Column(name = "payer")
    private String payer;

    @Column(name = "address1")
    private String address1;

    @Column(name = "address2")
    private String address2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zip")
    private String zip;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "default_lab")
    private String defaultLab;

    @Column(name = "phone")
    private String phone;

    @Column(name = "fax")
    private String fax;

    @Column(name = "payer_type")
    private String payerType;

    @Column(name = "payer_class")
    private String payerClass;

    @Column(name = "claim_format")
    private String claimFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PayerStatus status;
}
