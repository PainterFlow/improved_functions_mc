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
                        Commands.argument("input", StringArgumentType.greedyString())
                            .suggests(FuncCommandMod::suggestFunctions)
                            .executes(ctx ->
                                execute(ctx, StringArgumentType.getString(ctx, "input"))
                            )
                    )
            );
        });
    }

    // --------------------------------------------------
    // EXECUTION (NO OP REQUIRED, 1.21.11 SAFE)
    // --------------------------------------------------
    private static int execute(
        CommandContext<CommandSourceStack> ctx,
        String input
    ) {
        CommandSourceStack source = ctx.getSource();

        // Split "<function> [nbt...]"
        String functionPart;
        String nbtPart = null;

        int space = input.indexOf(' ');
        if (space == -1) {
            functionPart = input;
        } else {
            functionPart = input.substring(0, space);
            nbtPart = input.substring(space + 1);
        }

        ResourceLocation parsed;
        try {
            parsed = ResourceLocation.parse(functionPart);
        } catch (Exception e) {
            source.sendFailure(Component.literal("Invalid function ID"));
            return 0;
        }

        // data/<namespace>/functions/func/<path>.mcfunction
        ResourceLocation realFunction =
            ResourceLocation.fromNamespaceAndPath(
                parsed.getNamespace(),
                "func/" + parsed.getPath()
            );

        String command = "function " + realFunction;
        if (nbtPart != null && !nbtPart.isBlank()) {
            command += " " + nbtPart;
        }

        // 🔑 CORRECT FOR 1.21.11
        // Preserve player context, elevate permission
        CommandSourceStack elevated = source.withPermissionLevel(4);

        elevated.getServer()
            .getCommands()
            .performPrefixedCommand(elevated, command);

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