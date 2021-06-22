package com.leeseojune.neisapi

import com.leeseojune.neisapi.dto.Meal
import com.leeseojune.neisapi.dto.School
import spock.lang.Specification

class NeisApiTest extends Specification {

    def neisApi = new NeisApi.Builder()
    .build();

    def "학교 이름으로 학교 찾기"() {
        when:
        List<School> result = neisApi.getSchoolByName(name)

        then:
        assert result.get(0).getName() == schoolName

        where:
        name | schoolName
        "대덕소" | "대덕소프트웨어마이스터고등학교"


    }

//    def "GetMealsByAbsoluteDay"() {
//        when:
//        Meal result = neisApi.getMealsByAbsoluteDay("20210623", scCode, schoolCode)
//
//        then:
//        assert result.getBreakfast().get(0) == menuName
//
//        where:
//        scCode | schoolCode | menuName
//        "G10" | "7430310" | "시리얼"
//    }

    def "GetMealsByRelativeDay"() {
    }
}
