package hellojpa;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Member extends BaseEntity{
    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private int id;

    private String name;

//    @ManyToMany
//    private List<Product> products = new ArrayList<>();

//    @OneToOne
//    @JoinColumn(name = "LOCKER_ID")
//    private Locker locker;


//    @ManyToOne @JoinColumn(name = "TEAM_ID")
//    private Team team;
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

//    public Team getTeam() {
//        return team;
//    }
//
//    public void setTeam(Team team) {
//        this.team = team;
//        team.getMembers().add(this);
//    }
}
