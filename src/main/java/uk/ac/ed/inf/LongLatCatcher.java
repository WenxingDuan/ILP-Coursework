/**
 * Class used to get the latitude and longitude of the location.
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

    class LngLat {
        float lng;
        float lat;
    }

    class LongLatDetail {
        String country;
        Square square;
        String nearestPlace;
        LngLat coordinates;
        String words;
        String language;
        String map;

        class Square {
            LngLat southwest;
            LngLat northeast;
        }

    }

    public LongLatCatcher(String port) {
        this.port = port;
        this.address = "http://localhost:" + port;

    }

    public LongLat getCenterLongLat(String words) {
        String manuString = Client.get(linkBuilder(words));
        LongLatDetail detail = buildLongLatDetail(manuString);
        LongLat longLatDetail = new LongLat(detail.coordinates.lng, detail.coordinates.lat);
        return longLatDetail;
    }

    private String linkBuilder(String words) {

        return (this.address + "/words/" + words.replace(".", "/") + "/details.json");
    }

    private LongLatDetail buildLongLatDetail(String detailString) {
        Type respondType = new TypeToken<LongLatDetail>() {
        }.getType();
        return new Gson().fromJson(detailString, respondType);
    }

}
