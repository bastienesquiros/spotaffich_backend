package org.besquiros.spotaffich.service;

import org.besquiros.spotaffich.entity.GeoPoint;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DataNormalizer {

   public List<GeoPoint> normalizeData(String apiName, List<Map<String,Object>> data) {
       List<GeoPoint> dataNormalized = new ArrayList<>();



       return dataNormalized;
   }
}
