package com.example.funccommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class FuncCommandMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("func")
                    .then(
                        Commands.argument("function", StringArgumentType.string())
                            .executes(ctx -> execute(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "function"),
                                null
                            ))
                            .then(
                                Commands.argument("nbt", StringArgumentType.greedyString())
                                    .executes(ctx -> execute(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "function"),
                                        StringArgumentType.getString(ctx, "nbt")
                                    ))
                            )
                    )
            );
        });
    }

    private static int execute(CommandSourceStack source, String inputFunction, String nbt) {
        int colon = inputFunction.indexOf(':');
        if (colon == -1) {
            source.sendFailure(Component.literal("Invalid function ID. Use namespace:path"));
            return 0;
        }

        String namespace = inputFunction.substring(0, colon);
        String path = inputFunction.substring(colon + 1);

        // Map to data/<namespace>/functions/func/<path>.mcfunction
        String realFunction = namespace + ":func/" + path;

        MinecraftServer server = source.getServer();
        CommandSourceStack serverSource = server.createCommandSourceStack();

        String command = "function " + realFunction;
        if (nbt != null) {
            command += " " + nbt;
        }

        server.getCommands().performPrefixedCommand(serverSource, command);
        return 1;
    }
}
