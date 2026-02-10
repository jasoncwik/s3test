package com.datadobi.s3test.s3;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class SkipForQuirksRule implements MethodRule {
    @Override
    public Statement apply(Statement base, FrameworkMethod method, Object target) {
        SkipForQuirks skipForQuirks = method.getAnnotation(SkipForQuirks.class);
        if (skipForQuirks == null) {
            return base;
        }

        if (target instanceof S3TestBase s) {
            for (var q : skipForQuirks.value()) {
                if (s.target.hasQuirk(q)) {
                    return new IgnoreStatement(base, q);
                }
            }
        }

        return base;
    }

    private static class IgnoreStatement extends Statement {
        private final Statement next;
        private final Quirk quirk;

        private IgnoreStatement(Statement next, Quirk quirk) {
            this.next = next;
            this.quirk = quirk;
        }

        @Override
        public void evaluate() throws Throwable {
            assumeFalse("Ignored due to quirk " + quirk, quirk != null);
            this.next.evaluate();
        }
    }
}
