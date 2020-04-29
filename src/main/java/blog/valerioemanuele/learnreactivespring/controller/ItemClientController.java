package blog.valerioemanuele.learnreactivespring.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import blog.valerioemanuele.learnreactivespring.domain.Item;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
public class ItemClientController {
    WebClient webClient = WebClient.create("http://localhost:8081");
    
    @GetMapping("/client/retrieve")
    public Flux<Item> allItemsUsingRetrieve(){
        return webClient.get().uri("/v1/items")
            .retrieve()
            .bodyToFlux(Item.class)
            .log("Items in client project retrieve: ");
    }
    
    
    @GetMapping("/client/exchange")
    public Flux<Item> allItemsUsingExchange(){
        return webClient.get().uri("/v1/items")
            .exchange()
            .flatMapMany(clientResponse -> clientResponse.bodyToFlux(Item.class))
            .log("Items in client project exchange: ");
    }
    
    @GetMapping("/client/retrieve/singleItem")
    public Mono<Item> oneItemUsingRetrieve(){
        String id = "ABC";
        return webClient.get().uri("/v1/items/{id}", id)
            .retrieve()
            .bodyToMono(Item.class)
            .log("Single Item in client project retrieve: ");
    }
    
    @GetMapping("/client/exchange/singleItem")
    public Mono<Item> oneItemUsingExchange(){
        String id = "ABC";
        return webClient.get().uri("/v1/items/{id}", id)
            .exchange()
            .flatMap(clientResponse -> clientResponse.bodyToMono(Item.class))
            .log("Single Item in client project exchange: ");
    }
    
    @PutMapping("/client/createItem")
    public Mono<Item> createItem(@RequestBody Item item){
        return webClient.put().uri("/v1/items")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(item), Item.class)
            .retrieve()
            .bodyToMono(Item.class)
            .log("Created item is: ");
    }
    
    @PostMapping("/client/updateItem/{id}")
    public Mono<Item> updateItem(@PathVariable String id, @RequestBody Item item){
        return webClient.post().uri("/v1/items/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(item), Item.class)
            .retrieve()
            .bodyToMono(Item.class)
            .log("Updated item is: ");
    }
    
    @DeleteMapping("/client/deleteItem/{id}")
    public Mono<Void> deleteItem(@PathVariable String id){
        return webClient.delete().uri("/v1/items/{id}", id)
            .retrieve()
            .bodyToMono(Void.class)
            .log("Deleted item is: ");
    }
    
    @GetMapping("/client/retrieve/error")
    public Flux<Item> errorRetrieve(){
       
        return webClient.get().uri("/v1/items/runtimeException")
            .retrieve()
            .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                Mono<String> errorMono = clientResponse.bodyToMono(String.class);
                return errorMono.flatMap(errorMessage -> {
                    log.error("The error Message is: " + errorMessage);
                    throw new RuntimeException(errorMessage);
                });
            })
            .bodyToFlux(Item.class)
            .log("Retrieve error: ");
    }
    
    @GetMapping("/client/exchange/error")
    public Flux<Item> errorExchange(){
       
        return webClient.get().uri("/v1/items/runtimeException")
            .exchange()
            .flatMapMany(clientResponse -> {
                if(clientResponse.statusCode().is5xxServerError()){
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(errorMessage -> {
                                log.error("Error Message in errorExchange: " + errorMessage);
                                throw new RuntimeException(errorMessage);
                            });
                }
                else {
                    return clientResponse.bodyToFlux(Item.class);
                }
            });
    }
    
}
