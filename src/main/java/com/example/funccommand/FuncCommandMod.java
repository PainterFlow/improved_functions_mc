package com.example.funccommand;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
                        Commands.argument("function", StringArgumentType.word())
                            .suggests(FuncCommandMod::suggestFunctions)
                            // /dfunc <function>
                            .executes(ctx ->
                                execute(
                                    ctx,
                                    StringArgumentType.getString(ctx, "function"),
                                    null
                                )
                            )
                            .then(
                                // /dfunc <function> <nbt>
                                Commands.argument("nbt", StringArgumentType.greedyString())
                                    .executes(ctx ->
                                        execute(
                                            ctx,
                                            StringArgumentType.getString(ctx, "function"),
                                            StringArgumentType.getString(ctx, "nbt")
                                        )
                                    )
                            )
                    )
            );
        });
    }

    // --------------------------------------------------
    // EXECUTION (PLAYER CONTEXT, VANILLA BEHAVIOR)
    // --------------------------------------------------
    private static int execute(
        CommandContext<CommandSourceStack> ctx,
        String functionId,
        String nbt
    ) {
        CommandSourceStack source = ctx.getSource();

        ResourceLocation parsed;
        try {
            parsed = ResourceLocation.parse(functionId);
        } catch (Exception e) {
            source.sendFailure(
                net.minecraft.network.chat.Component.literal("Invalid function ID")
            );
            return 0;
        }

        // Redirect to data/<namespace>/functions/func/<path>.mcfunction
        ResourceLocation realFunction =
            ResourceLocation.fromNamespaceAndPath(
                parsed.getNamespace(),
                "func/" + parsed.getPath()
            );

        String command = "function " + realFunction;
        if (nbt != null && !nbt.isBlank()) {
            command += " " + nbt;
        }

        // Execute with full player context
        source.getServer()
            .getCommands()
            .performPrefixedCommand(source, command);

        return 1;
    }

    // --------------------------------------------------
    // AUTOCOMPLETION (CLIENT-SAFE)
    // --------------------------------------------------
    private static CompletableFuture<Suggestions> suggestFunctions(
        CommandContext<CommandSourceStack> ctx,
        SuggestionsBuilder builder
    ) {
        MinecraftServer server = ctx.getSource().getServer();
        if (server == null) {
            return builder.buildFuture();
        }

        for (var id : server.getFunctions().getFunctionNames()) {
            if (!id.getPath().startsWith("func/")) continue;

            builder.suggest(
                id.getNamespace() + ":" +
                id.getPath().substring("func/".length())
            );
        }

        return builder.buildFuture();
    }
}