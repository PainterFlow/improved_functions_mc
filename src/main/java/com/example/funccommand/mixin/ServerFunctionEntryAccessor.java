package com.example.funccommand.mixin;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.server.ServerFunctionManager$Entry")
public interface ServerFunctionEntryAccessor {

    @Accessor("id")
    ResourceLocation ipublic_function$getId();
}