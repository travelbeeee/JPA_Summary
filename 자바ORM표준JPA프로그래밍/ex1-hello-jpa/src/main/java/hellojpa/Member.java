package hellojpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Member {
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

    @Embedded
    Address address;

    @ElementCollection
    @CollectionTable(name = "FAVORITE_FOOD",
    joinColumns = @JoinColumn(name = "MEMBER_ID"))
    @Column(name = "FOOD_NAME")
    Set<String> favoriteFoods = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "DELIVERY_ADDRESS",
            joinColumns = @JoinColumn(name = "MEMBER_ID"))
    List<Address> deliveryAddress = new ArrayList<>();

    public Set<String> getFavoriteFoods() {
        return favoriteFoods;
    }

    public void setFavoriteFoods(Set<String> favoriteFoods) {
        this.favoriteFoods = favoriteFoods;
    }

    public List<Address> getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(List<Address> deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "TEAM_ID")
//    private Team team;

//    @ManyToMany
//    private List<Product> products = new ArrayList<>();

//    @OneToOne
//    @JoinColumn(name = "LOCKER_ID")
//    private Locker locker;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
