package com.numbericsuserportal.ai.action.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaalrActionResult {

    private boolean handled;
    private String reply;

    public static TaalrActionResult notHandled() {
        return new TaalrActionResult(false, null);
    }

    public static TaalrActionResult handled(String reply) {
        return new TaalrActionResult(true, reply);
    }
}
