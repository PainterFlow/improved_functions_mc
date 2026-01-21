package com.example.funccommand.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FunctionCommand.class)
public abstract class FunctionCommandMixin {

    /**
     * This injects into the registration of the /function command
     * and replaces the permission check with a conditional one.
     */
    @Inject(
        method = "register",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void modifyFunctionPermission(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CallbackInfo ci
    ) {
        dispatcher.register(
            Commands.literal("function")
                .then(
                    Commands.argument("name", ResourceLocationArgument.id())
                        .requires(source -> {
                            // Vanilla behavior: OPs / console / command blocks
                            if (source.hasPermission(2)) {
                                return true;
                            }

                            // Only players get the relaxed rule
                            if (!(source.getEntity() instanceof ServerPlayer)) {
                                return false;
                            }

                            // At this point, Brigadier has parsed arguments
                            ResourceLocation id =
                                ResourceLocationArgument.getId(
                                    source.getContext(),
                                    "name"
                                );

                            // Allow only ipublic/
                            return id.getPath().startsWith("ipublic/");
                        })
                        .executes(ctx ->
                            FunctionCommand.run(
                                ctx.getSource(),
                                ResourceLocationArgument.getId(ctx, "name")
                            )
                        )
                )
        );

        // Cancel vanilla registration
        ci.cancel();
    }
}