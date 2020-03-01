package top.hungrywu.enums;

/***
 *
 * @author : daviswujiahao
 * @date : 2020/2/27 3:46 下午
 **/
public enum HttpMethodEnum {
    GET(1), POST(2);

    private int idx;

    HttpMethodEnum(int idx) {
        this.idx = idx;
    }

    public int getIdx() {
        return idx;
    }

    public static HttpMethodEnum getHttpMethodByValue(int value) {
        for (HttpMethodEnum httpMethodEnum : HttpMethodEnum.values()) {
            if (value == httpMethodEnum.getIdx()) {
                return httpMethodEnum;
            }
        }
        return null;
    }

}

