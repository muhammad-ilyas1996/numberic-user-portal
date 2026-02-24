package com.numbericsuserportal.recieptupload.respository;

import com.numbericsuserportal.recieptupload.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    
    List<Receipt> findByUserId(Long userId);
    
    List<Receipt> findByUserIdAndIsActiveTrue(Long userId);
}
