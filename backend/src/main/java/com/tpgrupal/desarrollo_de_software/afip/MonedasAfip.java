package com.tpgrupal.desarrollo_de_software.afip;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Catálogo de monedas de ARCA (FEParamGetTiposMonedas) con su código ISO 4217.
 * ARCA usa su propio código (MonId: PES, DOL, 060...) y el ISO (ARS, USD, EUR...) sirve para mostrar importes.
 */
public final class MonedasAfip {

    public record Definicion(String codigoIso, String codigoAfip, String denominacion) {
    }

    private static final List<Definicion> CATALOGO = List.of(
            new Definicion("USD", "002", "Dólar Libre EEUU"),
            new Definicion("CHF", "009", "Franco Suizo"),
            new Definicion("MXN", "010", "Pesos Mejicanos"),
            new Definicion("UYU", "011", "Pesos Uruguayos"),
            new Definicion("BRL", "012", "Real"),
            new Definicion("DKK", "014", "Coronas Danesas"),
            new Definicion("NOK", "015", "Coronas Noruegas"),
            new Definicion("SEK", "016", "Coronas Suecas"),
            new Definicion("CAD", "018", "Dólar Canadiense"),
            new Definicion("JPY", "019", "Yens"),
            new Definicion("GBP", "021", "Libra Esterlina"),
            new Definicion("VES", "023", "Bolívar Venezolano"),
            new Definicion("CZK", "024", "Corona Checa"),
            new Definicion("RSD", "025", "Dinar Serbio"),
            new Definicion("AUD", "026", "Dólar Australiano"),
            new Definicion("ANG", "028", "Florín (Antillas Holandesas)"),
            new Definicion("PYG", "029", "Guaraní"),
            new Definicion("ILS", "030", "Shekel (Israel)"),
            new Definicion("BOB", "031", "Peso Boliviano"),
            new Definicion("COP", "032", "Peso Colombiano"),
            new Definicion("CLP", "033", "Peso Chileno"),
            new Definicion("ZAR", "034", "Rand Sudafricano"),
            new Definicion("PEN", "035", "Nuevo Sol Peruano"),
            new Definicion("RON", "040", "Leu Rumano"),
            new Definicion("XDR", "041", "Derechos Especiales de Giro"),
            new Definicion("DOP", "042", "Peso Dominicano"),
            new Definicion("PAB", "043", "Balboas Panameñas"),
            new Definicion("NIO", "044", "Córdoba Nicaragüense"),
            new Definicion("MAD", "045", "Dirham Marroquí"),
            new Definicion("EGP", "046", "Libra Egipcia"),
            new Definicion("SAR", "047", "Riyal Saudita"),
            new Definicion("XAU", "049", "Gramos de Oro Fino"),
            new Definicion("HKD", "051", "Dólar de Hong Kong"),
            new Definicion("SGD", "052", "Dólar de Singapur"),
            new Definicion("JMD", "053", "Dólar de Jamaica"),
            new Definicion("TWD", "054", "Dólar de Taiwan"),
            new Definicion("GTQ", "055", "Quetzal Guatemalteco"),
            new Definicion("HUF", "056", "Forint (Hungría)"),
            new Definicion("THB", "057", "Baht (Tailandia)"),
            new Definicion("KWD", "059", "Dinar Kuwaiti"),
            new Definicion("EUR", "060", "Euro"),
            new Definicion("PLN", "061", "Zloty Polaco"),
            new Definicion("INR", "062", "Rupia Hindú"),
            new Definicion("HNL", "063", "Lempira Hondureña"),
            new Definicion("CNY", "064", "Yuan (Rep. Pop. China)"),
            new Definicion("ARS", "PES", "Pesos Argentinos"),
            new Definicion("USD", "DOL", "Dólar Estadounidense"),
            new Definicion("NZD", "NZD", "Dólar Neozelandes"),
            new Definicion("RUB", "RUB", "Rublo (Rusia)")
    );

    // Para un mismo ISO (USD) se prefiere el código alfabético de ARCA (DOL)
    private static final Map<String, Definicion> POR_ISO = CATALOGO.stream()
            .collect(Collectors.toMap(Definicion::codigoIso, Function.identity(),
                    (actual, otro) -> esAlfabetico(actual.codigoAfip()) ? actual : otro));

    private static final Map<String, Definicion> POR_AFIP = CATALOGO.stream()
            .collect(Collectors.toMap(Definicion::codigoAfip, Function.identity()));

    private MonedasAfip() {
    }

    public static List<Definicion> catalogo() {
        return CATALOGO;
    }

    /** Devuelve el MonId de ARCA a partir de un código de ARCA o ISO. Sin dato: PES (pesos). */
    public static String codigoAfip(String codigo) {
        if (codigo == null || codigo.isBlank()) return "PES";
        String valor = codigo.trim().toUpperCase(Locale.ROOT);
        if (POR_AFIP.containsKey(valor)) return valor;
        Definicion porIso = POR_ISO.get(valor);
        return porIso != null ? porIso.codigoAfip() : valor;
    }

    public static String codigoIso(String codigoAfip) {
        if (codigoAfip == null || codigoAfip.isBlank()) return "ARS";
        String valor = codigoAfip.trim().toUpperCase(Locale.ROOT);
        Definicion d = POR_AFIP.get(valor);
        if (d != null) return d.codigoIso();
        d = POR_ISO.get(valor);
        return d != null ? d.codigoIso() : valor;
    }

    public static String simbolo(String codigoIso) {
        if (codigoIso == null) return "$";
        return switch (codigoIso.toUpperCase(Locale.ROOT)) {
            case "ARS" -> "$";
            case "USD" -> "US$";
            case "EUR" -> "€";
            case "GBP" -> "£";
            case "BRL" -> "R$";
            case "JPY" -> "¥";
            default -> codigoIso.toUpperCase(Locale.ROOT);
        };
    }

    public static boolean esPesos(String codigoAfip) {
        return codigoAfip == null || "PES".equalsIgnoreCase(codigoAfip.trim());
    }

    private static boolean esAlfabetico(String codigo) {
        return codigo != null && !codigo.isEmpty() && !Character.isDigit(codigo.charAt(0));
    }
}
