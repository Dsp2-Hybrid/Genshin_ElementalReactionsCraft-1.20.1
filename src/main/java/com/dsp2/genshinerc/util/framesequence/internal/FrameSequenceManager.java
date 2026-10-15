package com.dsp2.genshinerc.util.framesequence.internal;

import com.dsp2.genshinerc.util.framesequence.meta.FrameMetadata;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;

public class FrameSequenceManager {

    private static final Gson GSON = new Gson();

    public static FrameMetadata loadMetadata(ResourceLocation metaLocation) {
        try {
            Resource resource = Minecraft.getInstance()
                    .getResourceManager()
                    .getResource(metaLocation)
                    .orElseThrow();
            try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                return GSON.fromJson(reader, FrameMetadata.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load FrameSequence metadata: " + metaLocation, e);
        }
    }
}