package com.pclewis.mcpatcher.mod;

import com.pclewis.mcpatcher.*;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.MethodInfo;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static com.pclewis.mcpatcher.BinaryRegex.*;
import static com.pclewis.mcpatcher.BytecodeMatcher.*;
import static javassist.bytecode.Opcode.*;

public class ConnectedTextures extends Mod {
    public ConnectedTextures(MinecraftVersion minecraftVersion) {
        name = MCPatcherUtils.CONNECTED_TEXTURES;
        author = "MCPatcher";
        description = "Connects adjacent blocks of the same type.";
        version = "1.5";

        addDependency(BaseTexturePackMod.NAME);

        configPanel = new ConfigPanel();

        addClassMod(new MinecraftMod());
        addClassMod(new RenderEngineMod());
        addClassMod(new BaseMod.IBlockAccessMod());
        addClassMod(new BlockMod());
        addClassMod(new TessellatorMod(minecraftVersion));
        addClassMod(new RenderBlocksMod());
        addClassMod(new WorldRendererMod());

        addClassFile(MCPatcherUtils.CTM_UTILS_CLASS);
        addClassFile(MCPatcherUtils.CTM_UTILS_CLASS + "$1");
        addClassFile(MCPatcherUtils.SUPER_TESSELLATOR_CLASS);
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS);
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$CTM");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Random1");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Fixed");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Horizontal");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Vertical");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Top");
        addClassFile(MCPatcherUtils.TILE_OVERRIDE_CLASS + "$Repeat");
        addClassFile(MCPatcherUtils.GLASS_PANE_RENDERER_CLASS);
        addClassFile(MCPatcherUtils.RENDER_PASS_API_CLASS);

        getClassMap().addInheritance("Tessellator", MCPatcherUtils.SUPER_TESSELLATOR_CLASS);
    }

    private class ConfigPanel extends ModConfigPanel {
        private JPanel panel;
        private JCheckBox glassCheckBox;
        private JCheckBox glassPaneCheckBox;
        private JCheckBox bookshelfCheckBox;
        private JCheckBox sandstoneCheckBox;
        private JCheckBox standardCheckBox;
        private JCheckBox nonStandardCheckBox;
        private JCheckBox outlineCheckBox;

        public ConfigPanel() {
            glassCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "glass", glassCheckBox.isSelected());
                }
            });

            glassPaneCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "glassPane", glassPaneCheckBox.isSelected());
                }
            });

            bookshelfCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "bookshelf", bookshelfCheckBox.isSelected());
                }
            });

            sandstoneCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "sandstone", sandstoneCheckBox.isSelected());
                }
            });

            standardCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "standard", standardCheckBox.isSelected());
                }
            });

            nonStandardCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "nonStandard", nonStandardCheckBox.isSelected());
                }
            });

            outlineCheckBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    MCPatcherUtils.set(MCPatcherUtils.CONNECTED_TEXTURES, "outline", outlineCheckBox.isSelected());
                }
            });
        }

        @Override
        public JPanel getPanel() {
            return panel;
        }

        @Override
        public void load() {
            glassCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "glass", true));
            glassPaneCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "glassPane", true));
            bookshelfCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "bookshelf", true));
            sandstoneCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "sandstone", true));
            standardCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "standard", true));
            nonStandardCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "nonStandard", true));
            outlineCheckBox.setSelected(MCPatcherUtils.getBoolean(MCPatcherUtils.CONNECTED_TEXTURES, "outline", false));
        }

        @Override
        public void save() {
        }
    }

    private class MinecraftMod extends BaseMod.MinecraftMod {
        MinecraftMod() {
            final FieldRef renderEngine = new FieldRef(getDeobfClass(), "renderEngine", "LRenderEngine;");

            addMemberMapper(new FieldMapper(renderEngine));
        }
    }

    private class RenderEngineMod extends ClassMod {
        RenderEngineMod() {
            final MethodRef glTexSubImage2D = new MethodRef(MCPatcherUtils.GL11_CLASS, "glTexSubImage2D", "(IIIIIIIILjava/nio/ByteBuffer;)V");
            final MethodRef allocateAndSetupTexture = new MethodRef(getDeobfClass(), "allocateAndSetupTexture", "(Ljava/awt/image/BufferedImage;)I");
            final MethodRef getTexture = new MethodRef(getDeobfClass(), "getTexture", "(Ljava/lang/String;)I");

            addClassSignature(new ConstSignature("%clamp%"));
            addClassSignature(new ConstSignature("%blur%"));
            addClassSignature(new ConstSignature(glTexSubImage2D));

            addMemberMapper(new MethodMapper(getTexture)
                .accessFlag(AccessFlag.PUBLIC, true)
                .accessFlag(AccessFlag.STATIC, false)
            );

            addMemberMapper(new MethodMapper(allocateAndSetupTexture)
                .accessFlag(AccessFlag.PUBLIC, true)
                .accessFlag(AccessFlag.STATIC, false)
            );
        }
    }

    private class BlockMod extends BaseMod.BlockMod {
        BlockMod() {
            final FieldRef blockMaterial = new FieldRef(getDeobfClass(), "blockMaterial", "LMaterial;");
            final MethodRef getBlockTexture = new MethodRef(getDeobfClass(), "getBlockTexture", "(LIBlockAccess;IIII)I");
            final InterfaceMethodRef getBlockMetadata = new InterfaceMethodRef("IBlockAccess", "getBlockMetadata", "(III)I");
            final MethodRef getBlockTextureFromSideAndMetadata = new MethodRef(getDeobfClass(), "getBlockTextureFromSideAndMetadata", "(II)I");

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        begin(),
                        ALOAD_0,
                        ILOAD, 5,
                        ALOAD_1,
                        ILOAD_2,
                        ILOAD_3,
                        ILOAD, 4,
                        captureReference(INVOKEINTERFACE),
                        captureReference(INVOKEVIRTUAL),
                        IRETURN,
                        end()
                    );
                }
            }
                .setMethod(getBlockTexture)
                .addXref(1, getBlockMetadata)
                .addXref(2, getBlockTextureFromSideAndMetadata)
            );

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        push(" is already occupied by ")
                    );
                }
            }
                .matchConstructorOnly(true)
                .setMethod(new MethodRef(getDeobfClass(), "<init>", "(ILMaterial;)V"))
            );

            addMemberMapper(new FieldMapper(blockMaterial).accessFlag(AccessFlag.PUBLIC, true));
        }
    }

    private class TessellatorMod extends BaseMod.TessellatorMod {
        TessellatorMod(MinecraftVersion minecraftVersion) {
            super(minecraftVersion);

            final MethodRef constructor = new MethodRef(getDeobfClass(), "<init>", "(I)V");
            final MethodRef constructor0 = new MethodRef(getDeobfClass(), "<init>", "()V");
            final MethodRef reset = new MethodRef(getDeobfClass(), "reset", "()V");
            final FieldRef isDrawing = new FieldRef(getDeobfClass(), "isDrawing", "Z");
            final FieldRef drawMode = new FieldRef(getDeobfClass(), "drawMode", "I");
            final FieldRef texture = new FieldRef(getDeobfClass(), "texture", "I");
            final FieldRef bufferSize = new FieldRef(getDeobfClass(), "bufferSize", "I");
            final FieldRef addedVertices = new FieldRef(getDeobfClass(), "addedVertices", "I");
            final FieldRef vertexCount = new FieldRef(getDeobfClass(), "vertexCount", "I");
            final FieldRef rawBufferIndex = new FieldRef(getDeobfClass(), "rawBufferIndex", "I");

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        captureReference(GETFIELD),
                        push(4),
                        IREM,

                        any(0, 1000),

                        ALOAD_0,
                        DUP,
                        captureReference(GETFIELD),
                        ICONST_1,
                        IADD,
                        anyReference(PUTFIELD),

                        ALOAD_0,
                        DUP,
                        captureReference(GETFIELD),
                        push(8),
                        IADD,
                        anyReference(PUTFIELD)
                    );
                }
            }
                .setMethod(addVertex)
                .addXref(1, addedVertices)
                .addXref(2, vertexCount)
                .addXref(3, rawBufferIndex)
            );

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        begin(),
                        ALOAD_0,
                        captureReference(GETFIELD),

                        any(0, 50),

                        push("Already tesselating!"),
                        any(0, 100),

                        ALOAD_0,
                        captureReference(INVOKESPECIAL),

                        ALOAD_0,
                        ILOAD_1,
                        captureReference(PUTFIELD)
                    );
                }
            }
                .setMethod(startDrawing)
                .addXref(1, isDrawing)
                .addXref(2, reset)
                .addXref(3, drawMode)
            );

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        begin(),
                        ALOAD_0,
                        push(7),
                        captureReference(INVOKEVIRTUAL),
                        RETURN,
                        end()
                    );
                }
            }
                .setMethod(startDrawingQuads)
                .addXref(1, startDrawing)
            );

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ALOAD_0,
                        ILOAD_1,
                        captureReference(PUTFIELD)
                    );
                }
            }
                .matchConstructorOnly(true)
                .addXref(1, bufferSize)
            );

            addMemberMapper(new FieldMapper(instance).accessFlag(AccessFlag.STATIC, true));

            for (JavaRef ref : new JavaRef[]{constructor, startDrawing, isDrawing, drawMode, draw, reset, bufferSize,
                addedVertices, vertexCount, rawBufferIndex}) {
                addPatch(new MakeMemberPublicPatch(ref));
            }

            addPatch(new AddFieldPatch(texture));

            addPatch(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "replace tessellator instance";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        reference(NEW, new ClassRef("Tessellator")),
                        DUP,
                        capture(optional(anyLDC)),
                        capture(or(
                            build(reference(INVOKESPECIAL, constructor)),
                            build(reference(INVOKESPECIAL, constructor0))
                        ))
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    boolean isForge = getCaptureGroup(1).length == 0;
                    return buildCode(
                        reference(NEW, new ClassRef(MCPatcherUtils.SUPER_TESSELLATOR_CLASS)),
                        DUP,
                        getCaptureGroup(1),
                        reference(INVOKESPECIAL, new MethodRef(MCPatcherUtils.SUPER_TESSELLATOR_CLASS, "<init>", isForge ? "()V" : "(I)V"))
                    );
                }
            }.matchStaticInitializerOnly(true));

            addPatch(new BytecodePatch.InsertBefore() {
                @Override
                public String getDescription() {
                    return "initialize texture field to -1";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        RETURN
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    return buildCode(
                        ALOAD_0,
                        push(-1),
                        reference(PUTFIELD, texture)
                    );
                }
            }.matchConstructorOnly(true));

            addPatch(new BytecodePatch.InsertBefore() {
                @Override
                public String getDescription() {
                    return "bind texture before drawing";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        or(
                            build(
                                ALOAD_0,
                                anyReference(GETFIELD)
                            ),
                            anyReference(GETSTATIC)
                        ),
                        reference(INVOKEVIRTUAL, new MethodRef("java/nio/IntBuffer", "clear", "()Ljava/nio/Buffer;")),
                        POP
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    return buildCode(
                        // if (texture >= 0) {
                        ALOAD_0,
                        reference(GETFIELD, texture),
                        IFLT, branch("A"),

                        // GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
                        push(3553), // GL11.GL_TEXTURE_2D
                        ALOAD_0,
                        reference(GETFIELD, texture),
                        reference(INVOKESTATIC, new MethodRef(MCPatcherUtils.GL11_CLASS, "glBindTexture", "(II)V")),

                        // }
                        label("A")
                    );
                }
            }.targetMethod(draw));

            addPatch(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "fix references to reset method";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        reference(INVOKESPECIAL, reset)
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        reference(INVOKEVIRTUAL, reset)
                    );
                }
            });
        }
    }

    private class RenderBlocksMod extends BaseMod.RenderBlocksMod {
        private final MethodRef[] faceMethods = new MethodRef[6];
        private final FieldRef overrideBlockTexture = new FieldRef(getDeobfClass(), "overrideBlockTexture", "I");
        private final FieldRef blockAccess = new FieldRef(getDeobfClass(), "blockAccess", "LIBlockAccess;");
        private final FieldRef instance = new FieldRef("Tessellator", "instance", "LTessellator;");
        private final MethodRef renderStandardBlock = new MethodRef(getDeobfClass(), "renderStandardBlock", "(LBlock;III)Z");
        private final MethodRef drawCrossedSquares;
        private final MethodRef renderBlockPane = new MethodRef(getDeobfClass(), "renderBlockPane", "(LBlockPane;III)Z");
        private final MethodRef addVertexWithUV = new MethodRef("Tessellator", "addVertexWithUV", "(DDDDD)V");
        private final MethodRef setup = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "setup", "(LRenderBlocks;LBlock;IIIII)Z");
        private final MethodRef setupNoFace = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "setup", "(LRenderBlocks;LBlock;IIII)Z");
        private final MethodRef reset = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "reset", "()V");
        private final FieldRef newTextureIndex = new FieldRef(MCPatcherUtils.CTM_UTILS_CLASS, "newTextureIndex", "I");
        private final FieldRef newTessellator = new FieldRef(MCPatcherUtils.CTM_UTILS_CLASS, "newTessellator", "LTessellator;");
        private final MethodRef skipDefaultRendering = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "skipDefaultRendering", "(LBlock;)Z");

        RenderBlocksMod() {
            if (getMinecraftVersion().compareTo("12w34a") >= 0) {
                drawCrossedSquares = new MethodRef(getDeobfClass(), "drawCrossedSquares", "(LBlock;IDDDF)V");
            } else {
                drawCrossedSquares = new MethodRef(getDeobfClass(), "drawCrossedSquares", "(LBlock;IDDD)V");
            }

            setupBlockFace(0, "Bottom");
            setupBlockFace(1, "Top");
            setupBlockFace(2, "North");
            setupBlockFace(3, "South");
            setupBlockFace(4, "West");
            setupBlockFace(5, "East");

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // renderStandardBlock(par1BlockBrewingStand, par2, par3, par4);
                        ALOAD_0,
                        ALOAD_1,
                        ILOAD_2,
                        ILOAD_3,
                        ILOAD, 4,
                        captureReference(INVOKEVIRTUAL),
                        POP,

                        // overrideBlockTexture = 156;
                        ALOAD_0,
                        push(156),
                        captureReference(PUTFIELD)
                    );
                }
            }
                .addXref(1, renderStandardBlock)
                .addXref(2, overrideBlockTexture)
            );

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        ILOAD, 5,
                        push(18),
                        IF_ICMPNE_or_IF_ICMPEQ, any(2),

                        ALOAD_0,
                        ALOAD_1,
                        anyReference(CHECKCAST),
                        ILOAD_2,
                        ILOAD_3,
                        ILOAD, 4,
                        captureReference(INVOKEVIRTUAL),
                        IRETURN
                    );
                }
            }
                .addXref(1, renderBlockPane)
            );

            addMemberMapper(new FieldMapper(blockAccess));
            addMemberMapper(new MethodMapper(faceMethods));
            addMemberMapper(new MethodMapper(drawCrossedSquares));

            addPatch(new BytecodePatch.InsertBefore() {
                private int tessellatorRegister;
                private JavaRef renderBlockPaneMapped;

                {
                    addPreMatchSignature(new BytecodeSignature() {
                        @Override
                        public String getMatchExpression() {
                            return buildExpression(
                                reference(GETSTATIC, instance),
                                ASTORE, capture(any())
                            );
                        }

                        @Override
                        public boolean afterMatch() {
                            tessellatorRegister = getCaptureGroup(1)[0] & 0xff;
                            return true;
                        }
                    });
                }

                @Override
                public String getDescription() {
                    return "override texture (other blocks)";
                }

                @Override
                public boolean filterMethod() {
                    if (renderBlockPaneMapped == null) {
                        renderBlockPaneMapped = map(renderBlockPane);
                    }
                    MethodInfo methodInfo = getMethodInfo();
                    return methodInfo.getDescriptor().matches("^\\(L[a-z]+;III.*") &&
                        !(methodInfo.getDescriptor().equals(renderBlockPaneMapped.getType()) &&
                            methodInfo.getName().equals(renderBlockPaneMapped.getName()));
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // j = (i & 0x0f) << 4;
                        ILOAD, capture(any()),
                        push(0x0f),
                        IAND,
                        push(4),
                        ISHL,
                        ISTORE, any(),

                        // k = (i & 0xf0);
                        ILOAD, backReference(1),
                        push(0xf0),
                        IAND,
                        ISTORE, any()
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    Logger.log(Logger.LOG_BYTECODE, "tessellator register %d", tessellatorRegister);
                    return buildCode(
                        // if (overrideBlockTexture < 0
                        ALOAD_0,
                        reference(GETFIELD, overrideBlockTexture),
                        IFGE, branch("A"),

                        // && CTMUtils.setup(this, block, i, j, k, texture)) {
                        ALOAD_0,
                        ALOAD_1,
                        ILOAD_2,
                        ILOAD_3,
                        ILOAD, 4,
                        ILOAD, getCaptureGroup(1),
                        reference(INVOKESTATIC, setupNoFace),
                        IFEQ, branch("A"),

                        // texture = CTMUtils.newTextureIndex;
                        reference(GETSTATIC, newTextureIndex),
                        ISTORE, getCaptureGroup(1),

                        // tessellator = CTMUtils.newTessellator;
                        reference(GETSTATIC, newTessellator),
                        ASTORE, tessellatorRegister,
                        GOTO, branch("B"),

                        // } else if (CTMUtils.skipDefaultRendering(block)) {
                        label("A"),
                        ALOAD_1,
                        reference(INVOKESTATIC, skipDefaultRendering),
                        IFEQ, branch("B"),

                        // return false;
                        push(0),
                        IRETURN,

                        // }
                        label("B")
                    );
                }
            });

            addPatch(new BytecodePatch.InsertAfter() {
                @Override
                public String getDescription() {
                    return "override texture (crossed squares)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // tessellator = Tessellator.instance;
                        reference(GETSTATIC, instance),
                        ASTORE, capture(any()),

                        // i = par1Block.getBlockTextureFromSideAndMetadata(0, par2);
                        ALOAD_1,
                        ICONST_0,
                        ILOAD_2,
                        anyReference(INVOKEVIRTUAL),
                        ISTORE, capture(any())
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    return buildCode(
                        // tessellator = Tessellator.instance;
                        // if (overrideBlockTexture < 0
                        ALOAD_0,
                        reference(GETFIELD, overrideBlockTexture),
                        IFGE, branch("A"),

                        // && CTMUtils.setup(this, block, (int) x, (int) y, (int) z, texture)) {
                        ALOAD_0,
                        ALOAD_1,
                        DLOAD_3,
                        D2I,
                        DLOAD, 5,
                        D2I,
                        DLOAD, 7,
                        D2I,
                        ILOAD, getCaptureGroup(2),
                        reference(INVOKESTATIC, setupNoFace),
                        IFEQ, branch("A"),

                        // texture = CTMUtils.newTextureIndex;
                        reference(GETSTATIC, newTextureIndex),
                        ISTORE, getCaptureGroup(2),

                        // tessellator = CTMUtils.newTessellator;
                        reference(GETSTATIC, newTessellator),
                        ASTORE, getCaptureGroup(1),
                        GOTO, branch("B"),

                        // } else if (CTMUtils.skipDefaultRendering(block)) {
                        label("A"),
                        ALOAD_1,
                        reference(INVOKESTATIC, skipDefaultRendering),
                        IFEQ, branch("B"),

                        // return;
                        RETURN,

                        // }
                        label("B")
                    );
                }
            }.targetMethod(drawCrossedSquares));

            addPatch(new BytecodePatch.InsertAfter() {
                @Override
                public String getDescription() {
                    return "override texture (glass pane)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        // connectEast = par1BlockPane.canThisPaneConnectToThisBlockID(this.blockAccess.getBlockId(i + 1, j, k));
                        ALOAD_1,
                        ALOAD_0,
                        reference(GETFIELD, blockAccess),
                        ILOAD_2,
                        push(1),
                        IADD,
                        ILOAD_3,
                        ILOAD, 4,
                        anyReference(INVOKEINTERFACE),
                        anyReference(INVOKEVIRTUAL),
                        ISTORE, capture(any())
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    int reg = getCaptureGroup(1)[0] & 0xff;
                    Logger.log(Logger.LOG_BYTECODE, "glass side connect flags (%d %d %d %d)",
                        reg - 3, reg - 2, reg - 1, reg
                    );
                    return buildCode(
                        // GlassPaneRenderer.render(renderBlocks, overrideBlockTexture, blockPane, i, j, k, connectNorth, ...);
                        ALOAD_0,
                        ALOAD_0,
                        reference(GETFIELD, overrideBlockTexture),
                        ALOAD_1,
                        ILOAD_2,
                        ILOAD_3,
                        ILOAD, 4,
                        ILOAD, reg - 3,
                        ILOAD, reg - 2,
                        ILOAD, reg - 1,
                        ILOAD, reg,
                        reference(INVOKESTATIC, new MethodRef(MCPatcherUtils.GLASS_PANE_RENDERER_CLASS, "render", "(LRenderBlocks;ILBlock;IIIZZZZ)V"))
                    );
                }
            }.targetMethod(renderBlockPane));

            addPatch(new BytecodePatch() {
                private int[] sideUVRegisters;

                {
                    addPreMatchSignature(new BytecodeSignature() {
                        @Override
                        public String getMatchExpression() {
                            return buildExpression(
                                // sideU0 = (sideU + 7) / 256.0f;
                                anyILOAD,
                                push(7),
                                IADD,
                                I2F,
                                push(256.0f),
                                FDIV,
                                F2D,
                                DSTORE, capture(any())
                            );
                        }

                        @Override
                        public boolean afterMatch() {
                            int reg = getCaptureGroup(1)[0] & 0xff;
                            sideUVRegisters = new int[]{reg, reg + 2, reg + 4, reg + 6, reg + 8};
                            Logger.log(Logger.LOG_CONST, "glass side texture uv registers (%d %d %d %d %d)",
                                reg, reg + 2, reg + 4, reg + 6, reg + 8
                            );
                            return true;
                        }
                    });
                }

                @Override
                public String getDescription() {
                    return "disable default rendering (glass pane faces)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(repeat(build(
                        ALOAD, any(),
                        nonGreedy(any(0, 15)),
                        DLOAD, subset(sideUVRegisters, false),
                        DLOAD, subset(sideUVRegisters, false),
                        reference(INVOKEVIRTUAL, addVertexWithUV)
                    ), 8));
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        // if (!GlassPaneRenderer.active) {
                        reference(GETSTATIC, new FieldRef(MCPatcherUtils.GLASS_PANE_RENDERER_CLASS, "active", "Z")),
                        IFNE, branch("A"),

                        // ...
                        getMatch(),

                        // }
                        label("A")
                    );
                }
            }.targetMethod(renderBlockPane));
        }

        private void setupBlockFace(final int face, final String direction) {
            faceMethods[face] = new MethodRef(getDeobfClass(), "render" + direction + "Face", "(LBlock;DDDI)V");

            addPatch(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "override texture (" + direction.toLowerCase() + " face)";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        capture(build(
                            // tessellator = Tessellator.instance;
                            reference(GETSTATIC, instance),
                            ASTORE, capture(any()),

                            // if (overrideBlockTexture >= 0) {
                            ALOAD_0,
                            reference(GETFIELD, overrideBlockTexture)
                        )),
                        IFLT, any(2),

                        // texture = overrideBlockTexture;
                        capture(build(
                            ALOAD_0,
                            reference(GETFIELD, overrideBlockTexture),
                            ISTORE, capture(any())
                        ))
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        // tessellator = Tessellator.instance;
                        // if (overrideBlockTexture >= 0) {
                        getCaptureGroup(1),
                        IFLT, branch("A"),

                        // texture = overrideBlockTexture;
                        getCaptureGroup(3),

                        // CTMUtils.reset();
                        reference(INVOKESTATIC, reset),
                        GOTO, branch("C"),

                        // } else if (CTMUtils.setup(this, block, (int) x, (int) y, (int) z, face, texture)) {
                        label("A"),
                        ALOAD_0,
                        ALOAD_1,
                        DLOAD_2,
                        D2I,
                        DLOAD, 4,
                        D2I,
                        DLOAD, 6,
                        D2I,
                        push(face),
                        ILOAD, getCaptureGroup(4),
                        reference(INVOKESTATIC, setup),
                        IFEQ, branch("B"),

                        // texture = CTMUtils.newTextureIndex;
                        reference(GETSTATIC, newTextureIndex),
                        ISTORE, getCaptureGroup(4),

                        // tessellator = CTMUtils.newTessellator;
                        reference(GETSTATIC, newTessellator),
                        ASTORE, getCaptureGroup(2),
                        GOTO, branch("C"),

                        // } else if (CTMUtils.skipDefaultRendering(block)) {
                        label("B"),
                        ALOAD_1,
                        reference(INVOKESTATIC, skipDefaultRendering),
                        IFEQ, branch("C"),

                        // return;
                        RETURN,

                        // }
                        label("C")
                    );
                }
            }.targetMethod(faceMethods[face]));
        }
    }

    private class WorldRendererMod extends ClassMod {
        WorldRendererMod() {
            final MethodRef updateRenderer = new MethodRef(getDeobfClass(), "updateRenderer", "()V");
            final MethodRef start = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "start", "()V");
            final MethodRef finish = new MethodRef(MCPatcherUtils.CTM_UTILS_CLASS, "finish", "()V");

            addClassSignature(new ConstSignature(new MethodRef(MCPatcherUtils.GL11_CLASS, "glNewList", "(II)V")));

            addClassSignature(new BytecodeSignature() {
                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        push(1.000001F)
                    );
                }
            }.setMethod(updateRenderer));

            addPatch(new BytecodePatch() {
                @Override
                public String getDescription() {
                    return "pre render world";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        begin()
                    );
                }

                @Override
                public byte[] getReplacementBytes() throws IOException {
                    return buildCode(
                        reference(INVOKESTATIC, start)
                    );
                }
            }.targetMethod(updateRenderer));

            addPatch(new BytecodePatch.InsertBefore() {
                @Override
                public String getDescription() {
                    return "post render world";
                }

                @Override
                public String getMatchExpression() {
                    return buildExpression(
                        RETURN
                    );
                }

                @Override
                public byte[] getInsertBytes() throws IOException {
                    return buildCode(
                        reference(INVOKESTATIC, finish)
                    );
                }
            }.targetMethod(updateRenderer));
        }
    }
}
