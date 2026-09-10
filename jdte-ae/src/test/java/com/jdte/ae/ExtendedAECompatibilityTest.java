package com.jdte.ae;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAECompatibilityTest {
    private static final String CRYSTAL_ASSEMBLER =
            "com.glodblock.github.extendedae.common.tileentities.TileCrystalAssembler";
    private static final String TICKING_REQUEST_DESCRIPTOR = Type.getMethodDescriptor(
            Type.getType(TickRateModulation.class),
            Type.getType(IGridNode.class),
            Type.INT_TYPE);

    @Test
    void extendedAeAssemblersParticipateInGridTicking() throws Exception {
        assertGridTickable(
                "com.glodblock.github.extendedae.common.tileentities.TileExMolecularAssembler");
        assertGridTickable(CRYSTAL_ASSEMBLER);
        assertGridTickable(
                "com.glodblock.github.extendedae.common.tileentities.matrix."
                        + "TileAssemblerMatrixCrafter");
    }

    @Test
    void crystalAssemblerIgnoresTheElapsedTickArgument() throws IOException {
        String resourceName = CRYSTAL_ASSEMBLER.replace('.', '/') + ".class";
        AtomicBoolean methodFound = new AtomicBoolean();
        AtomicBoolean elapsedTicksUsed = new AtomicBoolean();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(input, "ExtendedAE must be present on the test runtime classpath");
            new ClassReader(input).accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    if (!name.equals("tickingRequest")
                            || !descriptor.equals(TICKING_REQUEST_DESCRIPTOR)) {
                        return null;
                    }
                    methodFound.set(true);
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitVarInsn(int opcode, int variable) {
                            if (variable == 2) {
                                elapsedTicksUsed.set(true);
                            }
                        }

                        @Override
                        public void visitIincInsn(int variable, int increment) {
                            if (variable == 2) {
                                elapsedTicksUsed.set(true);
                            }
                        }
                    };
                }
            }, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        }

        assertTrue(methodFound.get(), "ExtendedAE crystal assembler tickingRequest must exist");
        assertFalse(elapsedTicksUsed.get(),
                "ExtendedAE crystal assembler must ignore ticksSinceLastCall in this runtime");
    }

    private static void assertGridTickable(String className) throws ClassNotFoundException {
        Class<?> type = Class.forName(
                className, false, ExtendedAECompatibilityTest.class.getClassLoader());
        assertTrue(IGridTickable.class.isAssignableFrom(type),
                () -> className + " must participate in AE2 Grid ticking");
    }
}
