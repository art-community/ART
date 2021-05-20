package io.art.graal.feature;

import com.oracle.svm.core.annotate.*;
import com.oracle.svm.core.jni.*;
import org.graalvm.nativeimage.hosted.*;
import org.rocksdb.*;
import static java.lang.Class.*;

@AutomaticFeature
public class GraalRocksDbFeature implements Feature {
    private final static String ROCKS_DB_MODULE = "io.art.rocks.db.module.RocksDbModule";

    @Override
    public void beforeAnalysis(Feature.BeforeAnalysisAccess access) {
        try {
            forName(ROCKS_DB_MODULE);
            JNIRuntimeAccess.register(RocksDBException.class);
            JNIRuntimeAccess.register(RocksDBException.class.getConstructors());
            JNIRuntimeAccess.register(Status.class);
            JNIRuntimeAccess.register(Status.class.getDeclaredConstructors());
        } catch (ClassNotFoundException ignored) {
        }
    }
}
