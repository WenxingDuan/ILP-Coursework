/**
 * Class used to get the latitude and longitude of the location from web server. 
 *
 * @author Duan Wenxing
 */

package uk.ac.ed.inf;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class LongLatCatcher {
    private String port;
    private String address;

    /**
     * Class to store the details of a location for Gson analysis.
     */
    class LongLatDetail {
        String country;
        Square square;
        String nearestPlace;
        LngLat coordinates;
        String words;
        String language;
        String map;

        /**
         * Class to store the 2 diagonal points longitude and latitude.
         */
        class Square {
            LngLat southwest;
            LngLat northeast;
        }

        /**
         * Class to store longitude and latitude.
         */
        class LngLat {
            float lng;
            float lat;
        }
    }

    /**
     * Constructor of LongLatCatcher class. In the method, function create the
     * address string.
     * 
     * @param port the communication web port to the server
     */
    public LongLatCatcher(String port) {
        this.port = port;
        this.address = "http://localhost:" + port;

    }

    /**
     * Method to get the longitude and latitude fo the input WhatThreeWords
     * location.
     * 
     * @param words the WhatThreeWords location
     * @return longitude and latitude of the input WhatThreeWords in type of
     *         {@link LongLat}
     */
    public LongLat getCenterLongLat(String words) {
        String menuString = ServerUtils.get(linkBuilder(words));
        LongLatDetail detail = buildLongLatDetail(menuString);
        LongLat longLatDetail = new LongLat(detail.coordinates.lng, detail.coordinates.lat);
        return longLatDetail;
    }

    /**
     * Method to transform the input WhatThreeWords into the link of its location
     * details json
     * 
     * @param words the WhatThreeWords location
     * @return the web link to the WhatThreeWords json
     */
    private String linkBuilder(String words) {

        return (this.address + "/words/" + words.replace(".", "/") + "/details.json");
    }

    /**
     * Method to transform the location detail json to the detail object in type of
     * {@link LongLatDetail}
     * 
     * @param detailString the location detail json in type of {@link String}
     * @return location detail object in type of {@link LongLatDetail}
     */
    private LongLatDetail buildLongLatDetail(String detailString) {
        Type respondType = new TypeToken<LongLatDetail>() {
        }.getType();
        return new Gson().fromJson(detailString, respondType);
    }

}
