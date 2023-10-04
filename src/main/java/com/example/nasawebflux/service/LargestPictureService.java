package com.example.nasawebflux.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.Comparator;
import java.util.Objects;

@Service
public class LargestPictureService {
    private static final String API_KEY = "eBQUKf4CJvXCb4sKcVWYW0uQT4lKmufdNDPsLwT2";
    private WebClient webClient = WebClient.builder()
            .codecs(conf -> conf.defaultCodecs().maxInMemorySize(16*1024*1024))
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().followRedirect(true)))
            .build();

    public byte[] getLargestPhoto(String baseUrl, String path, int sol) {
        return Objects.requireNonNull(webClient.get()
                        .uri(baseUrl, b -> b.path(path)
                                .queryParam("sol", sol)
                                .queryParam("api_key", API_KEY)
                                .build())
                        .retrieve()
                        .toEntity(JsonNode.class)
                        .mapNotNull(HttpEntity::getBody)
                        .map(body -> body.findValuesAsText("img_src"))
                        .flatMapMany(Flux::fromIterable)
                        .flatMap(this::performRequest)
                        .collectList()
                        .block())
                .stream()
                .max(Comparator.comparing(Pair::contentLength))
                .map(p-> performGet(p.url))
                .orElseThrow();
    }

    private Mono<Pair> performRequest(String url) {
        return webClient.head()
                .uri(url)
                .retrieve()
                .toBodilessEntity()
                .map(respEntity -> new Pair(respEntity.getHeaders().getContentLength(), url));
    }

    private byte[] performGet(String url){
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }

    public record Pair(long contentLength, String url) {
    }
}

