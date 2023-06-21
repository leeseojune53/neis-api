package com.leeseojune.neisapi;

import com.leeseojune.neisapi.dto.Meal;
import com.leeseojune.neisapi.dto.School;
import com.leeseojune.neisapi.exceptions.detailed.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Instance of the MealApi class provider access to the neis api
 */
public class NeisApi {

    /**
     *  The default meal host of NeisApi calls.
     */
    private static final String DEFAULT_MEAL_HOST = "https://open.neis.go.kr/hub/mealServiceDietInfo";

    /**
     * The default school host of NeisApi calls.
     */
    private static final String DEFAULT_SCHOOL_HOST = "https://open.neis.go.kr/hub/schoolInfo";

    private static final Pattern PATTERN_BRACKET = Pattern.compile("\\([^\\(\\)]+\\)");

    private static final String MENU_PATTERN = "[^\\uAC00-\\uD7AF\\u1100-\\u11FF\\u3130-\\u318F\n]";

    private String mealHost;
    private String schoolHost;
    private String menuPattern;

    public NeisApi(Builder builder) {
        this.mealHost = builder.mealHost;
        this.schoolHost = builder.schoolHost;
        this.menuPattern = builder.menuPattern;
    }

    /**
     *
     * @param name school name ex)대덕소프트웨어
     * @return List of School scCode, schoolCode, location, name
     * @throws ParseException if an error occurs during parsing
     */
    public List<School> getSchoolByName(String name) throws ParseException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        Document doc;
        try{
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            name = URLEncoder.encode(name, StandardCharsets.UTF_8);
            doc = dBuilder.parse(schoolHost + "?SCHUL_NM=" + name);
        }catch (ParserConfigurationException | SAXException | IOException e) {
            throw new ParseException();
        }

        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("row");

        List<School> response = new ArrayList<>();

        for(int i=0, length = nList.getLength(); i < length; i++){
            Node node = nList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                response.add(new School(getTagValue("ATPT_OFCDC_SC_CODE", element), getTagValue("SD_SCHUL_CODE", element),
                        getTagValue("LCTN_SC_NM", element), getTagValue("SCHUL_NM", element)));
            }
        }

        return response;
    }

    /**
     *
     * @param day                                                       ex)20210616
     * @param scCode Neis calls this parameter "ATPT_OFCDC_SC_CODE"     ex)G10
     * @param schoolCode Neis calls this parameter "SD_SCHUL_CODE"      ex)7430310
     * @param apiKey Neis calls this parameter "KEY"                    ex)794ba8d38c6820490216b865063c7b28
     * @return Meals breakfast, lunch, dinner
     * @throws ParseException if an error occurs during parsing
     */
    public Meal getMealsByAbsoluteDay(String day, String scCode, String schoolCode, String apiKey) throws ParseException {
        Document doc;

        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(mealHost + "?ATPT_OFCDC_SC_CODE=" + scCode + "&SD_SCHUL_CODE=" + schoolCode
                    + "&MLSV_YMD=" + day
                    + "&KEY=" + apiKey);
        }catch(ParserConfigurationException | SAXException | IOException e){
            throw new ParseException();
        }
        return getMeal(doc);
    }

    /**
     *
     * @param day                                                       ex)10
     * @param scCode Neis calls this parameter "ATPT_OFCDC_SC_CODE"     ex)G10
     * @param schoolCode Neis calls this parameter "SD_SCHUL_CODE"      ex)7430310
     * @param apiKey Neis calls this parameter "KEY"                    ex)794ba8d38c6820490216b865063c7b28
     * @return Meals breakfast, lunch, dinner
     * @throws ParseException if an error occurs during parsing
     */
    public Meal getMealsByRelativeDay(int day, String scCode, String schoolCode, String apiKey) throws ParseException {
        LocalDate date = LocalDate.now().plusDays(day);
        Document doc;

        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(mealHost + "?ATPT_OFCDC_SC_CODE=" + scCode + "&SD_SCHUL_CODE=" + schoolCode
                    + "&MLSV_YMD=" + date.toString().replace("-", "")
                    + "&KEY=" + apiKey);
        }catch(ParserConfigurationException | SAXException | IOException e){
            throw new ParseException();
        }

        return getMeal(doc);
    }

    private Meal getMeal(Document doc) {
        doc.getDocumentElement().normalize();

        NodeList nList = doc.getElementsByTagName("row");

        Meal response = new Meal();

        for (int i = 0, length = nList.getLength(); i < length; i++) {
            Node node = nList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                List<String> menu = new ArrayList<>();
                String[] menus = deleteBracketTextByPattern(getTagValue("DDISH_NM", element)).split(menuPattern);

                for (String value : menus) {
                    if (value.length() != 0) {
                        menu.add(value);
                    }
                }

                switch (getTagValue("MMEAL_SC_NM", element)) {
                    case "조식":
                        response.setBreakfast(menu);
                        break;
                    case "중식":
                        response.setLunch(menu);
                        break;
                    case "석식":
                        response.setDinner(menu);
                        break;
                    default:
                        break;
                }
            }
        }

        return response;
    }

    private String getTagValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();

        Node nValue = nodeList.item(0);
        if (nValue == null)
            return "";
        return nValue.getNodeValue();
    }

    private static String deleteBracketTextByPattern(String text) {

        Matcher matcher = PATTERN_BRACKET.matcher(text);


        String pureText = text;
        String removeTextArea;

        while (matcher.find()) {
            int startIndex = matcher.start();
            int endIndex = matcher.end();

            removeTextArea = pureText.substring(startIndex, endIndex);
            pureText = pureText.replace(removeTextArea, "");
            matcher = PATTERN_BRACKET.matcher(pureText);
        }

        return pureText;
    }

    /**
     *  Builder class for building {@link NeisApi} instances.
     */
    public static class Builder {

        private String mealHost = DEFAULT_MEAL_HOST;
        private String schoolHost = DEFAULT_SCHOOL_HOST;
        private String menuPattern = MENU_PATTERN;

        /**
         * @param mealHost The Meal host setter.
         * @return A {@link Builder}
         */
        public Builder setMealHost(String mealHost) {
            this.mealHost = mealHost;
            return this;
        }

        /**
         * @param schoolHost The School host setter.
         * @return A {@link Builder}
         */
        public Builder setSchoolHost(String schoolHost) {
            this.schoolHost = schoolHost;
            return this;
        }

        /**
         * @param menuPattern The Menu pattern setter.
         * @return A {@link Builder}
         */
        public Builder setMenuPattern(String menuPattern) {
            this.menuPattern = menuPattern;
            return this;
        }

        public NeisApi build() {
            return new NeisApi(this);
        }

    }

}
