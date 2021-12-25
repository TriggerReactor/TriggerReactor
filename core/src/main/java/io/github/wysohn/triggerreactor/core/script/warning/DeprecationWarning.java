package io.github.wysohn.triggerreactor.core.script.warning;

public class DeprecationWarning extends Warning {
    private int row;
    private String value;
    private String context;

    public DeprecationWarning(int row, String value, String context) {
        this.row = row;
        this.value = value;
        this.context = context;
    }

    @Override
    public String[] getMessageLines() {
        return new String[]{"Deprecated token found at line " + row + ": ",
                            context,
                            value + " is deprecated and may be removed in a future release"};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeprecationWarning that = (DeprecationWarning) o;

        if (row != that.row) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        return context != null ? context.equals(that.context) : that.context == null;
    }
}
