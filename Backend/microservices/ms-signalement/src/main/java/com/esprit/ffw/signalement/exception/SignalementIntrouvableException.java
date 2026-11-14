package com.esprit.ffw.signalement.exception;
public class SignalementIntrouvableException extends RuntimeException {
    public SignalementIntrouvableException(String id) { super("Signalement introuvable : " + id); }
}
