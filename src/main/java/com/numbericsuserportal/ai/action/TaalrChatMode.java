package com.numbericsuserportal.ai.action;

/** Dashboard chat routing: automation vs Claude-only guidance. */
public enum TaalrChatMode {
    /** Try automation first (invoice/receipt/list), then Claude for guidance. Default. */
    AUTO,
    /** Claude guidance only — no new automation unless resuming pending work. */
    GUIDE
}
