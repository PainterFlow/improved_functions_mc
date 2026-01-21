package com.example.funccommand.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.FunctionCommand;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FunctionCommand.class)
public abstract class FunctionCommandMixin {

    @Inject(
        method = "queueFunctions",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void ipublic_function$restrictNonOpFunctions(
            CommandSourceStack source,
            ResourceLocation functionId,
            CallbackInfo ci
    ) {
        // OPs / vanilla behavior untouched
        if (source.hasPermission(2)) {
            return;
        }

        // Only players may bypass
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            ci.cancel();
            return;
        }

        // Only allow ipublic/*
        if (!functionId.getPath().startsWith("ipublic/")) {
            ci.cancel();
        }
    }
}