package org.incava.diffj;

import java.io.StringWriter;
import org.incava.analysis.DetailedReport;
import org.incava.analysis.Report;
import org.incava.ijdk.util.ANSI;

public class TestOutputContextHighlight extends AbstractTestOutputContext {
    public TestOutputContextHighlight(String name) {
        super(name);
    }

    public boolean highlight() {
        return true;
    }

    public String adorn(String str, boolean isDelete) {
        StringBuilder sb = new StringBuilder();
        sb.append(isDelete ? ANSI.RED : ANSI.YELLOW);
        sb.append(str);
        sb.append(ANSI.RESET);

        return sb.toString();
    }
}
