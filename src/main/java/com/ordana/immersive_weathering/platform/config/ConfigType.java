package com.ordana.immersive_weathering.platform.config;

public enum ConfigType {
    /** Per player, never synced. */
    CLIENT,
    /** Loaded on both sides; when a client joins a server it receives the server values. */
    COMMON_SYNCED
}
