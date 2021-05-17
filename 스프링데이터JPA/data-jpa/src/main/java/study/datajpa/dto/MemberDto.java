package study.datajpa.dto;

import lombok.Data;

@Data
public class MemberDto {
    private Integer id;
    private String username;
    private String teamName;

    public MemberDto(Integer id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
}
