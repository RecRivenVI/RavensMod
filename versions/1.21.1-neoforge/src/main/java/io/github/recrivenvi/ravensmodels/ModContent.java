package io.github.recrivenvi.ravensmodels;

import io.github.recrivenvi.ravensmodels.block.AceOfSpadesBlock;
import io.github.recrivenvi.ravensmodels.block.ChairSeatEntity;
import io.github.recrivenvi.ravensmodels.block.DecorativeModelBlockEntity;
import io.github.recrivenvi.ravensmodels.block.DirectionalPanelBlock;
import io.github.recrivenvi.ravensmodels.block.G3StairBlock;
import io.github.recrivenvi.ravensmodels.block.HorizontalFacingBlock;
import io.github.recrivenvi.ravensmodels.block.ModelBlock;
import io.github.recrivenvi.ravensmodels.block.PlasticChairBlock;
import io.github.recrivenvi.ravensmodels.block.WaterloggedBlock;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModContent {
    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(RavensModels.MOD_ID);
    private static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RavensModels.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RavensModels.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, RavensModels.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, RavensModels.MOD_ID);
    public static final DeferredBlock<Block> MIRROR_BLOCK =
            registerSimpleBlock("mirror_block", SoundType.GLASS);
    public static final DeferredBlock<Block> WHITE_BLOCK =
            registerSimpleBlock("white_block", SoundType.STONE);
    public static final DeferredBlock<WaterloggedBlock> MIRROR_SPHERE =
            registerStaticMeshBlock("mirror_sphere", SoundType.GLASS);
    public static final DeferredBlock<WaterloggedBlock> WHITE_SPHERE =
            registerStaticMeshBlock("white_sphere", SoundType.STONE);
    public static final DeferredBlock<WaterloggedBlock> DODECAHEDRON =
            registerStaticMeshBlock("dodecahedron", SoundType.STONE);
    public static final DeferredBlock<HorizontalFacingBlock> SHATTERED_THRONE =
            registerBlock("shattered_throne", HorizontalFacingBlock::new, SoundType.STONE, true);
    public static final DeferredBlock<G3StairBlock> G3_ROUND_CORNER =
            registerBlock("g3_round_corner", G3StairBlock::new, SoundType.STONE, true);
    public static final DeferredBlock<DirectionalPanelBlock> G3_CONNECTOR =
            registerBlock("g3_connector", DirectionalPanelBlock::new, SoundType.STONE, true);
    public static final DeferredBlock<HorizontalFacingBlock> DAVID_BUST =
            registerBlock("david_bust", HorizontalFacingBlock::new, SoundType.STONE, true);
    public static final DeferredBlock<PlasticChairBlock> PLASTIC_CHAIR =
            registerBlock("plastic_chair", PlasticChairBlock::new, SoundType.WOOD, true);
    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<DecorativeModelBlockEntity>>
            CHAIR_MODEL =
                    BLOCK_ENTITY_TYPES.register(
                            "chair_model",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    (pos, state) ->
                                                            new DecorativeModelBlockEntity(
                                                                    ModContent.CHAIR_MODEL.get(),
                                                                    pos,
                                                                    state),
                                                    PLASTIC_CHAIR.get())
                                            .build(null));
    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<DecorativeModelBlockEntity>>
            DAVID_BUST_MODEL =
                    BLOCK_ENTITY_TYPES.register(
                            "david_bust_model",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    (pos, state) ->
                                                            new DecorativeModelBlockEntity(
                                                                    ModContent.DAVID_BUST_MODEL
                                                                            .get(),
                                                                    pos,
                                                                    state),
                                                    DAVID_BUST.get())
                                            .build(null));
    public static final DeferredHolder<EntityType<?>, EntityType<ChairSeatEntity>> CHAIR_SEAT =
            ENTITY_TYPES.register("chair_seat", ModContent::createChairSeat);
    public static final DeferredBlock<AceOfSpadesBlock> ACE_OF_SPADES = registerAceOfSpades();
    public static final DeferredHolder<
                    BlockEntityType<?>, BlockEntityType<DecorativeModelBlockEntity>>
            MODEL_BLOCK =
                    BLOCK_ENTITY_TYPES.register(
                            "model_block",
                            () ->
                                    BlockEntityType.Builder.of(
                                                    DecorativeModelBlockEntity::create,
                                                    modelBlocks().toArray(Block[]::new))
                                            .build(null));

    public static List<Block> modelBlocks() {
        return BLOCKS.getEntries().stream()
                .map(entry -> (Block) entry.get())
                .filter(block -> block instanceof ModelBlock)
                .toList();
    }

    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_TAB =
            CREATIVE_TABS.register(
                    RavensModels.MOD_ID,
                    () ->
                            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                                    .icon(() -> new ItemStack(SHATTERED_THRONE.get()))
                                    .title(
                                            Component.translatable(
                                                    "itemGroup." + RavensModels.MOD_ID))
                                    .displayItems(
                                            (parameters, output) -> {
                                                output.accept(MIRROR_BLOCK.get());
                                                output.accept(WHITE_BLOCK.get());
                                                output.accept(MIRROR_SPHERE.get());
                                                output.accept(WHITE_SPHERE.get());
                                                output.accept(DODECAHEDRON.get());
                                                output.accept(SHATTERED_THRONE.get());
                                                output.accept(G3_ROUND_CORNER.get());
                                                output.accept(G3_CONNECTOR.get());
                                                output.accept(DAVID_BUST.get());
                                                output.accept(PLASTIC_CHAIR.get());
                                                output.accept(ACE_OF_SPADES.get());
                                            })
                                    .build());

    private ModContent() {}

    public static void initialize(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITY_TYPES.register(bus);
        BLOCK_ENTITY_TYPES.register(bus);
        CREATIVE_TABS.register(bus);
    }

    private static DeferredBlock<Block> registerSimpleBlock(String name, SoundType sound) {
        return registerBlock(name, Block::new, sound, false);
    }

    private static DeferredBlock<AceOfSpadesBlock> registerAceOfSpades() {
        DeferredBlock<AceOfSpadesBlock> block =
                BLOCKS.register(
                        "ace_of_spades",
                        () ->
                                new AceOfSpadesBlock(
                                        BlockBehaviour.Properties.of()
                                                .sound(SoundType.METAL)
                                                .noOcclusion()));
        ITEMS.register(
                "ace_of_spades",
                () -> new BlockItem(block.get(), new Item.Properties().stacksTo(1)));
        return block;
    }

    private static EntityType<ChairSeatEntity> createChairSeat() {
        return EntityType.Builder.<ChairSeatEntity>of(ChairSeatEntity::new, MobCategory.MISC)
                .sized(0.01f, 0.01f)
                .passengerAttachments(Vec3.ZERO)
                .noSummon()
                .fireImmune()
                .clientTrackingRange(8)
                .updateInterval(1)
                .build(id("chair_seat").toString());
    }

    private static DeferredBlock<WaterloggedBlock> registerStaticMeshBlock(
            String name, SoundType sound) {
        return registerBlock(name, WaterloggedBlock::new, sound, true);
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(
            String name,
            Function<BlockBehaviour.Properties, T> factory,
            SoundType sound,
            boolean noOcclusion) {
        DeferredBlock<T> block =
                BLOCKS.register(
                        name,
                        () -> {
                            BlockBehaviour.Properties properties =
                                    BlockBehaviour.Properties.of().sound(sound);
                            if (noOcclusion) properties.noOcclusion();
                            return factory.apply(properties);
                        });
        ITEMS.registerSimpleBlockItem(name, block);
        return block;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(RavensModels.MOD_ID, path);
    }
}
