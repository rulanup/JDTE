package com.jdte.common.solar;

public enum SolarPanelTier {
    CONCENTRATED("concentrated_solar_panel", 46_080, 4_608_000, false),
    SINGULARITY("singularity_solar_panel", 184_320, 18_432_000, false),
    STELLAR_FUSION("stellar_fusion_solar_panel", 737_280, 73_728_000, false),
    DIMENSIONAL_COLLAPSE("dimensional_collapse_solar_panel", 2_949_120, 294_912_000, false),
    CREATIVE("creative_solar_panel", Integer.MAX_VALUE, Integer.MAX_VALUE, true);

    private final String serializedName;
    private final int defaultGeneration;
    private final int defaultCapacity;
    private final boolean creative;

    SolarPanelTier(String serializedName, int defaultGeneration, int defaultCapacity, boolean creative) {
        this.serializedName = serializedName;
        this.defaultGeneration = defaultGeneration;
        this.defaultCapacity = defaultCapacity;
        this.creative = creative;
    }

    public String serializedName() {
        return serializedName;
    }

    public int defaultGeneration() {
        return defaultGeneration;
    }

    public int defaultCapacity() {
        return defaultCapacity;
    }

    public boolean creative() {
        return creative;
    }
}
