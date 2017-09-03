package io.github.wysohn.triggerreactor.core.bridge.event;

public interface IPlayerPermissionCheckEvent extends IPlayerEvent{

    public String getRequestedPermission();

    public boolean isAllowed();

    public void setAllowed(boolean allowed);

}