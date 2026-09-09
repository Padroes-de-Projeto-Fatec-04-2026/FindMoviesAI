package br.com.fatec.cinemindai.movie;

import java.util.List;

public record MovieSummary(String id, String title, int year, List<String> genres, String synopsis) {
}
