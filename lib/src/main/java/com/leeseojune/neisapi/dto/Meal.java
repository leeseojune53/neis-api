package com.leeseojune.neisapi.dto;

import java.util.List;

public class Meal {

    private List<String> breakfast;

    private List<String> lunch;

    private List<String> dinner;

    public Meal() {}

    public void setBreakfast(List<String> breakfast) {
        this.breakfast = breakfast;
    }

    public void setLunch(List<String> lunch) {
        this.lunch = lunch;
    }

    public void setDinner(List<String> dinner) {
        this.dinner = dinner;
    }

    public List<String> getBreakfast() {
        return breakfast;
    }

    public List<String> getLunch() {
        return lunch;
    }

    public List<String> getDinner() {
        return dinner;
    }

}
