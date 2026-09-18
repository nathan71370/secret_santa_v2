package fr.azrodorza;

import lombok.NonNull;

public record Player(String name, String email, String previousTarget) {
    @Override
    @NonNull
    public String toString() {
        return name;
    }
}