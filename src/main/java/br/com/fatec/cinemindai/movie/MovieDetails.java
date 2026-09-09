package br.com.fatec.cinemindai.movie;

import java.util.List;

public record MovieDetails(
        String id,
        String title,
        int year,
        List<String> genres,
        String synopsis,
        String director,
        List<String> cast,
        double rating) {
}
