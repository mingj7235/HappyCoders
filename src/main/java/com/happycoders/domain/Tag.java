package com.happycoders.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Builder @NoArgsConstructor @AllArgsConstructor
@Getter @Setter @EqualsAndHashCode (of = "id")
@Entity
public class Tag {

    @Id @GeneratedValue
    private Long id;

    @Column (unique = true, nullable = false)
    private String title;
}
