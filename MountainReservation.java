import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.google.gson.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;

// 추상 클래스 Mountain
abstract class Mountain {
    protected String name;
    protected String reserver;
    protected LocalDate reservationDate;
    protected String reservationTimeSlot;

    // 추가: 최대 예약 인원, 현재 예약 인원
    protected int maxCapacity = 0;
    protected int currentReservations = 0;

    public Mountain(String name) {
        this.name = name;
    }

    public void reserve(String reserver, LocalDate date, String timeSlot) {
        this.reserver = reserver;
        this.reservationDate = date;
        this.reservationTimeSlot = timeSlot;
    }

    public String getName() { return name; }

    // 추상 메서드: 해발고도
    public abstract int getAltitude();

    // 추가: 최대/현재 인원 getter/setter
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setCurrentReservations(int currentReservations) { this.currentReservations = currentReservations; }
    public int getCurrentReservations() { return currentReservations; }

    public void printReservationInfo() {
        System.out.println("예약자: " + reserver);
        System.out.println("산: " + name);
        System.out.println("해발고도: " + getAltitude() + "m");
        System.out.println("등산일: " + reservationDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
        System.out.println("등산시간: " + reservationTimeSlot);
        // 추가: 최대/현재 예약 인원 출력
        System.out.println("최대 예약 인원: " + maxCapacity);
        System.out.println("현재 예약 인원: " + currentReservations);
    }

    public String getQRData() {
        return "예약자: " + reserver +
                "\n산: " + name +
                "\n해발고도: " + getAltitude() + "m" +
                "\n등산일: " + reservationDate.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                "\n등산시간: " + reservationTimeSlot;
    }
}

// 산 클래스들
class Bukhansan extends Mountain { public Bukhansan() { super("북한산"); } public int getAltitude() { return 836; } }
class Gwanaksan extends Mountain { public Gwanaksan() { super("관악산"); } public int getAltitude() { return 632; } }
class Dobongsan extends Mountain { public Dobongsan() { super("도봉산"); } public int getAltitude() { return 740; } }
class Cheonggyesan extends Mountain { public Cheonggyesan() { super("청계산"); } public int getAltitude() { return 620; } }
class Suraksan extends Mountain { public Suraksan() { super("수락산"); } public int getAltitude() { return 638; } }
class Bulamsan extends Mountain { public Bulamsan() { super("불암산"); } public int getAltitude() { return 508; } }
class Achasan extends Mountain { public Achasan() { super("아차산"); } public int getAltitude() { return 287; } }
class Yongmasan extends Mountain { public Yongmasan() { super("용마산"); } public int getAltitude() { return 348; } }
class Inwangsan extends Mountain { public Inwangsan() { super("인왕산"); } public int getAltitude() { return 338; } }
class Umyeonsan extends Mountain { public Umyeonsan() { super("우면산"); } public int getAltitude() { return 293; } }

public class MountainReservation {
    private static final String SERVICE_KEY = "p%2FKd80ZfJ3x1VJt8BTvmEAbe%2BoC1OkeXZZjQM3DGqejKN6FyuoRpWNd87ZngFXzTH1cFYjVnH4X7q21nHJNq%2Fg%3D%3D";
    private static final String SIDO_NAME = "서울특별시";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random(); // 추가: 난수 생성을 위한 Random

        System.out.println("====================");
        System.out.println("서울 특별시 등산 예약 시스템");
        System.out.println("====================");
        System.out.print("예약자 이름: ");
        String userName = sc.nextLine().trim();
        System.out.print("등산 날짜를 입력하세요 (YYYYMMDD, 오늘~3일 이내): ");
        String dateInput = sc.nextLine().trim();

        String[] timeSlots = {
                "05시~08시까지",
                "08시~10시까지",
                "10시~12시까지",
                "12시~14시까지",
                "14시~16시까지",
                "16시~18시까지",
                "18시~19시까지"
        };
        System.out.println("등산 시간을 선택하세요:");
        for (int i = 0; i < timeSlots.length; i++) {
            System.out.println((i + 1) + ". " + timeSlots[i]);
        }
        int timeIdx;
        try {
            System.out.print("번호 입력: ");
            timeIdx = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("잘못된 입력입니다.");
            return;
        }
        if (timeIdx < 1 || timeIdx > timeSlots.length) {
            System.out.println("잘못된 선택입니다.");
            return;
        }
        String selectedTimeSlot = timeSlots[timeIdx - 1];

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(3);
        LocalDate reservationDate;
        try {
            reservationDate = LocalDate.parse(dateInput, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            System.out.println("날짜 형식이 올바르지 않습니다. (예: 20250507)");
            return;
        }
        if (reservationDate.isBefore(today) || reservationDate.isAfter(maxDate)) {
            System.out.println("오늘(" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + ")부터 "
                    + maxDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "까지의 날짜만 입력 가능합니다.");
            return;
        }

        List<Mountain> mountainList = Arrays.asList(
                new Bukhansan(), new Gwanaksan(), new Dobongsan(),
                new Cheonggyesan(), new Suraksan(), new Bulamsan(),
                new Achasan(), new Yongmasan(), new Inwangsan(), new Umyeonsan()
        );
        System.out.println("등산할 산을 선택하세요:");
        for (int i = 0; i < mountainList.size(); i++) {
            System.out.println((i + 1) + ". " + mountainList.get(i).getName());
        }
        int mIdx;
        try {
            mIdx = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("잘못된 입력입니다.");
            return;
        }
        if (mIdx < 1 || mIdx > mountainList.size()) {
            System.out.println("잘못된 선택입니다.");
            return;
        }
        Mountain selectedMountain = mountainList.get(mIdx - 1);

        // 예약 정보 임시 저장 (아래에서 예약 마감 체크 후 최종 예약 처리)
        String tempReserver = userName;
        LocalDate tempDate = reservationDate;
        String tempTimeSlot = selectedTimeSlot;

        // ---- 산불 위험도 API 연동 및 최대/현재 예약 인원 처리 ----
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime alarmTime = LocalDateTime.of(reservationDate.minusDays(1), java.time.LocalTime.of(23, 0));
        String analDateTime = reservationDate.minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE) + "23";
        String searchDate = reservationDate.format(DateTimeFormatter.BASIC_ISO_DATE);
        List<JsonObject> districts = getDistricts(searchDate, analDateTime);
        int maxCapacity = 0;
        int currentReservations = 0;
        int meanValue = -1;

