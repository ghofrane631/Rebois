package tn.esprit.ffw.risque.exception;

public class SecteurIntrouvableException extends RuntimeException {
    public SecteurIntrouvableException(Long id) { super("Secteur introuvable : " + id); }
}
