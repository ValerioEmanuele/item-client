package blog.valerioemanuele.learnreactivespring.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Item {
    private String id;
    private String description;
    private Double price;
}