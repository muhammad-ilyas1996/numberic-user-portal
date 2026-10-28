package com.numbericsuserportal.ai.service;

import com.numbericsuserportal.ai.action.dto.TaalrActionResult;
import com.numbericsuserportal.federaltax.domain.FederalTaxReturnType;
import com.numbericsuserportal.federaltax.dto.CreateFederalTaxCaseRequest;
import com.numbericsuserportal.federaltax.dto.FederalTaxCaseDto;
import com.numbericsuserportal.federaltax.service.FederalTaxReturnService;
import com.numbericsuserportal.usermanagement.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TAALR entry into Federal Tax Return intake (web APIs do the heavy lifting).
 */
@Service
public class TaalrFederalTaxReturnHandler {

    @Autowired
    private FederalTaxReturnService federalTaxReturnService;

    public TaalrActionResult start(User user, String message) {
        if (user == null || user.getUserId() == null) {
            return TaalrActionResult.handled(
                    "To start federal tax intake, log in to Numbrics (or link this WhatsApp number), "
                            + "then say \"start federal tax return\" again.");
        }
        CreateFederalTaxCaseRequest req = new CreateFederalTaxCaseRequest();
        String lower = message != null ? message.toLowerCase() : "";
        if (lower.contains("schedule c") || lower.contains("self-employ") || lower.contains("sch c")) {
            req.setReturnType(FederalTaxReturnType.SCHEDULE_C);
        } else {
            req.setReturnType(FederalTaxReturnType.FORM_1040);
        }
        req.setTaxpayerConsent(true);
        FederalTaxCaseDto created = federalTaxReturnService.createCase(user, req);
        return TaalrActionResult.handled(
                "TAALR empowered by Numbrics — Federal tax intake started.\n\n"
                        + "Case: " + created.getPublicCaseId() + "\n"
                        + "Type: " + created.getReturnType() + "\n"
                        + "Status: " + created.getClientStatus() + "\n"
                        + "Tax year: " + created.getTaxYear() + "\n\n"
                        + "Next steps:\n"
                        + "1) Upload W-2 / 1099 / ID / Schedule C docs in the Tax Documents area "
                        + "(or WhatsApp with a tax document photo).\n"
                        + "2) Complete the adaptive organizer (manual entry OK if a doc is missing).\n"
                        + "3) Choose File With Us or Export to My Tax Pro.\n\n"
                        + "Missing items right now: "
                        + (created.getMissingItems() == null || created.getMissingItems().isEmpty()
                        ? "none" : String.join(", ", created.getMissingItems()))
                        + "\n\nThis prepares a package for a tax professional — it is not an e-filed return.");
    }
}
