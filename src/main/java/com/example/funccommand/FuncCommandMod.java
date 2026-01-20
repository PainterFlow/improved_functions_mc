package com.example.funccommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.CompletableFuture;

public class FuncCommandMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                Commands.literal("dfunc")
                    .then(
                        Commands.argument("function", ResourceLocationArgument.id())
                            .suggests(FuncCommandMod::suggestFunctions)
                            .executes(ctx ->
                                execute(
                                    ctx,
                                    ResourceLocationArgument.getId(ctx, "function"),
                                    null
                                )
                            )
                            .then(
                                Commands.argument("nbt", StringArgumentType.greedyString())
                                    .executes(ctx ->
                                        execute(
                                            ctx,
                                            ResourceLocationArgument.getId(ctx, "function"),
                                            ctx.getArgument("nbt", String.class)
                                        )
                                    )
                            )
                    )
            );
        });
    }

    // --------------------------------------------------
    // EXECUTION (PLAYER CONTEXT + VANILLA RESPONSES)
    // --------------------------------------------------
    private static int execute(
        CommandContext<CommandSourceStack> ctx,
        ResourceLocation inputFunction,
        String nbt
    ) {
        CommandSourceStack source = ctx.getSource();

        String namespace = inputFunction.getNamespace();
        String path = inputFunction.getPath();

        // data/<namespace>/functions/func/<path>.mcfunction
        ResourceLocation realFunction =
            ResourceLocation.fromNamespaceAndPath(namespace, "func/" + path);

        String command = "function " + realFunction;
        if (nbt != null && !nbt.isBlank()) {
            command += " " + nbt;
        }

        // Execute with full player context (vanilla behavior)
        source.getServer()
            .getCommands()
            .performPrefixedCommand(source, command);

        return 1;
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
            // Only functions inside data/*/functions/func/
            if (!id.getPath().startsWith("func/")) continue;

            ResourceLocation suggestionId =
                ResourceLocation.fromNamespaceAndPath(
                    id.getNamespace(),
                    id.getPath().substring("func/".length())
                );

            builder.suggest(suggestionId.toString());
        }

        return builder.buildFuture();
    }
}
