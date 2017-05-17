package io.github.wysohn.triggerreactor.core.wrapper;

import io.github.wysohn.triggerreactor.tools.ReflectionUtil;

public class Accessor {
    public final Object targetParent;
    public final String targetName;
    public Accessor(Object targetParent, String targetName) {
        this.targetParent = targetParent;
        this.targetName = targetName;
    }

    public Object getTargetParent(){
        return targetParent;
    }

    public Object evaluateTarget() throws NoSuchFieldException, IllegalArgumentException{
        return ReflectionUtil.getField(targetParent, targetName);
    }

    public void setTargetValue(Object value) throws NoSuchFieldException, IllegalArgumentException{
        ReflectionUtil.setField(targetParent, targetName, value);
    }

    @Override
    public String toString() {
        return targetParent+"."+targetName;
    }


}
