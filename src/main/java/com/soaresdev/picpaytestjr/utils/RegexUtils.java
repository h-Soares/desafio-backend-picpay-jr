package com.soaresdev.picpaytestjr.utils;

public final class RegexUtils {

    private RegexUtils() {
    }

    public static final String CPF_REGEX = "^(\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11})$";
    public static final String CNPJ_REGEX = "^(\\d{2}\\.\\d{3}\\.\\d{3}\\/\\d{4}-\\d{2}|\\d{14})$";
    public static final String CPF_OR_CNPJ_REGEX = CPF_REGEX + "|" + CNPJ_REGEX;
}