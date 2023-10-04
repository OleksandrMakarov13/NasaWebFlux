package com.example.nasawebflux.controller;

import com.example.nasawebflux.service.LargestPictureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LargestPictureController {

    private final LargestPictureService service;

    @GetMapping(value = "/test", produces = "image/jpeg")
    public ResponseEntity<?> getLargestPhoto(@RequestParam int sol) {

        long start = System.currentTimeMillis();
        ResponseEntity<byte[]> response = ResponseEntity.ok(service.getLargestPhoto("https://api.nasa.gov", "/mars-photos/api/v1/rovers/curiosity/photos", sol));
        System.out.println(System.currentTimeMillis() - start + " ms");

        return response;
    }
}