        if (!districts.isEmpty()) {
            String mountain = selectedMountain.getName();
            JsonObject target = null;
            for (JsonObject obj : districts) {
                String sigun = getStringSafe(obj, "sigun");
                if (sigun.contains("구") || sigun.contains("군")) {
                    if (mountain.equals("북한산") && (sigun.contains("은평") || sigun.contains("강북") || sigun.contains("도봉"))) target = obj;
                    else if (mountain.equals("관악산") && (sigun.contains("관악") || sigun.contains("금천") || sigun.contains("동작"))) target = obj;
                    else if (mountain.equals("도봉산") && (sigun.contains("도봉") || sigun.contains("노원"))) target = obj;
                    else if (mountain.equals("청계산") && (sigun.contains("서초") || sigun.contains("과천"))) target = obj;
                    else if (mountain.equals("수락산") && (sigun.contains("노원") || sigun.contains("의정부") || sigun.contains("남양주"))) target = obj;
                    else if (mountain.equals("불암산") && (sigun.contains("노원") || sigun.contains("중랑") || sigun.contains("의정부"))) target = obj;
                    else if (mountain.equals("아차산") && (sigun.contains("광진") || sigun.contains("구리"))) target = obj;
                    else if (mountain.equals("용마산") && (sigun.contains("중랑") || sigun.contains("광진"))) target = obj;
                    else if (mountain.equals("인왕산") && (sigun.contains("종로") || sigun.contains("서대문"))) target = obj;
                    else if (mountain.equals("우면산") && (sigun.contains("서초"))) target = obj;
                }
            }
            if (target == null) target = districts.get(0);

            String mean = getStringSafe(target, "meanavg");
            try { meanValue = Integer.parseInt(mean); } catch (Exception ignore) {}

            // 위험도에 따라 최대 예약 인원 결정
            if (meanValue >= 86) {
                maxCapacity = 30;
            } else if (meanValue >= 66) {
                maxCapacity = 50;
            } else if (meanValue >= 51) {
                maxCapacity = 100;
            } else if (meanValue >= 0) {
                maxCapacity = 300;
            } else {
                maxCapacity = 300;
            }
        } else {
            // 데이터가 없으면 기본 300
            maxCapacity = 300;
        }

        // 현재 예약 인원은 난수로 설정 (0~maxCapacity)
        currentReservations = rand.nextInt(maxCapacity + 1);

        // 산 객체에 할당
        selectedMountain.setMaxCapacity(maxCapacity);
        selectedMountain.setCurrentReservations(currentReservations);

        // 예약 마감 체크
        if (currentReservations >= maxCapacity) {
            System.out.printf("❗️ 예약이 마감되었습니다. (최대 예약 인원: %d, 현재 예약 인원: %d)\n",
                    selectedMountain.getMaxCapacity(), selectedMountain.getCurrentReservations());
            return;
        }

        // 최종 예약 처리
        selectedMountain.reserve(tempReserver, tempDate, tempTimeSlot);

        System.out.println("\n예약이 완료되었습니다.");
        selectedMountain.printReservationInfo();
        System.out.println("산불 위험도 알림은 등산 전날 23시에 업데이트 되어 다시 한 번 알려드립니다.\n");

        if (now.isBefore(alarmTime)) {
            System.out.println("현재의 산불 위험도입니다.\n");
        } else {
            System.out.println("현재 예약 전날 23시 이후입니다. 산불 위험도를 조회합니다.\n");
        }

