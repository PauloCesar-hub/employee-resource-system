package br.com.ers.model;

public enum CategoriaRecurso {
    NOTEBOOK,
    CELULAR,
    CADEIRA,
    MONITOR,
    LICENCA,
    ACESSORIO,
    OUTRO;

    public static CategoriaRecurso fromText(String text) {
        if (text == null || text.isBlank()) {
            return OUTRO;
        }
        String normalized = text.trim().toUpperCase()
                .replace("Ç", "C")
                .replace("Ã", "A")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Ê", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Õ", "O")
                .replace("Ú", "U");
        for (CategoriaRecurso value : values()) {
            if (value.name().equals(normalized)) {
                return value;
            }
        }
        return OUTRO;
    }
}
