package io.github.wysohn.triggerreactor.core.config.source;

import java.util.Collection;
import java.util.LinkedList;

public class TypeValidatorChain implements ITypeValidator {
    protected final Collection<ITypeValidator> validators = new LinkedList<>();

    @Override
    public boolean isSerializable(Object obj) {
        return validators.stream().anyMatch(iTypeValidator -> iTypeValidator.isSerializable(obj));
    }

    public static class Builder {
        private final TypeValidatorChain chain = new TypeValidatorChain();

        public Builder() {
            this(ITypeValidator.DEFAULT);
        }

        public Builder(ITypeValidator head) {
            chain.validators.add(head);
        }

        public Builder addChain(ITypeValidator typeValidator) {
            chain.validators.add(typeValidator);
            return this;
        }

        public ITypeValidator build() {
            return chain;
        }
    }
}
