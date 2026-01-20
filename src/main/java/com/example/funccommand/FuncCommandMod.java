package com.example.funccommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class FuncCommandMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("func")
                    .requires(source -> true)
                    .then(
                        CommandManager.argument("function", StringArgumentType.string())
                            .executes(ctx -> execute(
                                ctx.getSource(),
                                StringArgumentType.getString(ctx, "function"),
                                null
                            ))
                            .then(
                                CommandManager.argument("nbt", StringArgumentType.greedyString())
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

    private static int execute(ServerCommandSource source, String inputFunction, String nbt) {
        int colon = inputFunction.indexOf(':');
        if (colon == -1) {
            source.sendError(Text.literal("Invalid function ID. Use namespace:path"));
            return 0;
        }

        String namespace = inputFunction.substring(0, colon);
        String path = inputFunction.substring(colon + 1);

        String realFunction = namespace + ":func/" + path;

        if (nbt != null) {
            try {
                StringNbtReader.parse(nbt);
            } catch (Exception e) {
                source.sendError(Text.literal("Invalid NBT."));
                return 0;
            }
        }

        MinecraftServer server = source.getServer();
        ServerCommandSource serverSource = server.getCommandSource();

        String command = "function " + realFunction;
        if (nbt != null) {
            command += " " + nbt;
        }

        server.getCommandManager().executeWithPrefix(serverSource, command);
        return 1;
    }
}
