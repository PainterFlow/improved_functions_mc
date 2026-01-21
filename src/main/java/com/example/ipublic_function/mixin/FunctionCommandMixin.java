package com.example.ipublic_function.mixin;

import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Allows non-OPs to execute /function ONLY for functions inside:
 * data/<namespace>/function/ipublic/
 *
 * Vanilla behavior is otherwise untouched.
 */
@Mixin(FunctionCommand.class)
public abstract class FunctionCommandMixin {

    /**
     * Redirects the permission check inside FunctionCommand.execute(...)
     */
    @Redirect(
        method = "execute",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/command/ServerCommandSource;hasPermissionLevel(I)Z"
        )
    )
    private static boolean ipublic_function$allowPublicFunctions(
            ServerCommandSource source,
            int level,
            Identifier functionId
    ) {
        // Vanilla behavior for ops
        if (source.hasPermissionLevel(level)) {
            return true;
        }

        // Non-op: allow ONLY functions in ipublic/
        return functionId.getPath().startsWith("ipublic/");
    }
}