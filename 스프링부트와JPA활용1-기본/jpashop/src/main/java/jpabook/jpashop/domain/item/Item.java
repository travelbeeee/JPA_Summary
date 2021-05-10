package jpabook.jpashop.domain.item;

import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public abstract class Item {
    @Id @GeneratedValue
    @Column(name = "ITEM_ID")
    private Integer id;

    private String name;

    private int price;

    private int stockQuantity;

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();

    // == 비지니스 로직 == //
    public void addStock(int quantity){
        this.stockQuantity += quantity;
    }
    public void removeStock(int quantity) throws NotEnoughStockException {
        if(this.stockQuantity < quantity)
            throw new NotEnoughStockException("need more stock");
        this.stockQuantity -= quantity;
    }
}