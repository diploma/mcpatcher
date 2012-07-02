package com.pclewis.mcpatcher.mod;

import com.pclewis.mcpatcher.*;

import java.io.IOException;

import static javassist.bytecode.Opcode.*;

public class BetterSkies extends Mod {
    public BetterSkies(MinecraftVersion minecraftVersion) {
        name = MCPatcherUtils.BETTER_SKIES;
        author = "MCPatcher";
        description = "Adds support for custom skyboxes.";
        version = "1.0";

        classMods.add(new BaseMod.MinecraftMod().mapTexturePackList());
        classMods.add(new BaseMod.TexturePackListMod(minecraftVersion));
        classMods.add(new BaseMod.TexturePackBaseMod(minecraftVersion));
        classMods.add(new BaseMod.TexturePackDefaultMod());
        classMods.add(new RenderGlobalMod());

        filesToAdd.add(ClassMap.classNameToFilename(MCPatcherUtils.SKY_RENDERER_CLASS));
    }

    private class RenderGlobalMod extends ClassMod {
        RenderGlobalMod() {
            final MethodRef renderSky = new MethodRef(getDeobfClass(), "renderSky", "(F)V");
            final MethodRef getTexture = new MethodRef("RenderEngine", "getTexture", "(Ljava/lang/String;)I");
            final MethodRef bindTexture = new MethodRef("RenderEngine", "bindTexture", "(I)V");
            final MethodRef getCelestialAngle = new MethodRef("World", "getCelestialAngle", "(F)F");
            final MethodRef startDrawingQuads = new MethodRef("Tessellator", "startDrawingQuads", "()V");
            final MethodRef setColorOpaque_I = new MethodRef("Tessellator", "setColorOpaque_I", "(I)V");
            final MethodRef addVertexWithUV = new MethodRef("Tessellator", "addVertexWithUV", "(DDDDD)V");
            final MethodRef draw = new MethodRef("Tessellator", "draw", "()I");
            final MethodRef glBindTexture = new MethodRef(MCPatcherUtils.GL11_CLASS, "glBindTexture", "(II)V");
            final MethodRef glRotatef = new MethodRef(MCPatcherUtils.GL11_CLASS, "glRotatef", "(FFFF)V");
            final MethodRef glCallList = new MethodRef(MCPatcherUtils.GL11_CLASS, "glCallList", "(I)V");
            final FieldRef tessellator = new FieldRef("Tessellator", "instance", "LTessellator;");
            final FieldRef renderEngine = new FieldRef(getDeobfClass(), "renderEngine", "LRenderEngine;");
            final FieldRef mc = new FieldRef(getDeobfClass(), "mc", "LMinecraft;");
            final FieldRef worldProvider = new FieldRef("World", "worldProvider", "LWorldProvider;");
            final FieldRef worldType = new FieldRef("WorldProvider", "worldType", "I");
            final FieldRef worldObj = new FieldRef(getDeobfClass(), "worldObj", "LWorld;");
            final FieldRef glSkyList = new FieldRef(getDeobfClass(), "glSkyList", "I");
            final FieldRef glSkyList2 = new FieldRef(getDeobfClass(), "glSkyList2", "I");
            final FieldRef glStarList = new FieldRef(getDeobfClass(), "glStarList", "I");
            final FieldRef active = new FieldRef(MCPatcherUtils.SKY_RENDERER_CLASS, "active", "Z");

            classSignatures.add(new ConstSignature("smoke"));
            classSignatures.add(new ConstSignature("/environment/clouds.png"));

            classSignatures.add(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // mc.theWorld.worldProvider.worldType == 1
                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        BinaryRegex.any(3),
                        BytecodeMatcher.captureReference(GETFIELD),
                        BytecodeMatcher.captureReference(GETFIELD),
                        ICONST_1,

                        // ...
                        BinaryRegex.any(0, 100),

                        // renderEngine.bindTexture(renderEngine.getTexture("/misc/tunnel.png"));
                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        ALOAD_0,
                        BinaryRegex.backReference(4),
                        push("/misc/tunnel.png"),
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),

                        // Tessellator tessellator = Tessellator.instance;
                        BytecodeMatcher.captureReference(GETSTATIC),
                        BytecodeMatcher.anyASTORE,

                        // ...
                        BinaryRegex.any(0, 1000),

                        // GL11.glRotatef(worldObj.getCelestialAngle(par1) * 360F, 1.0F, 0.0F, 0.0F);
                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        FLOAD_1,
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),
                        push(360.0f),
                        FMUL,
                        push(1.0f),
                        push(0.0f),
                        push(0.0f),
                        reference(INVOKESTATIC, glRotatef)
                    );
                }
            }
                .setMethod(renderSky)
                .addXref(1, mc)
                .addXref(2, worldProvider)
                .addXref(3, worldType)
                .addXref(4, renderEngine)
                .addXref(5, getTexture)
                .addXref(6, bindTexture)
                .addXref(7, tessellator)
                .addXref(8, worldObj)
                .addXref(9, getCelestialAngle)
            );

            classSignatures.add(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        reference(INVOKESTATIC, glCallList),

                        BinaryRegex.nonGreedy(BinaryRegex.any(0, 1000)),

                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        reference(INVOKESTATIC, glCallList),

                        BinaryRegex.nonGreedy(BinaryRegex.any(0, 1000)),

                        ALOAD_0,
                        BytecodeMatcher.captureReference(GETFIELD),
                        reference(INVOKESTATIC, glCallList)
                    );
                }
            }
                .setMethod(renderSky)
                .addXref(1, glSkyList)
                .addXref(2, glStarList)
                .addXref(3, glSkyList2)
            );

            classSignatures.add(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // tessellator.startDrawingQuads();
                        ALOAD_2,
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),

                        // tessellator.setColorOpaque_I(0x...);
                        ALOAD_2,
                        BytecodeMatcher.anyLDC,
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),

                        // tessellator.addVertexWithUV(-100D, -100D, -100D, 0.0D, 0.0D);
                        ALOAD_2,
                        push(-100.0),
                        push(-100.0),
                        push(-100.0),
                        push(0.0),
                        push(0.0),
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),

                        // tessellator.addVertexWithUV(-100D, -100D, 100D, 0.0D, 16D);
                        ALOAD_2,
                        push(-100.0),
                        push(-100.0),
                        push(100.0),
                        push(0.0),
                        push(16.0),
                        BinaryRegex.backReference(3),

                        // tessellator.addVertexWithUV(100D, -100D, 100D, 16D, 16D);
                        ALOAD_2,
                        push(100.0),
                        push(-100.0),
                        push(100.0),
                        push(16.0),
                        push(16.0),
                        BinaryRegex.backReference(3),

                        // tessellator.addVertexWithUV(100D, -100D, -100D, 16D, 0.0D);
                        ALOAD_2,
                        push(100.0),
                        push(-100.0),
                        push(-100.0),
                        push(16.0),
                        push(0.0),
                        BinaryRegex.backReference(3),

                        // tessellator.draw();
                        ALOAD_2,
                        BytecodeMatcher.captureReference(INVOKEVIRTUAL),
                        POP
                    );
                }
            }
                .addXref(1, startDrawingQuads)
                .addXref(2, setColorOpaque_I)
                .addXref(3, addVertexWithUV)
                .addXref(4, draw)
            );

            patches.add(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "setup for sky rendering";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        BinaryRegex.begin()
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        ALOAD_0,
                        reference(GETFIELD, worldObj),
                        ALOAD_0,
                        reference(GETFIELD, renderEngine),
                        FLOAD_1,
                        reference(INVOKESTATIC, new MethodRef(MCPatcherUtils.SKY_RENDERER_CLASS, "setup", "(LWorld;LRenderEngine;F)V"))
                    );
                }
            }.targetMethod(renderSky));

            patches.add(new BytecodePatch.InsertBefore() {
                @Override
                public String getDescription() {
                    return "render sky (part 1)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        FSTORE, BinaryRegex.capture(BinaryRegex.any()),
                        FLOAD, BinaryRegex.backReference(1),
                        push(0.0f),
                        FCMPL,
                        IFLE, BinaryRegex.any(2)
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    return buildCode(
                        reference(INVOKESTATIC, new MethodRef(MCPatcherUtils.SKY_RENDERER_CLASS, "renderSky", "()Z")),
                        POP
                    );
                }
            }.targetMethod(renderSky));

            patches.add(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "render sky (part 2)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        reference(GETFIELD, glSkyList),
                        reference(INVOKESTATIC, glCallList)
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        reference(GETSTATIC, active),
                        IFNE, branch("A"),
                        getMatch(),
                        label("A")
                    );
                }
            }.targetMethod(renderSky));

            patches.add(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "render sky (part 3)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        reference(GETFIELD, glSkyList2),
                        reference(INVOKESTATIC, glCallList)
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        reference(GETSTATIC, active),
                        IFNE, branch("A"),
                        getMatch(),
                        label("A")
                    );
                }
            });

            patches.add(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "render stars";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        reference(GETFIELD, glStarList),
                        reference(INVOKESTATIC, glCallList)
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        reference(INVOKESTATIC, new MethodRef(MCPatcherUtils.SKY_RENDERER_CLASS, "renderStars", "()Z")),
                        IFNE, branch("A"),
                        getMatch(),
                        label("A")
                    );
                }
            }.targetMethod(renderSky));
        }
    }
}