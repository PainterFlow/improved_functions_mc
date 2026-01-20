package com.example.funccommand;

import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

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
                        Commands.argument("input", StringArgumentType.greedyString())
                            .suggests(FuncCommandMod::suggestFunctions)
                            .executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "input")))
                    )
            );
        });
    }

    private static int execute(CommandContext<CommandSourceStack> ctx, String input) {
        CommandSourceStack source = ctx.getSource();

        // Split function + optional NBT
        String functionPart;
        String nbtPart = null;

        int space = input.indexOf(' ');
        if (space == -1) {
            functionPart = input;
        } else {
            functionPart = input.substring(0, space);
            nbtPart = input.substring(space + 1);
        }

        int colon = functionPart.indexOf(':');
        if (colon == -1) {
            source.sendFailure(Component.literal("Invalid function ID. Use namespace:path"));
            return 0;
        }

        String namespace = functionPart.substring(0, colon);
        String path = functionPart.substring(colon + 1);

        // Map to data/<namespace>/functions/func/<path>.mcfunction
        String realFunction = namespace + ":func/" + path;

        MinecraftServer server = source.getServer();
        CommandSourceStack serverSource = server.createCommandSourceStack();

        String command = "function " + realFunction;
        if (nbtPart != null && !nbtPart.isBlank()) {
            command += " " + nbtPart;
        }

        server.getCommands().performPrefixedCommand(serverSource, command);
        return 1;
    }

    // --------------------
    // AUTOCOMPLETION
    // --------------------
private static CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestFunctions(
    CommandContext<CommandSourceStack> ctx,
    SuggestionsBuilder builder
) {
    MinecraftServer server = ctx.getSource().getServer();

    for (var id : server.getFunctions().getFunctionNames()) {
        // Only suggest functions inside /func/
        if (!id.getPath().startsWith("func/")) continue;

        String suggestion =
            id.getNamespace() + ":" + id.getPath().substring("func/".length());

        builder.suggest(suggestion);
    }

    return builder.buildFuture();
}
}
