package com.drugbox.service;


import com.drugbox.common.Util.GeocodingUtil;
import com.drugbox.common.exception.CustomException;
import com.drugbox.common.exception.ErrorCode;
import com.drugbox.domain.BinId;
import com.drugbox.domain.BinLocation;
import com.drugbox.dto.request.CoordRequest;
import com.drugbox.dto.response.BinLocationResponse;
import com.drugbox.dto.response.MapDetailResponse;
import com.drugbox.dto.response.MapResponse;
import com.drugbox.repository.BinLocationRepository;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MapService {
    private final BinLocationRepository binLocationRepository;
    private final GeocodingUtil geocodingUtil;

    @Value("${application.spring.cloud.gcp.placeAPI}")
    private String API_KEY;

    private static final String[] divisions = {"서울특별시", "전라남도", "경기도", "경상남도"};

    public Long saveSeoulDrugBinLocations(){
        JSONParser parser = new JSONParser();
        Long cnt=1L;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("seoul.geojson")){
            JSONObject jsonObject = (JSONObject) parser.parse(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));

            JSONArray features = (JSONArray) jsonObject.get("features");
            for(Object obj : features){
                JSONObject feature = (JSONObject) obj;
                JSONObject properties = (JSONObject) feature.get("properties");
                String lat = (String) properties.get("COORD_Y");
                String lng = (String) properties.get("COORD_X");
                String addr = (String) properties.get("ADDR_NEW"); // 도로명주소
                String detail = (String) properties.get("VALUE_01");  // ex) 복지관, 구청, 주민센터
                String[] parts = addr.split("\\s+");
                String addrLvl1 = parts[0];
                String addrLvl2 = parts[1];
                Integer divisionKey=0;
                for(int i=0; i<divisions.length; i++){
                    if(divisions[i].equals(addrLvl1)){
                        divisionKey = i+1;
                        break;
                    }
                }

                BinLocation bin = BinLocation.builder()
                        .id(new BinId(cnt++, divisionKey))
                        .lat(lat)
                        .lng(lng)
                        .address(addr)
                        .detail(detail)
                        .addrLvl1(addrLvl1)
                        .addrLvl2(addrLvl2)
                        .build();
                binLocationRepository.save(bin);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e){
            e.printStackTrace();
        }
        return cnt;
    }

    public void saveDrugBinLocations(){
        Long cnt = saveSeoulDrugBinLocations() +1;

        String[] line;
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("drugbin.CSV")){
            CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream, "UTF-8"))
                    .withSkipLines(1) // skip header
                    .build();
            while((line = csvReader.readNext()) != null) {
                String lat = "";
                String lng = "";
                if(line[4].equals("") || line[5].equals("")){
                    Map<String, String> coords = geocodingUtil.getCoordsByAddress(line[3]);
                    lat = coords.get("lat");
                    lng = coords.get("lng");
                } else{
                    lat = line[4];
                    lng = line[5];
                }
                Integer divisionKey=0;
                String addrLvl1 = line[0];
                for(int i=0; i<divisions.length; i++){
                    if(divisions[i].equals(addrLvl1)){
                        divisionKey = i+1;
                        break;
                    }
                }
                BinLocation bin = BinLocation.builder()
                        .id(new BinId(cnt++, divisionKey))
                        .lat(lat)
                        .lng(lng)
                        .addrLvl1(line[0])
                        .addrLvl2(line[1])
                        .detail(line[2])
                        .address(line[3])
                        .build();
                binLocationRepository.save(bin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BinLocationResponse> getSeoulDrugBinLocations(){
        List<BinLocation> binLocations = binLocationRepository.findAll();
        return binLocations.stream()
                .map(bin -> BinLocationToBinLocationResponse(bin))
                .collect(Collectors.toList());
    }

    public List<BinLocationResponse> getDivisionDrugBinLocations(String addrLvl1, String addrLvl2){
        List<BinLocation> binLocations = new ArrayList<>();
        if(addrLvl2 == null){
            binLocations = binLocationRepository.findAllByAddrLvl1(addrLvl1);
        } else {
            binLocations = binLocationRepository.findAllByAddrLvl1AndAddrLvl2(addrLvl1, addrLvl2);
        }
        return binLocations.stream()
                .map(bin -> BinLocationToBinLocationResponse(bin))
                .collect(Collectors.toList());
    }

    private BinLocationResponse BinLocationToBinLocationResponse(BinLocation bin){
        return BinLocationResponse.builder()
                .id(bin.getId().getBinId())
                .address(bin.getAddress())
                .lat(bin.getLat())
                .lng(bin.getLng())
                .detail(bin.getDetail())
                .addrLvl1(bin.getAddrLvl1())
                .addrLvl2(bin.getAddrLvl2())
                .build();
    }

    public List<MapResponse> getNearbyPharmacyAndConvenienceLocations(CoordRequest coordRequest) throws IOException, ParseException {
        String requestUrl = "https://places.googleapis.com/v1/places:searchNearby";
        StringBuilder requestBody = new StringBuilder();
        requestBody.append("{\"includedTypes\":[\"pharmacy\",\"convenience_store\"],"); // 약국, 편의점 검색
        requestBody.append("\"locationRestriction\":{" +
                "\"circle\":{" +
                     "\"center\":{" +
                    "\"latitude\":"+ coordRequest.getLatitude() +
                    ",\"longitude\":" + coordRequest.getLongitude() + "}," +
                "\"radius\": 1000.0 } }," +
            "\"languageCode\":\"ko\"}");

        return getLocationsDetail(requestUrl,requestBody.toString());
    }

    // 위치 검색하기
    public List<MapResponse> searchLocations(String name) throws IOException, ParseException {
        String requestUrl = "https://places.googleapis.com/v1/places:searchText";
        String requestBody = "{\"textQuery\":\""+ name+"\"," +
                "\"languageCode\":\"ko\"}";

        return getLocationsDetail(requestUrl,requestBody);
    }

    // 장소 세부사항
    public MapDetailResponse searchLocationDetail(String id) throws IOException, ParseException {
        String requestUrl = "https://places.googleapis.com/v1/places/"+id;
        return getLocationDetail(requestUrl);
    }

    private List<MapResponse> getLocationsDetail(String requestUrl, String requestBody) throws IOException, ParseException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("X-Goog-Api-key",API_KEY);
        conn.setRequestProperty("X-Goog-FieldMask","places.displayName,places.formattedAddress,places.id");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        StringBuilder response = new StringBuilder();
        String responseLine;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            while((responseLine = br.readLine()) != null){
                response.append(responseLine);
            }
        }
        conn.disconnect();

        return parseLocationsDetail(response.toString());
    }

    private MapDetailResponse getLocationDetail(String requestUrl) throws IOException, ParseException {
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type","application/json");
        conn.setRequestProperty("X-Goog-Api-key",API_KEY);
        conn.setRequestProperty("X-Goog-FieldMask","displayName,formattedAddress,id,photos,currentOpeningHours");
        conn.setDoInput(true);

        StringBuilder response = new StringBuilder();
        String responseLine;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))){
            while((responseLine = br.readLine()) != null){
                response.append(responseLine);
            }
        }
        conn.disconnect();

        return parseLocationDetail(response.toString());
    }

    private List<MapResponse> parseLocationsDetail(String result) throws ParseException {
        System.out.println(result);
        if(result.equals("{}")){
            throw new CustomException(ErrorCode.NOT_FOUND_MAP_RESULT);
        }
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(result);
        JSONArray locations = (JSONArray) jsonObject.get("places");
        List<MapResponse> mapResponses = new ArrayList<>();
        for(Object object: locations){
            JSONObject location = (JSONObject) object;
            Map<String, String> coords = geocodingUtil.getCoordsByAddress((String)location.get("formattedAddress"));
            JSONObject displayName = (JSONObject) location.get("displayName");
            MapResponse mapResponse = MapResponse.builder()
                    .locationName((String)displayName.get("text"))
                    .locationAddress((String)location.get("formattedAddress"))
                    .locationId((String)location.get("id"))
                    .latitude(coords.get("lat"))
                    .longitude(coords.get("lng"))
                    .build();
            mapResponses.add(mapResponse);
        }
        return mapResponses;
    }

    private MapDetailResponse parseLocationDetail(String result) throws ParseException, IOException {
        System.out.println(result);
        JSONParser parser = new JSONParser();
        JSONObject location = (JSONObject) parser.parse(result);
        JSONObject name = (JSONObject) location.get("displayName");
        JSONArray photos = (JSONArray) location.get("photos");
        JSONObject photo = (JSONObject) photos.get(0);
        return MapDetailResponse.builder()
                .locationName((String)name.get("text"))
                .locationPhotos(getLocationPhotoUrl((String)photo.get("name")))
                .locationId((String)location.get("id"))
                .formattedAddress((String)location.get("formattedAddress"))
                .currentOpeningHours((String)location.get("currentOpeningHours"))
                .build();
    }

    private String getLocationPhotoUrl(String photoName) throws IOException, ParseException {
        String requestUrl = "https://places.googleapis.com/v1/"+photoName+"/media?key="+API_KEY+"&maxHeightPx=400&maxWidthPx=400&skipHttpRedirect=true";

        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        StringBuilder response = new StringBuilder();
        String responseLine;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))){
            while((responseLine = br.readLine()) != null){
                response.append(responseLine);
            }
        }
        conn.disconnect();
        return parseLocationPhotoUrl(response.toString());
    }

    private String parseLocationPhotoUrl(String result) throws ParseException {
        System.out.println("\n\n"+result);
        JSONParser parser = new JSONParser();
        JSONObject photo = (JSONObject) parser.parse(result);
        return (String) photo.get("photoUri");
    }
}

