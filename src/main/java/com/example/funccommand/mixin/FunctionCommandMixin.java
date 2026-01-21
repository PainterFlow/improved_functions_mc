package com.example.funccommand.mixin;

import com.example.funccommand.mixin.ServerFunctionEntryAccessor; // 👈 REQUIRED

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ServerFunctionManager.class)
public abstract class FunctionCommandMixin {

    @Inject(
        method = "method_9206",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ipublic_function$restrictNonOpFunctions(
            Collection<?> entries,
            CompoundTag _tag,
            CommandSourceStack source,
            CallbackInfoReturnable<Integer> cir
    ) {
        if (source.hasPermission(2)) {
            return;
        }

        ServerPlayer player = source.getPlayer();
        if (player == null) {
            cir.setReturnValue(0);
            return;
        }

        for (Object entry : entries) {
            ResourceLocation id =
                ((ServerFunctionEntryAccessor) entry)
                    .ipublic_function$getId();

            if (!id.getPath().startsWith("ipublic/")) {
                cir.setReturnValue(0);
                return;
            }
        }
    }
}