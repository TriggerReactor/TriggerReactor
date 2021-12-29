package io.github.wysohn.triggerreactor.core.script.warning;

public class StringInterpolationWarning extends Warning {

    private int row;
    private String context;

    /**
     * @param row    the row of the unescaped $
     * @param string the containing string
     */
    public StringInterpolationWarning(int row, String context) {
        this.row = row;
        this.context = context;
    }

    @Override
    public String[] getMessageLines() {
        return new String[]{"Unescaped $ found at line " + row + ": ",
                            context,
                            "to ensure compatibility with the upcoming features of trg 3.0, all $ must be escaped: \\$"};
    }
}
