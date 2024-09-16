package com.github.zomb_676.smart_pot;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SmartPot.MOD_ID)
public class SmartPot {

    public static final String MOD_ID = "smart_pot";
    public static final String MOD_NAME = "SmartPot";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SmartPot() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        EventHandle.registerEvent(modEventBus, MinecraftForge.EVENT_BUS);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::addCommand);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }

    private void addCommand(RegisterCommandsEvent event) {
        if (FMLEnvironment.production) return;

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(SmartPot.MOD_NAME)
                .then(Commands.literal("displayHidden")
                        .then(Commands.argument("display", BoolArgumentType.bool())
                                .executes(c -> {
                                    var state = c.getArgument("display", Boolean.class);
                                    Config.displayHidden.set(state);
                                    return Command.SINGLE_SUCCESS;
                                }))));
    }
}
