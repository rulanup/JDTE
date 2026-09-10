package com.jdte.ae;

import com.jdte.common.acceleration.ExternalTimeAccelerationBackends;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(JDTEAE.MOD_ID)
public final class JDTEAE {
    public static final String MOD_ID = "jdte_ae";
    private static final Logger LOGGER = LoggerFactory.getLogger(JDTEAE.class);

    public JDTEAE() {
        ExternalTimeAccelerationBackends.register(AeGridAccelerationBackend.INSTANCE);
        LOGGER.info("JDT Extras AE Acceleration loaded; registered AE2 full-grid time acceleration backend");
    }
}
