package com.numbericsuserportal.registration.service;

import com.numbericsuserportal.registration.dto.*;
import com.numbericsuserportal.registration.entity.*;
import com.numbericsuserportal.registration.repo.*;
import com.numbericsuserportal.usermanagement.domain.PortalType;
import com.numbericsuserportal.usermanagement.domain.Role;
import com.numbericsuserportal.usermanagement.domain.User;
import com.numbericsuserportal.usermanagement.domain.UserRole;
import com.numbericsuserportal.usermanagement.domain.UserRoleId;
import com.numbericsuserportal.usermanagement.repo.PortalTypeRepository;
import com.numbericsuserportal.usermanagement.repo.RoleRepository;
import com.numbericsuserportal.usermanagement.repo.UserRepository;
import com.numbericsuserportal.usermanagement.repo.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RegistrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PortalTypeRepository portalTypeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private BusinessProfileRepository businessProfileRepository;
    
    @Autowired
    private BeneficialOwnerRepository beneficialOwnerRepository;
    
    @Autowired
    private BankAccountRepository bankAccountRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRoleRepository userRoleRepository;
    
    @Transactional
    public User registerStep1(RegistrationStep1Dto dto) {
        // Get NUMBRICS portal
        PortalType portalType = portalTypeRepository.findByPortalName(PortalType.NUMBRICS_PORTAL_NAME)
            .orElseThrow(() -> new RuntimeException("NUMBRICS Portal not found"));
        
        // Create user
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setUsername(dto.getEmail()); // Use email as username
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());
        user.setAccountType(User.AccountType.valueOf(dto.getAccountType().name()));
        user.setTermsAccepted(dto.getTermsAccepted());
        user.setMarketingOptIn(dto.getMarketingOptIn() != null ? dto.getMarketingOptIn() : false);
        user.setRegistrationStatus(User.RegistrationStatus.STEP1_COMPLETED);
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setPortalType(portalType);
        user.setUserType(User.UserType.practice); // Default type
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        User savedUser = userRepository.save(user);
        
        // Save company name if provided (for business accounts)
        if (dto.getCompanyName() != null && !dto.getCompanyName().trim().isEmpty() 
            && dto.getAccountType() == RegistrationStep1Dto.AccountType.business) {
            BusinessProfile profile = new BusinessProfile();
            profile.setUserId(savedUser.getUserId());
            profile.setCompanyName(dto.getCompanyName());
            businessProfileRepository.save(profile);
        }
        
        // Assign role (required field - validated in DTO)
        // Available roles: Founder, Business_Owner, Accountant_Pro
        String roleCode = dto.getPreferredRole().trim();
        assignRole(savedUser, roleCode);
        
        return savedUser;
    }
    
    private void assignRole(User user, String roleCode) {
        // Validate role code (should not be null/empty as it's required in DTO)
        if (roleCode == null || roleCode.trim().isEmpty()) {
            throw new RuntimeException("Role is required. Available roles: Founder, Business_Owner, Accountant_Pro");
        }
        
        // Available roles: Founder, Business_Owner, Accountant_Pro
        Optional<Role> role = roleRepository.findByCodeName(roleCode.trim());
        
        if (role.isEmpty()) {
            throw new RuntimeException("Role '" + roleCode + "' not found. Available roles: Founder, Business_Owner, Accountant_Pro");
        }
        
        // Assign role to user
        UserRole userRole = new UserRole();
        UserRoleId userRoleId = new UserRoleId();
        userRoleId.setUserId(user.getUserId());
        userRoleId.setRoleId(role.get().getRoleId());
        userRole.setId(userRoleId);
        userRole.setUser(user);
        userRole.setRole(role.get());
        userRole.setIsActive(true);
        userRole.setCreatedAt(LocalDateTime.now());
        userRole.setAddedBy(user.getUserId());
        userRoleRepository.save(userRole);
        
        System.out.println("Role assigned: " + role.get().getCodeName() + " to user: " + user.getEmail());
    }
    
    @Transactional
    public BusinessProfile registerStep2(Long userId, RegistrationStep2Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Step 2 is only for business accounts
        if (user.getAccountType() != User.AccountType.business) {
            // For individual accounts, just update status and return
            user.setRegistrationStatus(User.RegistrationStatus.STEP2_COMPLETED);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return null; // No business profile for individual accounts
        }
        
        // Validate required fields for business accounts
        if (dto.getBusinessLegalName() == null || dto.getBusinessLegalName().trim().isEmpty()) {
            throw new RuntimeException("Business legal name is required for business accounts");
        }
        if (dto.getBusinessType() == null) {
            throw new RuntimeException("Business type is required for business accounts");
        }
        
        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
            .orElse(new BusinessProfile());
        
        profile.setUserId(userId);
        // Keep existing companyName if already set from step 1
        if (profile.getCompanyName() == null) {
            profile.setCompanyName(null); // Will be set if provided in step 1
        }
        profile.setBusinessLegalName(dto.getBusinessLegalName());
        profile.setDba(dto.getDba());
        profile.setBusinessType(dto.getBusinessType());
        profile.setIndustry(dto.getIndustry());
        profile.setNaicsCode(dto.getNaicsCode());
        profile.setEin(dto.getEin());
        profile.setTaxClassification(dto.getTaxClassification());
        profile.setWebsite(dto.getWebsite());
        
        BusinessProfile saved = businessProfileRepository.save(profile);
        
        // Update user registration status
        user.setRegistrationStatus(User.RegistrationStatus.STEP2_COMPLETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return saved;
    }
    
    @Transactional
    public void registerStep3(Long userId, RegistrationStep3Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Step 3 is optional for individual accounts, but if provided, validate
        if (user.getAccountType() == User.AccountType.individual) {
            // For individual accounts, we can skip step 3 or store address differently
            // For now, allow it but don't require business profile
            user.setRegistrationStatus(User.RegistrationStatus.STEP3_COMPLETED);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            return;
        }
        
        // For business accounts, require business profile
        
        BusinessProfile profile = businessProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Business profile not found. Complete step 2 first."));
        
        // Save business address
        Address businessAddress = convertToAddress(dto.getBusinessAddress());
        Address savedBusinessAddress = addressRepository.save(businessAddress);
        profile.setBusinessAddressId(savedBusinessAddress.getAddressId());
        
        // Save mailing address
        if (dto.getMailingAddressSame() != null && !dto.getMailingAddressSame()) {
            if (dto.getMailingAddress() == null) {
                throw new RuntimeException("Mailing address is required when mailing address same is false");
            }
            Address mailingAddress = convertToAddress(dto.getMailingAddress());
            Address savedMailingAddress = addressRepository.save(mailingAddress);
            profile.setMailingAddressId(savedMailingAddress.getAddressId());
        } else {
            profile.setMailingAddressId(savedBusinessAddress.getAddressId());
        }
        
        profile.setMailingAddressSame(dto.getMailingAddressSame());
        businessProfileRepository.save(profile);
        
        // Update user registration status
        user.setRegistrationStatus(User.RegistrationStatus.STEP3_COMPLETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Transactional
    public BeneficialOwner registerStep4(Long userId, RegistrationStep4Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Save owner address if provided
        Long ownerAddressId = null;
        if (dto.getOwnerAddress() != null) {
            Address ownerAddress = convertToAddress(dto.getOwnerAddress());
            Address saved = addressRepository.save(ownerAddress);
            ownerAddressId = saved.getAddressId();
        }
        
        BeneficialOwner owner = new BeneficialOwner();
        owner.setUserId(userId);
        owner.setFullName(dto.getFullName());
        owner.setTitle(dto.getTitle());
        owner.setDateOfBirth(dto.getDateOfBirth());
        owner.setSsnLast4(dto.getSsnLast4());
        owner.setOwnerAddressId(ownerAddressId);
        owner.setPhone(dto.getPhone());
        owner.setEmail(dto.getEmail());
        owner.setOwnershipPercent(dto.getOwnershipPercent());
        owner.setSsnConsent(dto.getSsnConsent());
        
        BeneficialOwner saved = beneficialOwnerRepository.save(owner);
        
        // Update user registration status only if this is the first owner
        // (Allow multiple owners to be added)
        if (user.getRegistrationStatus() == null || 
            user.getRegistrationStatus().ordinal() < User.RegistrationStatus.STEP4_COMPLETED.ordinal()) {
            user.setRegistrationStatus(User.RegistrationStatus.STEP4_COMPLETED);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
        }
        
        return saved;
    }
    
    @Transactional
    public BankAccount registerStep5(Long userId, RegistrationStep5Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Encrypt account number (simple implementation - in production use proper encryption)
        String accountNumberEncrypted = encryptAccountNumber(dto.getAccountNumber());
        String accountNumberLast4 = dto.getAccountNumber().length() >= 4 
            ? dto.getAccountNumber().substring(dto.getAccountNumber().length() - 4) 
            : dto.getAccountNumber();
        
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(userId);
        bankAccount.setAccountHolderName(dto.getAccountHolderName());
        bankAccount.setRoutingNumber(dto.getRoutingNumber());
        bankAccount.setAccountNumberEncrypted(accountNumberEncrypted);
        bankAccount.setAccountNumberLast4(accountNumberLast4);
        bankAccount.setAccountType(dto.getAccountType());
        bankAccount.setVerificationMethod(dto.getVerificationMethod());
        bankAccount.setIsVerified(false);
        
        BankAccount saved = bankAccountRepository.save(bankAccount);
        
        // Update user registration status
        user.setRegistrationStatus(User.RegistrationStatus.STEP5_COMPLETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        return saved;
    }
    
    @Transactional
    public void registerStep6(Long userId, RegistrationStep6Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setTwoFactorEnabled(dto.getTwoFactorEnabled() != null ? dto.getTwoFactorEnabled() : false);
        if (dto.getTwoFactorMethod() != null) {
            user.setTwoFactorMethod(User.TwoFactorMethod.valueOf(dto.getTwoFactorMethod().name()));
        }
        user.setRegistrationStatus(User.RegistrationStatus.STEP6_COMPLETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    @Transactional
    public void registerStep7(Long userId, RegistrationStep7Dto dto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (dto.getAccountingIntegration() != null) {
            user.setAccountingIntegration(dto.getAccountingIntegration().name());
        }
        user.setConsentToShareData(dto.getConsentToShareData() != null ? dto.getConsentToShareData() : false);
        user.setRegistrationStatus(User.RegistrationStatus.COMPLETED);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public RegistrationStatusDto getRegistrationStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        RegistrationStatusDto status = new RegistrationStatusDto();
        status.setUserId(user.getUserId());
        status.setEmail(user.getEmail());
        status.setStatus(convertToStatus(user.getRegistrationStatus()));
        status.setCurrentStep(getCurrentStep(user.getRegistrationStatus()));
        status.setIsCompleted(user.getRegistrationStatus() == User.RegistrationStatus.COMPLETED);
        
        return status;
    }
    
    // Helper methods
    private Address convertToAddress(AddressDto dto) {
        Address address = new Address();
        address.setLine1(dto.getLine1());
        address.setLine2(dto.getLine2());
        address.setCity(dto.getCity());
        address.setStateProvince(dto.getStateProvince());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        return address;
    }
    
    private String encryptAccountNumber(String accountNumber) {
        // Simple base64 encoding - in production use proper encryption (AES-256)
        return java.util.Base64.getEncoder().encodeToString(accountNumber.getBytes());
    }
    
    private RegistrationStatusDto.RegistrationStatus convertToStatus(User.RegistrationStatus status) {
        if (status == null) return RegistrationStatusDto.RegistrationStatus.INCOMPLETE;
        return RegistrationStatusDto.RegistrationStatus.valueOf(status.name());
    }
    
    private Integer getCurrentStep(User.RegistrationStatus status) {
        if (status == null) return 0;
        return switch (status) {
            case STEP1_COMPLETED -> 1;
            case STEP2_COMPLETED -> 2;
            case STEP3_COMPLETED -> 3;
            case STEP4_COMPLETED -> 4;
            case STEP5_COMPLETED -> 5;
            case STEP6_COMPLETED -> 6;
            case STEP7_COMPLETED, COMPLETED -> 7;
            default -> 0;
        };
    }
}

