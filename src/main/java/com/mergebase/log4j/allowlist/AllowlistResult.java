package com.mergebase.log4j.allowlist;

public final class AllowlistResult {

    /**
     * allowed == true: file is contained in an allowlist
     * allowed == false: file was not found in any allowlist, doesn't mean it's evil.
     */
    private final boolean allowed;
    private final String allowlist;

    public static AllowlistResult notFound() {
        return new AllowlistResult(false, null);
    }

    public AllowlistResult(boolean allowed, String allowlist) {
        this.allowed = allowed;
        this.allowlist = allowlist;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public String getAllowlist() {
        return allowlist;
    }
}
