package io.github.wysohn.triggerreactor.manager.trigger.share.api.vault;

@SuppressWarnings("serial")
public class VaultSupportException extends RuntimeException {
    public VaultSupportException(String type) {
        super(type + " is not hooked!");
    }
}