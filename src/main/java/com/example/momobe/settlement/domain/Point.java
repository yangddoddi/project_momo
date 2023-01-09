package com.example.momobe.settlement.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@NoArgsConstructor
public class Point {
    private Long points;

    public Point(Long payment){
        this.points = payment;
    }
    public void changeCurrentPoint(Long payment){
        this.points += payment;
    }

}
