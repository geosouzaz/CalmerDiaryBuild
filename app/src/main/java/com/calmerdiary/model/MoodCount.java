package com.calmerdiary.model;

/**
 * Resultado agregado de humor (quantas vezes cada humor apareceu num período).
 * Usado pelas estatísticas — preenchido por uma consulta GROUP BY do Room.
 */
public class MoodCount {
    public int mood;
    public int count;
}
