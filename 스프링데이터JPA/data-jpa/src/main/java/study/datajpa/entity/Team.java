package study.datajpa.entity;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.catalina.LifecycleState;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@ToString(of = {"id", "name"})
public class Team {

    @Id @GeneratedValue
    @Column(name = "team_id")
    Integer id;

    String name;

    @OneToMany(mappedBy = "team")
    List<Member> members = new ArrayList<>();

    protected Team(){}

    public Team(String name) {
        this.name = name;
    }
}
