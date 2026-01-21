package com.example.ipublic_function.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerFunctionManager.class)
public abstract class FunctionCommandMixin {

    @Inject(
        method = "execute",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ipublic_function$restrictNonOpFunctions(
            ResourceLocation functionId,
            CommandSourceStack source,
            CallbackInfoReturnable<Integer> cir
    ) {
        // If not a player (console, command block, system), allow
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // If player is op (permission level >= 2), allow
        if (source.hasPermission(2)) {
            return;
        }

        // Non-op: only allow functions in function/ipublic/
        if (!functionId.getPath().startsWith("ipublic/")) {
            // Vanilla-style failure: just return 0 (same as function not found / failed)
            cir.setReturnValue(0);
        }
    }
}