        // 이하 기존 산불 위험도 안내, QR 코드 생성 등은 기존 코드 그대로
        if (districts.isEmpty()) {
            System.out.println("해당 날짜에 대한 산불 위험도 데이터가 없습니다.");
        } else {
            String mountain = selectedMountain.getName();
            JsonObject target = null;
            for (JsonObject obj : districts) {
                String sigun = getStringSafe(obj, "sigun");
                if (sigun.contains("구") || sigun.contains("군")) {
                    if (mountain.equals("북한산") && (sigun.contains("은평") || sigun.contains("강북") || sigun.contains("도봉"))) target = obj;
                    else if (mountain.equals("관악산") && (sigun.contains("관악") || sigun.contains("금천") || sigun.contains("동작"))) target = obj;
                    else if (mountain.equals("도봉산") && (sigun.contains("도봉") || sigun.contains("노원"))) target = obj;
                    else if (mountain.equals("청계산") && (sigun.contains("서초") || sigun.contains("과천"))) target = obj;
                    else if (mountain.equals("수락산") && (sigun.contains("노원") || sigun.contains("의정부") || sigun.contains("남양주"))) target = obj;
                    else if (mountain.equals("불암산") && (sigun.contains("노원") || sigun.contains("중랑") || sigun.contains("의정부"))) target = obj;
                    else if (mountain.equals("아차산") && (sigun.contains("광진") || sigun.contains("구리"))) target = obj;
                    else if (mountain.equals("용마산") && (sigun.contains("중랑") || sigun.contains("광진"))) target = obj;
                    else if (mountain.equals("인왕산") && (sigun.contains("종로") || sigun.contains("서대문"))) target = obj;
                    else if (mountain.equals("우면산") && (sigun.contains("서초"))) target = obj;
                }
            }
            if (target == null) target = districts.get(0);

            String analdate = getStringSafe(target, "analdate");
            String doname = getStringSafe(target, "doname");
            String sigun = getStringSafe(target, "sigun");
            String mean = getStringSafe(target, "meanavg");
            String max = getStringSafe(target, "maxi");
            String min = getStringSafe(target, "mini");

            System.out.println("[" + userName + "님의 산불 위험도 알림]");
            System.out.println("▸ 분석 시간: " + analdate);
            System.out.println("▸ 지역: " + doname + " " + sigun);
            System.out.println("▸ 평균 위험지수: " + mean);
            System.out.println("▸ 최대 위험지수: " + max);
            System.out.println("▸ 최소 위험지수: " + min);

            if (meanValue >= 86) {
                System.out.println("⚠️ 산불 위험이 매우 높으니 등산을 삼가시기 바랍니다.");
            } else if (meanValue >= 66) {
                System.out.println("⚠️ 산불 위험이 다소 높으니 각별히 주의하세요.");
            } else if (meanValue >= 51) {
                System.out.println("산불 위험이 보통입니다. 주의하며 등산하세요.");
            } else if (meanValue >= 0) {
                System.out.println("산불 위험이 낮습니다. 안전한 등산 되세요.");
            } else {
                System.out.println("분석이 제공되지 않는 지역입니다, 더 나은 서비스를 위해 노력하겠습니다.");
            }
            System.out.println("----------------------------------");
        }

        String qrData = selectedMountain.getQRData();
        String fileName = "entry_" + userName + ".png";
        try {
            generateQRCode(qrData, fileName);
            System.out.println("✅ QR 코드 생성 완료! 파일명: " + fileName);
        } catch (Exception e) {
            System.out.println("❌ QR 코드 생성 오류: " + e.getMessage());
        }
    }

    public static void generateQRCode(String text, String filePath) throws WriterException, IOException {
        int width = 300;
        int height = 300;
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
    }

    private static List<JsonObject> getDistricts(String date, String analDateTime) {
        List<JsonObject> districts = new ArrayList<>();
        try {
            String encodedSido = URLEncoder.encode(SIDO_NAME, "UTF-8");
            String encodedDate = URLEncoder.encode(date, "UTF-8");
            String encodedAnalDate = URLEncoder.encode(analDateTime, "UTF-8");

            String apiUrl = "https://apis.data.go.kr/1400377/forestPoint/forestPointListSigunguSearch"
                    + "?serviceKey=" + SERVICE_KEY
                    + "&searchDate=" + encodedDate
                    + "&analdate=" + encodedAnalDate
                    + "&searchSido=" + encodedSido
                    + "&pageNo=1"
                    + "&numOfRows=50"
                    + "&_type=json";

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            JsonElement rootElem = JsonParser.parseString(response.toString());
            JsonObject rootObj = rootElem.getAsJsonObject();
            JsonObject body = rootObj.getAsJsonObject("response").getAsJsonObject("body");
            JsonObject items = body.getAsJsonObject("items");
            JsonElement itemElem = items.get("item");
            if (itemElem == null) return districts;

            if (itemElem.isJsonArray()) {
                JsonArray itemArr = itemElem.getAsJsonArray();
                for (JsonElement elem : itemArr) {
                    districts.add(elem.getAsJsonObject());
                }
            } else if (itemElem.isJsonObject()) {
                districts.add(itemElem.getAsJsonObject());
            }

        } catch (Exception e) {
            System.out.println("구 조회 오류: " + e.getMessage());
        }
        return districts;
    }

    private static String getStringSafe(JsonObject obj, String key) {
        return (obj.has(key) && !obj.get(key).isJsonNull()) ? obj.get(key).getAsString() : "정보 없음";
    }
}
