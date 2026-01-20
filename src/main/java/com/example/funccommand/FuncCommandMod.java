package com.example.funccommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public class FuncCommandMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("func")
                    .then(
                        Commands.argument("function", StringArgumentType.string())
                            .suggests(FuncCommandMod::suggestFunctions)
                            .executes(ctx ->
                                execute(ctx,
                                    StringArgumentType.getString(ctx, "function"),
                                    null)
                            )
                            .then(
                                Commands.argument("nbt", StringArgumentType.greedyString())
                                    .executes(ctx ->
                                        execute(ctx,
                                            StringArgumentType.getString(ctx, "function"),
                                            StringArgumentType.getString(ctx, "nbt"))
                                    )
                            )
                    )
            );
        });
    }

    // --------------------------------------------------
    // EXECUTION (PLAYER CONTEXT PRESERVED)
    // --------------------------------------------------
    private static int execute(
        CommandContext<CommandSourceStack> ctx,
        String inputFunction,
        String nbt
    ) {
        CommandSourceStack source = ctx.getSource();

        int colon = inputFunction.indexOf(':');
        if (colon == -1) {
            // Vanilla-style failure
            source.sendFailure(Component.translatable("commands.function.invalid"));
            return 0;
        }

        String namespace = inputFunction.substring(0, colon);
        String path = inputFunction.substring(colon + 1);

        // Redirect to data/<namespace>/functions/func/<path>.mcfunction
        String realFunction = namespace + ":func/" + path;

        String command = "function " + realFunction;
        if (nbt != null && !nbt.isBlank()) {
            command += " " + nbt;
        }

        // 🔑 KEY LINE:
        // Use the SAME CommandSourceStack the player has
        // so vanilla handles execution + feedback
        return source.getServer()
            .getCommands()
            .performPrefixedCommand(source, command);
    }

    // --------------------------------------------------
    // AUTOCOMPLETION
    // --------------------------------------------------
    private static CompletableFuture<Suggestions> suggestFunctions(
        CommandContext<CommandSourceStack> ctx,
        SuggestionsBuilder builder
    ) {
        MinecraftServer server = ctx.getSource().getServer();

        for (var id : server.getFunctions().getFunctionNames()) {
            // Only suggest functions inside data/*/functions/func/
            if (!id.getPath().startsWith("func/")) continue;

            String suggestion =
                id.getNamespace() + ":" +
                id.getPath().substring("func/".length());

            builder.suggest(suggestion);
        }

        return builder.buildFuture();
    }
}