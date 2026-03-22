package com.electronwill.nightconfig.toml;

public enum TomlVersion {
    v1_0,
    v1_1;

    @Override
    public String toString() {
        switch (this) {
            case v1_0:
                return "v1.0";
            case v1_1:
                return "v1.1";
            default:
                return "?";
        }
    }
}
